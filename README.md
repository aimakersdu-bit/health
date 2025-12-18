# Cloud Native Health Check Demo

This project demonstrates a cloud-native approach to health checks using Java (Spring Boot 3), satisfying requirements to check:
1.  **Database Connection** (MySQL)
2.  **Middleware Connection** (Redis)
3.  **Service Registration** (Simulated)
4.  **Business Logic Availability** (Custom Indicator)

## Prerequisites

*   Java 17+
*   Docker & Docker Compose

## Quick Start

1.  **Start Infrastructure**
    Launch MySQL and Redis using Docker Compose:
    ```bash
    docker-compose up -d
    ```

2.  **Run Application**
    ```bash
    mvn spring-boot:run
    ```

3.  **Check Health**
    Access the Actuator Health endpoint:
    ```bash
    curl http://localhost:8080/actuator/health
    ```

    You will see a detailed JSON response including the status of Redis, MySQL, Custom Registration, and Business Logic.

## Control Endpoints (Simulation)

You can toggle the health status of components dynamically to observe how the health check responds.

*   **Toggle Business Health (UP/DOWN)**
    ```bash
    # Set DOWN
    curl -X POST "http://localhost:8080/control/business/toggle?up=false"

    # Check Health again
    curl http://localhost:8080/actuator/health
    ```

*   **Toggle Registration Status**
    ```bash
    # Set NOT_REGISTERED
    curl -X POST "http://localhost:8080/control/registration/toggle?registered=false"
    ```

## Key Components

*   `RegistrationHealthIndicator.java`: Custom indicator simulating a check against a Service Registry (like Nacos/Eureka).
*   `BusinessHealthIndicator.java`: Custom indicator checking a specific business flag (e.g., maintenance mode).
*   `application.yml`: Configures Actuator to expose all details and enables Kubernetes Liveness/Readiness probe support.
