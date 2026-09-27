# Rota 🛵

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

- 👤 Cliente
- 🏪 Restaurante
- 🛵 Entregador
- 👨‍💼 Administrador

## Estrutura

```
backend/     microsserviços Spring Boot (Maven multi-módulo)
frontend/    aplicação React
infra/       configuração de infraestrutura (bancos, métricas)
```

## Executando

```bash
docker compose up -d        # infraestrutura (Postgres, Redis, RabbitMQ)
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Roadmap

- [ ] Fase 1 — MVP: Auth, Restaurant, Product, Cart, Order
- [ ] Fase 2 — Negócio: Payment, Coupon, Rating, Delivery
- [ ] Fase 3 — Distribuído: RabbitMQ, Redis, WebSocket, Gateway, Eureka, Resilience4j
- [ ] Fase 4 — Produção: Docker, CI/CD, Prometheus, Grafana, OpenTelemetry
- [ ] Fase 5 — Qualidade: testes unitários e de integração, segurança, documentação
