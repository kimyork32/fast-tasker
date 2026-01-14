# API Gateway

The **API Gateway** is the single entry point for the Fast Tasker backend ecosystem. It is responsible for routing client requests to the appropriate microservices, handling cross-cutting concerns, and simplifying the client-side experience.

## 🚀 Features

*   **Routing:** Intelligently routes incoming HTTP requests to:
    *   `Monolith App` (Core business logic, users, tasks)
    *   `Notification Service` (Real-time notifications)
*   **Load Balancing:** Distributes traffic across service instances (if scaled).
*   **Security:** Acts as a first line of defense (can be extended for rate limiting, IP whitelisting, etc.).
*   **Protocol Translation:** Handles HTTP to WebSocket upgrades where necessary.

## ⚙️ Configuration

The gateway is configured via `application.yml` (or `application.properties`) and environment variables.

### Key Environment Variables

| Variable | Description | Default |
| :--- | :--- | :--- |
| `SERVER_PORT` | Port where the gateway listens. | `8081` |
| `MONOLITH_URI` | Base URL of the Monolith service. | `http://monolith:8082` |
| `NOTIFICATION_URI` | Base URL of the Notification service. | `http://notification-service:8083` |
| `WEBSOCKET_URI` | WebSocket URL for notifications. | `ws://notification-service:8083` |
| `CLIENT_URL` | URL of the frontend client (for CORS). | `http://localhost:3000` |

## 🛠️ Build and Run

### Using Docker (Recommended)
The gateway is part of the main `docker-compose.yml`.
```bash
docker compose up -d api-gateway
```

### Manual Run (Development)
```bash
./mvnw spring-boot:run
```

## 🔗 Routes

| Path | Target Service | Description |
| :--- | :--- | :--- |
| `/api/v1/auth/**` | Monolith | Authentication endpoints. |
| `/api/v1/users/**` | Monolith | User management. |
| `/api/v1/tasks/**` | Monolith | Task management. |
| `/ws/**` | Notification Service | WebSocket connection for real-time updates. |
