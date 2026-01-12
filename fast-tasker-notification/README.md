# Fast Tasker - Notification Microservice

## Description
Microservice responsible for handling all notification-related operations in the Fast Tasker platform. Uses RabbitMQ for asynchronous event processing and WebSockets for real-time notifications.

## Architecture
- **Pattern**: Strangler Fig Migration
- **Message Broker**: RabbitMQ
- **Database**: PostgreSQL (shared with monolith)
- **Real-time**: WebSockets (STOMP)

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### Run Locally
```bash
mvn clean package
java -jar target/fast-tasker-notification-0.0.1-SNAPSHOT.jar
```

### Run with Docker
```bash
docker-compose up notification-service
```

## Endpoints

### REST API
- `GET /api/notifications` - Get all notifications
- `GET /api/notifications/{id}` - Get specific notification
- `PATCH /api/notifications/{id}/read` - Mark as read
- `GET /actuator/health` - Health check

### WebSocket
- **Connect**: `ws://localhost:8081/ws`
- **Subscribe**: `/topic/notifications/{userId}`

## RabbitMQ Configuration
- **Exchange**: `notification.exchange`
- **Queue**: `notification.queue`
- **Routing Key**: `notification.routing.key`

## Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8081 | Application port |
| `DB_URL` | jdbc:postgresql://localhost:5433/fast_tasker | Database URL |
| `RABBITMQ_HOST` | localhost | RabbitMQ host |
| `RABBITMQ_PORT` | 5672 | RabbitMQ port |

## Monitoring
- Health: http://localhost:8081/actuator/health
- RabbitMQ UI: http://localhost:15672
