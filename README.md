# Rota

Plataforma de delivery construída com arquitetura de microsserviços — projeto de portfólio fullstack.

## Stack

| Área | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security + JWT, Spring Data JPA |
| Arquitetura | Spring Cloud Gateway, Eureka, OpenFeign, Resilience4j |
| Dados | PostgreSQL (database per service), Flyway, Redis |
| Mensageria | RabbitMQ |
| Tempo real | Spring WebSocket (STOMP) |
| Frontend | React, TypeScript, Vite, TanStack Query, Zustand, Tailwind |
| Observabilidade | Actuator, Prometheus, Grafana, OpenTelemetry |
| Testes | JUnit 5, Mockito, Testcontainers |
| DevOps | Docker, Docker Compose, GitHub Actions |

## Usuários

- Cliente
- Restaurante
- Entregador
- Administrador

## Estrutura

```
backend/
  common/               exceções, JWT, eventos, outbox e filas compartilhados
  discovery-server/     registro de serviços (Eureka)                  :8761
  api-gateway/          entrada única: rotas, JWT, CORS e rate limit   :8080
  auth-service/         cadastro, login, JWT e refresh token           :8181  auth_db
  restaurant-service/   restaurantes, cardápio, cotação (cache Redis)  :8182  restaurant_db
  order-service/        carrinho (Redis), pedidos, cupons e avaliações :8183  order_db
  payment-service/      pagamentos (Strategy: fake e dinheiro)         :8184  payment_db
  delivery-service/     entregadores e corridas                        :8185  delivery_db
  notification-service/ e-mails e WebSocket (STOMP)                    :8186
frontend/               aplicação React                                :5173
infra/                  scripts de infraestrutura
```

Cada serviço segue a mesma organização em camadas:

```
interfaces/rest     controllers e DTOs
application         casos de uso e portas (interfaces para outros serviços)
domain              entidades, regras de negócio e repositórios
infrastructure      configuração, clientes HTTP, persistência externa
```

## Executando

Pré-requisitos: Docker e Node 18+. O backend compila dentro do Docker, então o JDK local é opcional.

```bash
docker compose up -d --build     # infraestrutura, Eureka, gateway e os serviços

cd frontend
npm install
npm run dev                      # http://localhost:5173 (chama o gateway em :8080)
```

| Painel | Endereço |
|---|---|
| Aplicação | http://localhost:5173 |
| API Gateway | http://localhost:8080/api/... |
| Eureka | http://localhost:8761 |
| RabbitMQ (rota / rota) | http://localhost:15672 |
| E-mails de teste (Mailpit) | http://localhost:8025 |

Os serviços Java têm limite de 448 MB cada no compose; a stack inteira usa cerca de 3 GB.

Contas de demonstração (senha `rota12345`):

| E-mail | Perfil |
|---|---|
| cliente@rota.dev | Cliente |
| restaurante@rota.dev | Restaurante (dono dos restaurantes de exemplo) |
| entregador@rota.dev | Entregador |
| admin@rota.dev | Administrador |

Para testar:

- **Cliente**: cupons `SAVE10` (10%, mín. R$ 50), `BEMVINDO` (R$ 15, mín. R$ 40) e `PIZZA20` (só no Forno da Nonna).
- **Pagamento fake**: Pix sempre aprovado; cartão recusado acima de R$ 500; dinheiro é confirmado na hora.
- **Restaurante**: em `/partner`, aceite o pedido, inicie o preparo e marque como pronto. Isso abre a corrida.
- **Entregador**: em `/driver`, fique online, use a posição de demonstração e aceite a entrega.
- **Tempo real**: abra o pedido como cliente em uma janela e o painel do restaurante ou do entregador em
  outra (janela anônima). Status, avisos e a posição do entregador no mapa mudam sem recarregar.
- **E-mails**: pagamento aprovado, saída para entrega, entrega e cancelamento chegam no Mailpit.

Testes do backend (sem JDK local):

```bash
./backend/mvn-docker.sh verify
```

## Arquitetura

```
                    React (Vite)
                         |  HTTP /api/**  e  WebSocket /ws
                         v
                    api-gateway  ---- JWT, CORS, rate limit (Redis), X-Request-Id
                         |  lb:// (Eureka)
     +---------+---------+----------+-----------+-------------+
     v         v         v          v           v             v
   auth   restaurant   order     payment     delivery    notification
              ^          |  ^  Feign + Resilience4j            |  e-mail (Mailpit)
              |          |  +--- cotação / cobrança            |  STOMP -> navegador
              |          v
              +------ RabbitMQ (exchange rota.events) ------+--+
```

Só duas chamadas continuam síncronas, porque o usuário espera a resposta: a **cotação** no catálogo
e a **cobrança** no pagamento. As duas passam por OpenFeign com timeout, retry com backoff e circuit breaker.
O resto da integração é por eventos:

| Evento | Publicado por | Consumido por |
|---|---|---|
| `order.status-changed` | order-service | notification (e-mail e WebSocket) |
| `order.ready-for-pickup` | order-service | delivery-service (abre a corrida) |
| `delivery.status-changed` | delivery-service | order-service (entregador, saiu, entregue), notification |
| `driver.location-updated` | delivery-service | notification (posição ao vivo para o cliente) |
| `review.created` | order-service | restaurant-service (média de avaliações) |

## Fluxo de um pedido

```
Cliente      checkout -------> order-service: recota (Feign) -> cupom -> cobra (Feign) -> PAID
Restaurante  aceita/prepara/pronto ------> order.ready-for-pickup ------> delivery-service abre a corrida
Entregador   aceita/retira/entrega ------> delivery.status-changed -----> order-service avança o pedido
Cliente      acompanha no mapa <--------- driver.location-updated <------ posição do entregador
Cliente      avalia ----------------------> review.created -------------> restaurant-service atualiza a média
```

## Destaques

- **Máquina de estados do pedido** (`OrderStatus`): só transições válidas são aceitas; as demais retornam 409.
- **Auditoria**: cada transição é registrada em `order_history` com usuário, horário e motivo.
- **Preço sempre vem do catálogo**: carrinho e checkout recotam os itens no restaurant-service; valores enviados pelo cliente são ignorados.
- **Refresh token com rotação** e detecção de reuso (revoga todas as sessões do usuário).
- **Autorização por perfil e por dono**: restaurante só vê e altera pedidos do próprio restaurante; cliente só cancela antes do aceite.
- **Pagamento com Strategy**: `PaymentProvider` com implementações trocáveis por configuração; cobrança idempotente por pedido.
- **Cupons**: validade, pedido mínimo, teto de desconto, limite de uso com incremento atômico e restrição por restaurante.
- **Entregas**: busca por entregadores próximos com Haversine, aceite concorrente protegido por lock otimista.
- **Transactional Outbox**: o evento é gravado na mesma transação da mudança e publicado por um relay
  com publisher confirms. Um pedido feito com o RabbitMQ fora do ar não perde nenhum evento.
- **Consumidores idempotentes e DLQ**: cada evento tem um `eventId`; reentregas são descartadas
  (`processed_events` ou `SET NX` no Redis). Após 4 tentativas com backoff, a mensagem vai para `<fila>.dlq`.
- **Domain events**: toda mudança de status do `Order` registra um evento de domínio, publicado pelo
  Spring Data ao salvar; nenhum caminho de código consegue mudar o status sem notificar.
- **Resiliência**: com o payment-service fora, o circuito abre após falhas seguidas, o checkout responde
  na hora e o pedido fica aguardando pagamento até o serviço voltar.
- **Cache transacional**: busca e detalhe de restaurantes no Redis, invalidados só depois do commit.
- **Gateway**: rejeita token inválido antes de chegar aos serviços, bloqueia `/internal/**` e limita
  o login a 10 tentativas seguidas por IP.
- **WebSocket autenticado**: JWT no frame CONNECT; cada usuário só assina a própria fila, e só entregadores
  assinam o tópico de corridas.

## Roadmap

- [x] Fase 1 — MVP: Auth, Restaurant, Product, Cart, Order
- [x] Fase 2 — Negócio: Payment, Coupon, Rating, Delivery
- [x] Fase 3 — Distribuído: RabbitMQ, Redis, WebSocket, Gateway, Eureka, Resilience4j
- [ ] Fase 4 — Produção: Docker, CI/CD, Prometheus, Grafana, OpenTelemetry
- [ ] Fase 5 — Qualidade: testes unitários e de integração, segurança, documentação
