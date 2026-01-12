# Common Library

This module is a shared library used across the Fast Tasker microservices ecosystem. It encapsulates common logic, configurations, and data structures to ensure consistency and reduce code duplication.

## 📦 Components

The library provides the following reusable components:

### 1. Configuration (`config`)
*   **Security:**
    *   `JwtService`: Utility for generating and validating JWT tokens.
    *   `JwtAuthenticationFilter`: Filter to intercept requests and validate JWTs.
    *   `UserAuthenticationProvider`: Custom authentication provider logic.
    *   `WebSocketAuthChannelInterceptor`: Interceptor for securing WebSocket connections.
*   **Messaging:**
    *   `RabbitMQConfig`: Common RabbitMQ configuration (exchanges, queues).
*   **Error Handling:**
    *   `GlobalExceptionHandler`: Centralized exception handling for REST controllers.

### 2. Constants (`constant`)
*   `RabbitMQConstants`: Shared constants for RabbitMQ queues, exchanges, and routing keys.

### 3. Exceptions (`exception`)
*   `DomainException`: Base exception for domain-specific errors.
*   `AccountNotFoundException`: Thrown when an account is not found.
*   `EmailAlreadyExistsException`: Thrown when registering with an existing email.

## 🛠️ Usage

To use this library in another microservice, add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.fasttasker</groupId>
    <artifactId>common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## 🚀 Installation (Local Development)

Since this is a local library and not published to a remote repository (like Maven Central), you must install it to your local Maven repository before building dependent services.

```bash
cd common
./mvnw clean install
```
