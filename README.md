<p align="center">
  <img src="art/logo.png" alt="Hexagonal Betting Engine" width="600" />
</p>

<h1 align="center">Hexagonal Betting Engine</h1>

> A distributed betting settlement system built with **Spring Boot 4**, **Kafka**, and **RocketMQ** using **Hexagonal Architecture**.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Kafka%20Client-4.1.1-black.svg)](https://kafka.apache.org/)
[![RocketMQ](https://img.shields.io/badge/RocketMQ%20Client-5.3.2-red.svg)](https://rocketmq.apache.org/)
[![H2](https://img.shields.io/badge/H2-2.4.240-blue.svg)](https://www.h2database.com/)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Communication Flow](#-communication-flow)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Monitoring](#-monitoring)
- [Design Decisions](#-design-decisions)

---

## 🎯 Overview

The **Hexagonal Betting Engine** is an event-driven microservice that processes betting settlements in real-time. It consumes event outcomes from Kafka, updates bet statuses in the database, and publishes settlement notifications to RocketMQ.

### Key Features

- ✅ **Event-Driven Architecture** - Kafka for event ingestion, RocketMQ for settlement notifications
- ✅ **Hexagonal Architecture** - Clean separation between domain, application, and infrastructure layers
- ✅ **Transactional Consistency** - ACID guarantees with Spring `@Transactional`
- ✅ **Error Handling** - Dead Letter Queue (DLQ) for failed messages
- ✅ **Comprehensive Testing** - Unit tests, integration tests, and E2E tests
- ✅ **In-Memory Database** - H2 for development and testing
- ✅ **OpenAPI Documentation** - Swagger UI for API exploration

---

## 🏛️ Architecture

This project implements **Hexagonal Architecture** (Ports & Adapters) to achieve high testability, maintainability, and independence from external frameworks.

```mermaid
graph TB
    subgraph "Application Layer"
        Controller[EventOutcomeController<br/>REST API]
    end

    subgraph "Domain Layer"
        CommandHandler[EventOutcomeCommandHandler]
        BetSettlementService[BetSettlementService]
    end

    subgraph "Infrastructure Layer"
        KafkaProducer[EventOutcomePublisherAdapter<br/>Kafka Producer]
        KafkaConsumer[EventOutcomeListenerAdapter<br/>Kafka Consumer]
        DBAdapter[BetRepositoryAdapter<br/>JPA]
        RocketMQAdapter[RocketMQBetSettlementPublisher /<br/>LoggingBetSettlementPublisher]
    end

    subgraph "External Systems"
        Kafka[(Kafka<br/>event-outcomes)]
        DB[(H2 Database)]
        RocketMQ[(RocketMQ Broker<br/>bet-settlements)]
    end

    Controller --> CommandHandler
    CommandHandler --> KafkaProducer
    KafkaProducer --> Kafka
    Kafka --> KafkaConsumer
    KafkaConsumer --> BetSettlementService
    BetSettlementService --> DBAdapter
    BetSettlementService --> RocketMQAdapter
    DBAdapter --> DB
    RocketMQAdapter --> RocketMQ

    style CommandHandler fill:#4CAF50
    style BetSettlementService fill:#4CAF50
    style Controller fill:#2196F3
```

### Architectural Layers

| Layer | Responsibility | Examples |
|-------|---------------|----------|
| **Domain** | Core business logic, entities, ports | `Bet`, `BetSettlementService`, `BetRepository` |
| **Application** | Use cases, DTOs, exception handling | `EventOutcomeController`, `GlobalExceptionHandler` |
| **Infrastructure** | External integrations, adapters | Kafka, RocketMQ, JPA, H2 |

---

## 💻 Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 4.0.2 |
| **Messaging** | Kafka Client | 4.1.1 |
| **Messaging** | RocketMQ Client | 5.3.2 |
| **Database** | H2 (In-Memory) | Latest |
| **Migration** | Flyway | Latest |
| **Build Tool** | Gradle | 9.3 |
| **Testing** | JUnit 5, Mockito, AssertJ | Latest |
| **Documentation** | SpringDoc OpenAPI | 3.0.1 |
| **Docker** | Kafka (Confluent) | 7.7.7 |
| **Docker** | RocketMQ | 4.9.7 |

---

## 🔄 Communication Flow

### End-to-End Settlement Flow

```mermaid
sequenceDiagram
    participant Client
    participant API as REST API
    participant Kafka as Kafka Topic<br/>event-outcomes
    participant Consumer as Kafka Consumer
    participant Service as BetSettlementService
    participant DB as H2 Database
    participant RMQ as RocketMQ<br/>bet-settlements

    Client->>API: POST /api/event-outcomes
    API->>Kafka: Publish EventOutcome
    API-->>Client: 202 Accepted

    Note over Kafka,Consumer: Asynchronous Processing

    Kafka->>Consumer: Poll EventOutcome
    Consumer->>Service: settle(EventOutcome)
    Service->>DB: Find pending bets by eventId
    DB-->>Service: List<Bet>

    loop For each pending bet
        Service->>Service: Calculate status (WON/LOST)
        Service->>DB: Update bet status
        Service->>RMQ: Publish BetSettlement
    end

    Service-->>Consumer: Settlement complete
```

### Kafka Error Handling with DLQ

```mermaid
graph LR
    A[Kafka Topic<br/>event-outcomes] --> B{Consumer<br/>Processing}
    B -->|Success| C[BetSettlementService]
    B -->|Failure| D[Retry 3x]
    D -->|Still Fails| E[DLQ Topic<br/>event-outcomes-dlq]
    D -->|Success| C
    C --> F[Database Update]
    C --> G[RocketMQ Publish]

    style E fill:#f44336
    style C fill:#4CAF50
```

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/mario/hexagonalbettingengine/
│   │   ├── domain/                      # 🟢 Domain Layer (Core Business Logic)
│   │   │   ├── betting/
│   │   │   │   ├── Bet.java            # Domain entity
│   │   │   │   ├── BetStatus.java      # Domain enum
│   │   │   │   ├── BetRepository.java  # Port (interface)
│   │   │   │   ├── BetSettlement.java  # Port (interface)
│   │   │   │   ├── BetSettlementService.java  # Domain service
│   │   │   │   └── BetSettlementPublisher.java # Port (interface)
│   │   │   └── eventoutcome/
│   │   │       ├── EventOutcome.java
│   │   │       └── EventOutcomeCommandHandler.java
│   │   │
│   │   ├── application/                 # 🔵 Application Layer (Use Cases)
│   │   │   ├── eventoutcome/
│   │   │   │   ├── EventOutcomeController.java
│   │   │   │   ├── request/
│   │   │   │   └── mapper/
│   │   │   └── GlobalExceptionHandler.java
│   │   │
│   │   └── infrastructure/              # 🟠 Infrastructure Layer (Adapters)
│   │       ├── betting/
│   │       │   ├── BetEntity.java       # JPA entity
│   │       │   ├── BetJpaRepository.java
│   │       │   ├── BetRepositoryAdapter.java  # Port implementation
│   │       │   ├── RocketMQBetSettlementPublisher.java
│   │       │   ├── LoggingBetSettlementPublisher.java
│   │       │   └── mapper/
│   │       ├── eventoutcome/
│   │       │   ├── EventOutcomeListenerAdapter.java  # Kafka consumer
│   │       │   ├── EventOutcomePublisherAdapter.java # Kafka producer
│   │       │   └── mapper/
│   │       └── config/
│   │           ├── KafkaConfig.java
│   │           ├── KafkaTopicConfig.java
│   │           └── MessagingProperties.java
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-test.yml
│       └── db/migration/
│           └── V1__init_schema.sql
│
└── test/
    ├── java/com/mario/hexagonalbettingengine/
    │   ├── domain/                      # Unit tests
    │   ├── infrastructure/              # Integration tests
    │   └── BetSettlementEndToEndIT.java # E2E test
    └── resources/
        └── application-test.yml
```

---

## 🚀 Getting Started

### Prerequisites

- **Docker** 🐳
- **Docker Compose** 🐙
- **Java 21**
- **Gradle 9+** (optional, wrapper included)

### Quick Start

#### 1️⃣ Clone the Repository

```bash
git clone <repository-url>
cd hexagonal-betting-engine
```

#### 2️⃣ Start Infrastructure Services

Start Kafka, RocketMQ, and their UIs using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- **Kafka** on `localhost:9092`
- **Kafka UI** on `http://localhost:8090`
- **RocketMQ NameServer** on `localhost:9876`
- **RocketMQ Broker** on `localhost:10911`
- **RocketMQ Dashboard** on `http://localhost:8082`

#### 3️⃣ Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

---

## 📚 API Documentation

### Swagger UI

Access the interactive API documentation at:

```
http://localhost:8080/swagger-ui/index.html
```

![Swagger UI](art/swagger.png)

### Core Endpoint

#### 🎲 Publish Event Outcome

Publishes an event outcome to Kafka, triggering bet settlement for all pending bets on that event.

```bash
curl -X POST http://localhost:8080/api/event-outcomes \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "match-100",
    "eventName": "Real Madrid vs Barcelona",
    "eventWinnerId": "REAL_MADRID"
  }'
```

**Response:**
```
HTTP/1.1 202 Accepted
```

#### 📊 Seeded Test Data

The application comes with **pre-seeded pending bets** via Flyway migration (`V2__seed_initial_bets.sql`):

| Event ID | Event Name | Seeded Bets | Available Winners |
|----------|------------|-------------|-------------------|
| `match-100` | Real Madrid vs Barcelona | 4 bets | `REAL_MADRID`, `BARCELONA`, `DRAW` |
| `match-200` | Liverpool vs Milan | 3 bets | `LIVERPOOL`, `MILAN` |
| `match-300` | Lakers vs Celtics | 3 bets | `LAKERS`, `CELTICS` |

**Example: Settle match-100 bets**
```bash
curl -X POST http://localhost:8080/api/event-outcomes \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "match-100",
    "eventName": "Real Madrid vs Barcelona",
    "eventWinnerId": "REAL_MADRID"
  }'
```

This will:
- Find 4 pending bets for `match-100`
- Mark 2 bets as **WON** (bet-ids: `b-001`, `b-004`)
- Mark 2 bets as **LOST** (bet-ids: `b-002`, `b-003`)
- Publish 4 settlement notifications to RocketMQ

---

## 🧪 Testing

### Test Coverage

The project has **comprehensive test coverage** across all architectural layers:

| Test Type | Coverage |
|-----------|----------|
| **Unit Tests** | Domain & Application layers |
| **Integration Tests** | Kafka, RocketMQ, Database |
| **E2E Tests** | Full flow: Kafka → DB → RocketMQ |

### Running Tests

#### Run All Tests

```bash
./gradlew test
```

#### Run Only Unit Tests

```bash
./gradlew test --tests "*Test"
```

#### Run Only Integration Tests

```bash
./gradlew test --tests "*IT"
```

### Test Report

After running tests, view the HTML report:

```bash
open build/reports/tests/test/index.html
```

---

## 📊 Monitoring

### Kafka UI

Monitor Kafka topics, consumer groups, and messages:

```
http://localhost:8090
```

**Navigation:**
1. Go to **Topics** → `event-outcomes`
2. Click **Messages** to see consumed events
3. Check **Consumer Groups** for processing status

#### Event Outcomes Topic

![Kafka Event Outcomes Topic](art/event-outcomes.png)

#### Dead Letter Queue (DLQ)

![Kafka DLQ Topic](art/event-outcomes-dlq.png)

### RocketMQ Dashboard

Monitor RocketMQ topics and message flows:

```
http://localhost:8082
```

![RocketMQ Dashboard](art/rocketmq.png)

**Features:**
- View topic statistics
- Monitor message traces
- Check consumer status

### H2 Database Console

Access the H2 in-memory database console for development and debugging:

```
http://localhost:8080/h2-console
```

![H2 Console](art/h2-console.png)

**Connection details:**
- JDBC URL: `jdbc:h2:mem:betting-db`
- Username: `sa`
- Password: _(leave empty)_

---

## 🎨 Design Decisions

### 1. **Hexagonal Architecture (Ports & Adapters)**

**Why:** Achieve independence from frameworks and external systems.

- **Domain Layer** contains pure business logic with no external dependencies
- **Ports** (interfaces) define contracts between layers
- **Adapters** (implementations) handle external integrations (Kafka, RocketMQ, JPA)

**Benefits:**
- ✅ Testability: Domain logic can be tested without infrastructure
- ✅ Flexibility: Easy to swap adapters (e.g., replace Kafka with RabbitMQ)
- ✅ Maintainability: Clear separation of concerns

### 2. **Event-Driven Architecture**

**Why:** Decouple event ingestion from settlement processing.

- REST API publishes events to Kafka (non-blocking)
- Kafka consumer processes events asynchronously
- RocketMQ publishes settlement notifications to downstream systems

**Benefits:**
- ✅ Scalability: Consumers can scale independently
- ✅ Resilience: Failed messages go to DLQ for manual review
- ✅ Performance: Non-blocking API responses

### 3. **Domain-Driven Design (DDD)**

**Why:** Model complex business rules explicitly.

- `Bet` aggregate encapsulates bet validation and status transitions
- `BetSettlementService` orchestrates settlement logic
- Domain events could be added for audit trails (future enhancement)

### 4. **Transactional Outbox Pattern** (Implicit)

**Why:** Ensure consistency between database updates and message publishing.

- Settlement updates and RocketMQ publishing happen within a single `@Transactional` boundary
- If RocketMQ fails, database transaction rolls back

### 5. **Configuration-Driven Design**

**Why:** Externalize configuration for different environments.

```yaml
app:
  messaging:
    rocketmq:
      enabled: true
      topic: bet-settlements
    kafka:
      event-outcomes:
        topic: event-outcomes
        dlq-topic: event-outcomes-dlq
        retry-attempts: 3
```

### 6. **Dead Letter Queue (DLQ) Pattern**

**Why:** Handle poison messages without blocking the consumer.

- Failed messages are retried 3 times with exponential backoff
- After 3 failures, message is sent to `event-outcomes-dlq`
- DLQ messages can be manually reviewed and reprocessed

### 7. **Type-Safe Configuration with Records**

**Why:** Immutable, compile-time-safe configuration.

```java
@ConfigurationProperties(prefix = "app.messaging")
public record MessagingProperties(
    RocketMqConfig rocketmq,
    KafkaConfig kafka
) { }
```

---