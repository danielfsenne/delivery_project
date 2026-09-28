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
  common/               exceções, tratamento de erros e JWT compartilhados
  auth-service/         cadastro, login, JWT e refresh token          :8181  auth_db
  restaurant-service/   restaurantes, cardápio, horários e cotação     :8182  restaurant_db
  order-service/        carrinho (Redis) e pedidos                     :8183  order_db
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
docker compose up -d --build     # Postgres, Redis, RabbitMQ e os serviços

cd frontend
npm install
npm run dev                      # http://localhost:5173
```

Contas de demonstração (senha `rota12345`):

| E-mail | Perfil |
|---|---|
| cliente@rota.dev | Cliente |
| restaurante@rota.dev | Restaurante (dono dos restaurantes de exemplo) |
| entregador@rota.dev | Entregador |
| admin@rota.dev | Administrador |

Testes do backend (sem JDK local):

```bash
./backend/mvn-docker.sh verify
```

## Destaques

- **Máquina de estados do pedido** (`OrderStatus`): só transições válidas são aceitas; as demais retornam 409.
- **Auditoria**: cada transição é registrada em `order_history` com usuário, horário e motivo.
- **Preço sempre vem do catálogo**: carrinho e checkout recotam os itens no restaurant-service; valores enviados pelo cliente são ignorados.
- **Refresh token com rotação** e detecção de reuso (revoga todas as sessões do usuário).
- **Autorização por perfil e por dono**: restaurante só vê e altera pedidos do próprio restaurante; cliente só cancela antes do aceite.

## Roadmap

- [x] Fase 1 — MVP: Auth, Restaurant, Product, Cart, Order
- [ ] Fase 2 — Negócio: Payment, Coupon, Rating, Delivery
- [ ] Fase 3 — Distribuído: RabbitMQ, Redis, WebSocket, Gateway, Eureka, Resilience4j
- [ ] Fase 4 — Produção: Docker, CI/CD, Prometheus, Grafana, OpenTelemetry
- [ ] Fase 5 — Qualidade: testes unitários e de integração, segurança, documentação
