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
- [Monitoring and Observability](#monitoring-and-observability)
- [Development](#development)
- [Testing](#testing)
- [License](#license)

## Overview

The Gym CRM System is an application designed to manage gym operations including trainee and trainer profiles, training sessions, and user authentication. The system implements industry best practices including onion, clean, hexagonal architecture patterns, and security measures.

## Architecture

The application follows **Onion Architecture** (with elements of Clean and Hexagonal Architecture) principles with separation of concerns:

```
┌─────────────────────────────────────────┐
│         Interfaces Layer (Web)          │
│     Controllers, DTOs, REST APIs        │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│        Application Layer                │
│   Facades, Services, Domain Validation  │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Domain Layer                   │
│   Domain Entities, Ports                │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│      Infrastructure Layer               │
│ Persistence, Security, External APIs    │
└─────────────────────────────────────────┘
```

### Layer Responsibilities

- **Domain Layer**: Core business entities and repository interfaces (ports)
- **Application Layer**: Business logic orchestration, and application services
- **Infrastructure Layer**: Technical implementations (JPA, Security, Logging, Monitoring)
- **Interfaces Layer**: REST API controllers and DTOs for external communication

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
- Filter trainings by date, trainer/trainee, and type
- Duration tracking
- Support for defining training types (using enums)

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
    - Transaction ID tracking
    - MDC (Mapped Diagnostic Context) support
    - Request/Response logging
    - Comprehensive error logging

### Additional Features

- CORS configuration for cross-origin requests
- OpenAPI/Swagger documentation
- Database bootstrapping with initial json data
- Comprehensive input validation
- Global exception handling
- RESTful API design

## Technology Stack

### Core Framework
- **Spring Boot 3.5.8** - Application framework
- **Java 17+** - Programming language

### Persistence
- **Spring Data JPA** - Data access layer
- **Hibernate** - ORM implementation
- **H2/PostgreSQL** - Database (configurable)

### Security
- **Spring Security 6.x** - Security framework
- **JWT (JSON Web Tokens)** - Authentication
- **BCrypt** - Password encryption

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

The application uses Spring profiles for environment-specific configurations:

#### Application Properties

Create `application-{profile}.properties` or use the configured local profile `application-local.properties`:

#### Example properties
```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=${POSTGRESQL_URL}
spring.datasource.username=${POSTGRESQL_USERNAME}
spring.datasource.password=${POSTGRESQL_PASSWORD}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# JWT Configuration
security.jwt.secret=your-secret-key-min-256-bits
security.jwt.lifetime=24h

# Login Security Bruteforce attack protection
security.login.max-attempts=3
security.login.penalty=5m

# CORS Configuration
security.cors.allowed-origins=http://localhost:3000

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Bootstrap Data for json files (local profile only)
storage.init.users=/users.json
storage.init.trainees=/trainees.json
storage.init.trainers=/trainers.json
storage.init.trainings=/trainings.json
storage.init.trainingTypes=/trainingTypes.json
```

#### Security Configuration

**Important**: Change the JWT secret in production:

```properties
security.jwt.secret=
```

Generate a secure secret using:
```bash
openssl rand -base64 32
```

#### Profiles

- **local**: Development profile with Postgres database(docker container) and bootstrap data with json files
- **prod**: Production profile with external database

Activate a profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Running the Application(with local profile)

#### Using Maven

```bash
docker compose up -d 
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

#### Using JAR

```bash
./mvnw clean package
docker compose up -d
export SPRING_PROFILES_ACTIVE=local
java -jar target/gym-crm-1.0-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## API Documentation

### Swagger UI

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI Specification

Raw OpenAPI specification available at:
```
http://localhost:8080/v3/api-docs
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

#### Training Types

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/training-types` | Get all training types | Yes |

### Example Requests

#### Register Trainee

```bash
curl -X POST http://localhost:8080/api/trainees \
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
curl -X POST http://localhost:8080/api/auth/login \
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

#### Get Trainee Profile (Authenticated)

```bash
curl -X GET http://localhost:8080/api/trainees/John.Doe \
  -H "Authorization: Bearer "
```

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

### Brute Force Protection

- Maximum failed attempts: 3 (configurable)
- Lockout duration: 5 minutes (configurable)
- Automatic cleanup of expired locks

### Password Requirements

- Minimum length: 10 characters
- Maximum length: 100 characters
- Cannot be only whitespace
- Stored using BCrypt hashing

## Database

### Entity Relationship Diagram

```
┌─────────────┐       ┌──────────────┐       ┌─────────────┐
│   UserDAO   │       │  TraineeDAO  │       │ TrainerDAO  │
├─────────────┤       ├──────────────┤       ├─────────────┤
│ userId (PK) │──────>│ traineeId    │       │ trainerId   │
│ username    │       │ userId (FK)  │       │ userId (FK) │
│ password    │       │ dob          │       │ training... │
│ role        │       │ address      │       │   Type (FK) │
│ active      │       └──────────────┘       └─────────────┘
└─────────────┘              │                       │
                             └───────────┬───────────┘
                                         │
                                         │ Many-to-Many
                                         │
                                  ┌──────▼───────┐
                                  │  TrainingDAO │
                                  ├──────────────┤
                                  │ trainingId   │
                                  │ traineeId    │
                                  │ trainerId    │
                                  │ typeId (FK)  │
                                  │ name         │
                                  │ date         │
                                  │ duration     │
                                  └──────────────┘
```

### Database Initialization

In the `local` profile, the database is automatically populated from JSON files:

- `users.json`: User accounts
- `trainees.json`: Trainee profiles
- `trainers.json`: Trainer profiles
- `trainings.json`: Training sessions
- `trainingTypes.json`: Training type definitions

## Monitoring and Observability

The application includes comprehensive monitoring and observability features using Spring Boot Actuator, Micrometer, Prometheus, and Grafana.

### Health Checks

Access health status:
```bash
curl http://localhost:8080/actuator/health
```

Available health indicators:
- **database**: Database connectivity
- **training-types**: Training types availability
- **bootstrap**: Bootstrap data loading status (local profile)

### Metrics with Prometheus & Grafana

#### Prerequisites

- Docker and Docker Compose installed
- Application running on `http://localhost:8080`

#### Quick Start

1. **Start Prometheus and Grafana**

   ```bash
   cd prometheus-grafana
   docker-compose up -d
   ```

2. **Verify Services**

  - **Prometheus**: http://localhost:9090
  - **Grafana**: http://localhost:3000
  - **Application Metrics**: http://localhost:8080/actuator/prometheus

3. **Access Grafana Dashboard**

  - URL: http://localhost:3000
  - Username: `admin`
  - Password: `grafana`

4. **View Metrics in Prometheus**

   Navigate to http://localhost:9090/graph and try these queries:

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

The application exposes these business metrics:

- `trainee_registered_total`: Total trainee registrations
- `trainer_registered_total`: Total trainer registrations
- `user_login_attempts_total`: Total login attempts
- `user_login_failed_total`: Failed login attempts

Access all metrics:
```bash
curl http://localhost:8080/actuator/metrics
```

Access specific metric:
```bash
curl http://localhost:8080/actuator/metrics/trainee_registered_total
```

#### Creating Grafana Dashboards

1. **Login to Grafana** (http://localhost:3000)

2. **Create New Dashboard**
  - Click "+" → "Dashboard"
  - Add new panel

3. **Sample Panels**

   **Login Attempts Panel:**
   ```promql
   rate(user_login_attempts_total[5m])
   ```

   **Failed Login Rate Panel:**
   ```promql
   rate(user_login_failed_total[5m])
   ```

   **Registration Metrics Panel:**
   ```promql
   trainee_registered_total + trainer_registered_total
   ```

   **JVM Memory Usage Panel:**
   ```promql
   sum(jvm_memory_used_bytes) by (area)
   ```

   **HTTP Request Rate Panel:**
   ```promql
   rate(http_server_requests_seconds_count[5m])
   ```

4. **Import Spring Boot Dashboard**

  - Go to Dashboards → Import
  - Use Dashboard ID: `4701` (JVM Micrometer)
  - Or ID: `11378` (Spring Boot Statistics)
  - Select Prometheus as data source

#### Docker Compose Configuration

The monitoring stack includes:

```yaml
services:
  prometheus:
    image: prom/prometheus
    ports:
      - 9090:9090
    volumes:
      - ./prometheus:/etc/prometheus
    # Scrapes metrics from Spring Boot app every 15s
    
  grafana:
    image: grafana/grafana
    ports:
      - 3000:3000
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=grafana
    # Prometheus pre-configured as datasource
```

#### Prometheus Configuration

Located in `prometheus-grafana/prometheus/prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'spring-boot-application'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['host.docker.internal:8080']
```

#### Stopping Monitoring Stack

```bash
cd prometheus-grafana
docker-compose down
```

To remove volumes (persistent data):
```bash
docker-compose down -v
```

#### Troubleshooting

**Prometheus can't scrape metrics:**
- Verify the application is running and accessible on port `8080`
- Ensure actuator endpoints are exposed in `application.properties`
- On Windows / macOS, verify `host.docker.internal` resolves correctly
- On Linux, use `172.17.0.1` instead of `host.docker.internal`
- On SELinux-enabled systems, append `:z` to volume mounts to allow container access

**Grafana shows "No Data":**
- Verify Prometheus is scraping successfully (check Targets in Prometheus UI)
- Check data source connection in Grafana Configuration
- Ensure time range in Grafana includes recent data

**Application metrics not appearing:**
- Confirm Micrometer dependencies are included
- Check that custom metrics are being registered
- Verify actuator prometheus endpoint: `curl http://localhost:8080/actuator/prometheus`

### Logging

All logs include transaction IDs for request tracing:

```
INFO [TXN-1234567890-a1b2c3d4] Authentication attempt for user: john.doe
```

Log levels can be configured per package:
```properties
logging.level.com.epam=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Monitoring in Production

For production deployments, consider:

1. **Persistent Storage**: Configure Prometheus with persistent volumes
2. **Alerting**: Set up Prometheus Alertmanager for critical metrics
3. **Retention**: Configure Prometheus data retention policy
4. **Grafana**: Set up proper authentication and SSL
5. **Backup**: Regular backups of Grafana dashboards and Prometheus data
6. **Scaling**: Consider using Prometheus federation for large deployments

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

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## Acknowledgments

- Thanks to [@ValeriyNechayev](https://github.com/ValeriyNechayev) at EPAM Systems for mentorship and architectural/code review guidance.
---
