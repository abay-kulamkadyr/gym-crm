# Gym CRM System

A comprehensive Customer Relationship Management system for gym operations.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Configuration](#configuration)
    - [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Database](#database)
- [Messaging](#messaging)
- [Service Discovery & Inter-Service Communication](#service-discovery--inter-service-communication)
- [Monitoring and Observability](#monitoring-and-observability)
- [Development](#development)
- [Testing](#testing)
- [License](#license)

## Overview

The Gym CRM System is an application designed to manage gym operations including trainee and trainer profiles, training sessions, and user authentication. The system is built as part of a microservices ecosystem, integrating with a dedicated **Trainer Workload Service** via Kafka messaging and Feign HTTP clients. It implements industry best practices including onion, clean, hexagonal architecture patterns, resilience patterns (circuit breaker), and comprehensive security measures.

## Architecture

The application follows **Onion Architecture** (with elements of Clean and Hexagonal Architecture) principles with separation of concerns:

```
┌─────────────────────────────────────────┐
│         Interfaces Layer (Web)          │
│  Controllers, DTOs, REST APIs,          │
│  Feign Clients, Client DTOs             │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│        Application Layer                │
│   Facades, Services, Domain Validation, │
│   Messaging Events & Publishers         │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Domain Layer                   │
│   Domain Entities, Ports                │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│      Infrastructure Layer               │
│ Persistence, Security, Logging,         │
│ Monitoring, Kafka Config                │
└─────────────────────────────────────────┘
```

### Layer Responsibilities

- **Domain Layer**: Core business entities and repository interfaces (ports)
- **Application Layer**: Business logic orchestration, application services, and outbound messaging events
- **Infrastructure Layer**: Technical implementations (JPA, Security, Logging, Monitoring, Kafka producer config)
- **Interfaces Layer**: REST API controllers, DTOs, Feign clients for external service communication

## Features

### User Management

- **Trainee Management**
    - Registration with automatic username and password generation
    - Profile management (update, delete)
    - Date of birth and address tracking
    - Trainer assignment and management

- **Trainer Management**
    - Registration with automatic username and password generation
    - Training type specialization management (ex. CARDIO, STRENGTH, YOGA, etc)
    - Profile management (update, delete)
    - Trainee assignment tracking

### Training Management

- Create and track training sessions
- Delete training sessions
- Filter trainings by date, trainer/trainee, and type
- Duration tracking
- Support for defining training types (using enums)
- **Automatic workload event publishing** on training create/delete via Kafka

### Messaging & Event-Driven Integration

- **Kafka Producer**: publishes `TrainerWorkloadEvent` to the `gym.trainings.created` topic on every training creation and deletion
- **Fire-and-forget** publish pattern — the HTTP transaction commits independently of Kafka acknowledgement
- Events carry a `transactionId` (propagated from MDC) for end-to-end traceability across services
- `ADD` and `DELETE` action types allow the downstream Trainer Workload Service to maintain an accurate running summary

### Authentication & Security

- **JWT-based Authentication**
    - Secure token generation
    - Token validation and refresh
    - Token blacklisting on logout

- **Brute Force Protection**
    - Failed login attempt tracking
    - Account lockout after multiple failures
    - Configurable lockout duration

- **Password Management**
    - Secure password hashing (BCrypt)
    - Password change functionality
    - Strong password validation
    - Auto-generated passwords on registration

### Monitoring & Observability

- **Health Checks**
    - Database connectivity
    - Training types availability

- **Metrics** (via Micrometer)
    - Trainee/Trainer registration counts
    - Login attempt tracking
    - Failed authentication monitoring

- **Structured Logging**
    - Transaction ID tracking across services
    - MDC (Mapped Diagnostic Context) support
    - Request/Response logging interceptor
    - Comprehensive error logging

### Additional Features

- CORS configuration for cross-origin requests
- OpenAPI/Swagger documentation
- Database bootstrapping with initial JSON data
- Comprehensive input validation
- Global exception handling
- RESTful API design

## Technology Stack

### Core Framework
- **Spring Boot 3.x** - Application framework
- **Java 17+** - Programming language

### Persistence
- **Spring Data JPA** - Data access layer
- **Hibernate** - ORM implementation
- **H2/PostgreSQL** - Database (configurable per profile)

### Security
- **Spring Security 6.x** - Security framework
- **JWT (JSON Web Tokens)** - Authentication
- **BCrypt** - Password encryption

### Messaging
- **Apache Kafka** - Asynchronous event streaming
- **Spring Kafka** - Kafka producer integration

### Service Discovery & Communication
- **Spring Cloud Netflix Eureka Client** - Service registration and discovery
- **Spring Cloud OpenFeign** - Declarative HTTP client for inter-service calls
- **Resilience4j Circuit Breaker** - Fault tolerance for downstream service calls

### Monitoring & Observability
- **Spring Boot Actuator** - Health checks and metrics
- **Micrometer** - Application metrics
- **SLF4J/Logback** - Logging framework

### Documentation
- **SpringDoc OpenAPI** - API documentation (Swagger UI)

### Build
- **Maven** - Dependency management and build tool

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose (for local infrastructure)

### Installation

1. **Clone the repository**
   ```bash
   git clone git@github.com:abay-kulamkadyr/gym-crm.git
   cd gym-crm-system
   ```

2. **Build the project**
   ```bash
   ./mvnw clean install
   ```

### Configuration

The application uses Spring profiles for environment-specific configurations.

#### Application Properties

Key properties (set via environment variables or `application-{profile}.properties`):

```properties
# Server Configuration
server.port=8082
spring.application.name=gym-crm

# Database Configuration
spring.datasource.url=${POSTGRESQL_URL}
spring.datasource.username=${POSTGRESQL_USERNAME}
spring.datasource.password=${POSTGRESQL_PASSWORD}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=create-drop

# JWT Configuration
security.jwt.secret=your-secret-key-min-256-bits
security.jwt.lifetime=30m

# Login Security — Brute force protection
security.login.max-attempts=3
security.login.penalty=5m

# CORS Configuration
security.cors.allowed-origins=http://localhost:3000

# Kafka
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS}
app.kafka.topics.training-created=gym.trainings.created

# Service Discovery (Eureka)
eureka.client.serviceUrl.defaultZone=${EUREKA_URL:http://localhost:8761/eureka/}

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Bootstrap Data — JSON files (local profile only)
storage.init.users=/users.json
storage.init.trainees=/trainees.json
storage.init.trainers=/trainers.json
storage.init.trainings=/trainings.json
storage.init.trainingTypes=/trainingTypes.json
```

#### Security Configuration

**Important**: Change the JWT secret in production:

```bash
openssl rand -base64 32
```

#### Profiles

| Profile | Database | DDL | Bootstrap Data | Swagger | Kafka |
|---------|----------|-----|----------------|---------|-------|
| `local` | Local PostgreSQL (Docker) | `create` | JSON files | Enabled | `localhost:9092` |
| `staging` | External PostgreSQL | `validate` | None | Disabled | Env var |
| `prod` | External PostgreSQL | `validate` | None | Disabled | Env var |

Activate a profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Running the Application (local profile)

The local profile requires PostgreSQL and Kafka. A `docker-compose.yml` is provided for both:

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

#### Using JAR

```bash
./mvnw clean package
docker compose up -d
java -Dspring.profiles.active=local -jar target/gym-crm-1.0-SNAPSHOT.jar
```

The application will start on `http://localhost:8082`.

## API Documentation

### Swagger UI

Access the interactive API documentation at (local profile only):
```
http://localhost:8082/swagger-ui/index.html
```

### OpenAPI Specification

```
http://localhost:8082/v3/api-docs
```

### Main Endpoints

#### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/logout` | User logout | Yes |
| PUT | `/api/auth/password` | Change password | Yes |

#### Trainees

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/trainees` | Register trainee | No |
| GET | `/api/trainees/{username}` | Get trainee profile | Yes |
| PUT | `/api/trainees/{username}` | Update trainee profile | Yes |
| DELETE | `/api/trainees/{username}` | Delete trainee | Yes |
| PATCH | `/api/trainees/{username}/activation` | Toggle activation | Yes |
| GET | `/api/trainees/{username}/trainings` | Get trainings | Yes |
| GET | `/api/trainees/{username}/available-trainers` | Get available trainers | Yes |
| PUT | `/api/trainees/{username}/trainers` | Update trainer list | Yes |

#### Trainers

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/trainers` | Register trainer | No |
| GET | `/api/trainers/{username}` | Get trainer profile | Yes |
| PUT | `/api/trainers/{username}` | Update trainer profile | Yes |
| PATCH | `/api/trainers/{username}/activation` | Toggle activation | Yes |
| GET | `/api/trainers/{username}/trainings` | Get trainings | Yes |

#### Trainings

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/trainings` | Create training | Yes |
| DELETE | `/api/trainings` | Delete training | Yes |

#### Training Types

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/training-types` | Get all training types | Yes |

### Example Requests

#### Register Trainee

```bash
curl -X POST http://localhost:8082/api/trainees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-01",
    "address": "123 Main St"
  }'
```

Response:
```json
{
  "username": "John.Doe",
  "password": "Ab12Cd34Ef"
}
```

#### Login

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "John.Doe",
    "password": "Ab12Cd34Ef"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Create Training (triggers Kafka event)

```bash
curl -X POST http://localhost:8082/api/trainings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "traineeUsername": "John.Doe",
    "trainerUsername": "Jane.Smith",
    "trainingName": "Morning Cardio",
    "trainingDate": "2025-06-01T09:00:00",
    "trainingDurationMin": 60
  }'
```

This call saves the training and asynchronously publishes an `ADD` workload event to Kafka.

#### Delete Training (triggers Kafka event)

```bash
curl -X DELETE http://localhost:8082/api/trainings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "traineeUsername": "John.Doe",
    "trainerUsername": "Jane.Smith",
    "trainingDate": "2025-06-01T09:00:00"
  }'
```

This call removes the training and publishes a `DELETE` workload event to Kafka.

## Security

### Authentication Flow

1. **Registration**: User registers and receives auto-generated credentials
2. **Login**: User authenticates with username/password, receives JWT token
3. **Authorization**: JWT token included in `Authorization: Bearer <token>` header
4. **Logout**: Token is blacklisted and invalidated

### Authorization

- Users can only access their own resources
- `@PreAuthorize` annotations enforce resource ownership
- Example: `@PreAuthorize("#username == authentication.name")`
- Training endpoints allow access if the authenticated user is either the trainee or the trainer

### Brute Force Protection

- Maximum failed attempts: 3 (configurable via `security.login.max-attempts`)
- Lockout duration: 5 minutes (configurable via `security.login.penalty`)
- Automatic cleanup of expired locks (scheduled hourly)

### Password Requirements

- Minimum length: 10 characters
- Maximum length: 100 characters
- Cannot be only whitespace
- Stored using BCrypt hashing

## Database

### Entity Relationship Diagram

```
┌─────────────┐       ┌──────────────┐        ┌─────────────────┐
│   UserDAO   │       │  TraineeDAO  │        │   TrainerDAO    │
├─────────────┤       ├──────────────┤        ├─────────────────┤
│ userId (PK) │──────>│ traineeId    │        │ trainerId (PK)  │
│ username    │       │ userId (FK)  │        │ userId (FK)     │
│ password    │       │ dob          │        │ trainingType(FK)│
│ role        │       │ address      │        └────────┬────────┘
│ active      │       └──────┬───────┘                 │
└─────────────┘              │                         │
                             │    trainee_trainer      │
                             └──────────(M:M)──────────┘
                                         │
                                  ┌──────▼───────────┐    ┌──────────────────┐
                                  │   TrainingDAO    │    │ TrainingTypeDAO  │
                                  ├──────────────────┤    ├──────────────────┤
                                  │ trainingId (PK)  │    │ trainingTypeId   │
                                  │ traineeId (FK)   │    │ trainingTypeName │
                                  │ trainerId (FK)   │    └──────────────────┘
                                  │ typeId (FK)      │
                                  │ name             │
                                  │ date             │
                                  │ duration         │
                                  └──────────────────┘
```

### Database Initialization

In the `local` profile, the database is automatically populated from JSON files on startup (skipped if data already exists):

- `users.json`: User accounts with hashed passwords
- `trainees.json`: Trainee profiles with trainer assignments
- `trainers.json`: Trainer profiles with specializations
- `trainings.json`: Training session history
- `trainingTypes.json`: Training type definitions

## Messaging

### Kafka Integration

The application produces events to Kafka whenever a training session is created or deleted. This keeps the downstream **Trainer Workload Service** in sync without tight coupling.

#### Topic Configuration

| Property | Value |
|----------|-------|
| Topic | `gym.trainings.created` (configurable via `app.kafka.topics.training-created`) |
| Partitions | 3 |
| Replicas | 1 |
| Key | Trainer username (ensures ordering per trainer) |
| Value | `TrainerWorkloadEvent` (JSON serialized) |

#### Event Schema — `TrainerWorkloadEvent`

```json
{
  "trainerUsername": "Jane.Smith",
  "trainerFirstname": "Jane",
  "trainerLastname": "Smith",
  "isActive": true,
  "trainingDate": "2025-06-01T09:00:00",
  "trainingDurationMinutes": 60,
  "actionType": "ADD",
  "transactionId": "TXN-1748700000000-a1b2c3d4"
}
```

`actionType` is either `ADD` (training created) or `DELETE` (training deleted).

#### Delivery Semantics

Events are published **fire-and-forget** — the HTTP response is returned to the caller once the database transaction commits, regardless of whether Kafka has acknowledged the message. Failures are logged but do not roll back the transaction. 

#### Transaction ID Propagation

The `transactionId` field in each event is populated from the MDC context (set by `TransactionIdFilter`). This allows correlation of a single user request across the Gym CRM logs and the Trainer Workload Service logs.

## Service Discovery & Inter-Service Communication

### Eureka Service Discovery

The application registers itself with a Eureka Server on startup, enabling other services in the ecosystem to discover it by name rather than hard-coded URLs.

```properties
eureka.client.serviceUrl.defaultZone=${EUREKA_URL:http://localhost:8761/eureka/}
spring.application.name=gym-crm
```

### Feign Client — Trainer Workload Service

A declarative Feign client (`TrainerWorkloadClient`) allows the Gym CRM to query trainer workload summaries from the dedicated workload service:

```java
@FeignClient(name = "trainer-workload-service", fallbackFactory = TrainerWorkloadFallbackFactory.class)
public interface TrainerWorkloadClient {
    @GetMapping("/api/workload/{username}")
    TrainerSummaryResponse getTrainerSummary(@PathVariable("username") String username);
}
```

The client resolves the target host via Eureka using the service name `trainer-workload-service`.

#### Header Propagation

The `FeignInterceptor` automatically propagates two headers from the incoming request to all outbound Feign calls:

| Header | Purpose |
|--------|---------|
| `Authorization` | Forwards the JWT token so downstream services can authenticate the caller |
| `X-Transaction-Id` | Forwards the transaction ID for end-to-end request tracing |

### Circuit Breaker

Feign calls are protected by a **Resilience4j circuit breaker**. If the Trainer Workload Service becomes unavailable, the circuit opens and the fallback is invoked:

```java
// Fallback returns an empty summary rather than propagating the error
return new TrainerSummaryResponse(username, "Unavailable", "Unavailable", false, Collections.emptyList());
```

#### Circuit Breaker Configuration

```properties
spring.cloud.openfeign.circuitbreaker.enabled=true
resilience4j.circuitbreaker.instances.trainer-workload-service.slidingWindowSize=20
resilience4j.circuitbreaker.instances.trainer-workload-service.failureRateThreshold=50
resilience4j.circuitbreaker.instances.trainer-workload-service.waitDurationInOpenState=20s
resilience4j.timelimiter.instances.trainer-workload-service.timeoutDuration=3s
```

The circuit opens when 50% of calls in a sliding window of 20 fail, and stays open for 20 seconds before attempting recovery. Individual calls time out after 3 seconds.

## Monitoring and Observability

### Health Checks

Access health status:
```bash
curl http://localhost:8082/actuator/health
```

Available health indicators:

| Indicator | Description | Profile |
|-----------|-------------|---------|
| `database` | Database connectivity | All |
| `training-types` | Verifies ≥5 training types exist in DB | All |
| `bootstrap` | Bootstrap data loading status | `local` only |

### Metrics with Prometheus & Grafana

#### Prerequisites

- Docker and Docker Compose installed
- Application running on `http://localhost:8082`

#### Quick Start

1. **Start Prometheus and Grafana**

   ```bash
   cd prometheus-grafana
   docker-compose up -d
   ```

2. **Verify Services**

    - **Prometheus**: http://localhost:9090
    - **Grafana**: http://localhost:3000
    - **Application Metrics**: http://localhost:8082/actuator/prometheus

3. **Access Grafana Dashboard**

    - URL: http://localhost:3000
    - Username: `admin`
    - Password: `grafana`

4. **View Metrics in Prometheus**

   ```promql
   # Total trainee registrations
   trainee_registered_total

   # Login attempts rate (per second)
   rate(user_login_attempts_total[5m])

   # Failed login rate
   rate(user_login_failed_total[5m])

   # JVM memory usage
   jvm_memory_used_bytes

   # HTTP request duration
   http_server_requests_seconds_sum
   ```

#### Custom Application Metrics

| Metric | Description |
|--------|-------------|
| `trainee_registered_total` | Total trainee registrations |
| `trainer_registered_total` | Total trainer registrations |
| `user_login_attempts_total` | Total login attempts |
| `user_login_failed_total` | Failed login attempts |

```bash
# List all metrics
curl http://localhost:8082/actuator/metrics

# Inspect a specific metric
curl http://localhost:8082/actuator/metrics/trainee_registered_total
```

#### Sample Grafana Panels

```promql
# Login Attempts
rate(user_login_attempts_total[5m])

# Failed Login Rate
rate(user_login_failed_total[5m])

# Registration Metrics
trainee_registered_total + trainer_registered_total

# JVM Memory by Area
sum(jvm_memory_used_bytes) by (area)

# HTTP Request Rate
rate(http_server_requests_seconds_count[5m])
```

Import pre-built dashboards: ID `4701` (JVM Micrometer) or `11378` (Spring Boot Statistics) from Grafana.

#### Prometheus Configuration

Located in `prometheus-grafana/prometheus/prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'gym-crm'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['host.docker.internal:8082']
```

> **Note**: On Linux, replace `host.docker.internal` with `172.17.0.1`.

#### Stopping Monitoring Stack

```bash
cd prometheus-grafana
docker-compose down        # Stop services
docker-compose down -v     # Stop and remove volumes
```

### Logging

All logs include transaction IDs for cross-service request tracing:

```
INFO [TXN-1748700000000-a1b2c3d4] Authentication attempt for user: john.doe
```

Log levels can be configured per package:
```properties
logging.level.com.epam=DEBUG
logging.level.org.springframework.security=DEBUG
```

## Development

### Code Style

The project follows standard Java conventions:
- Use meaningful variable names
- Follow SOLID principles
- Write self-documenting code
- Add comments for complex logic only

### Adding New Features

1. **Define Domain Model**: Add entities to `domain/model/`
2. **Create Repository Interface**: Add port to `domain/port/`
3. **Implement Repository**: Add implementation in `infrastructure/persistence/repository/`
4. **Create Service**: Add business logic in `application/service/`
5. **Add Controller**: Expose via REST API in `interfaces/web/controller/`
6. **Write Tests**: Add unit and integration tests

### Adding a New Kafka Event

1. Define the event POJO in `application/messaging/event/`
2. Add a publisher method to `TrainingEventPublisher` (or create a new publisher)
3. Declare the topic in `application.properties` and auto-create it via `KafkaProducerConfig`
4. Call the publisher from the relevant service after the database operation

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TraineeServiceTest

# Run with coverage
./mvnw test jacoco:report
```

### Test Infrastructure

| Test Type | Technology                                            | Notes |
|-----------|-------------------------------------------------------|-------|
| Unit tests | JUnit 5 + Mockito                                     | Service and utility classes |
| Integration tests | Spring Boot Test + Test containers  | Full application context |
| Controller tests | `@WebMvcTest` + MockMvc                               | Slice tests, security disabled via `@ImportAutoConfiguration(exclude = SecurityAutoConfiguration.class)` |


## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## Acknowledgments

- Thanks to [@ValeriyNechayev](https://github.com/ValeriyNechayev) at EPAM Systems for mentorship and architectural/code review guidance.