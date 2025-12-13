# Gym CRM System

A Customer Relationship Management (CRM) system designed for gyms and fitness centers to manage trainees, trainers, and training sessions.

## Features

- **Trainee Management**: Register and manage gym members
- **Trainer Management**: Handle trainer profiles and specializations
- **Training Sessions**: Schedule and track training sessions
- **User Authentication**: Secure access control system
- **Reporting**: Generate reports on training activities

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.5.8
- **Database**: PostgreSQL 16
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Containerization**: Docker

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- Docker and Docker Compose
- PostgreSQL 16 (or use the provided Docker setup)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/gym-crm.git
   cd gym-crm
   ```

2. Start the database using Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. Build the application:
   ```bash
   ./mvnw clean install
   ```

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The application will be available at `http://localhost:8080`

## Testing

Run the test suite with:
```bash
./mvnw test
```

## API Documentation

Once the application is running, you can access the API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Architecture

The application follows an onion architecture with the following layers:

### Interface Adapters Layer
- **Facade**: `com.epam.interface_adapters.facade.GymFacade`
  - Coordinates application services
  - Provides unified API for external access

### Application Layer
- **Services**: `com.epam.application.service`
  - Implements business workflows and use cases
  - Orchestrates domain objects
  - Includes services for Trainee, Trainer, and Training management

### Domain Layer (Core)
- **Models**: `com.epam.domain.model`
  - Pure business entities (User, Trainee, Trainer, Training, TrainingType)

### Infrastructure Layer
- **Persistence**: Handles data storage and retrieval
- **Configuration**: Application and security configurations
- **Initialization**: Data loaders and initial setup

```