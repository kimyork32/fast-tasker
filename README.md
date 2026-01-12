# Fast Tasker Backend

![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.1%2B-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-D24939?style=for-the-badge&logo=jenkins&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-DDD%20%2F%20Clean%20Code-blueviolet?style=for-the-badge)

## 📖 Overview

This project is a backend system for a task management application, designed with a microservices architecture.

This project is the result of migrating a monolithic system to a **Microservices** architecture. The ecosystem is designed following **Domain-Driven Design (DDD)** and **Clean Architecture** principles to ensure scalability, maintainability, and decoupling.

## 🏗 Architecture

The system uses a distributed architecture orchestrated via Docker Compose. Each microservice internally follows a **Hexagonal Architecture (Ports and Adapters)** divided into layers:

- **Domain:** Entities and business rules (pure core).
- **Application:** Use cases.
- **Infrastructure:** Persistence, external clients, messaging.
- **Interface (API):** REST Controllers.

### 📦 Services and Components

| Service | Docker Profile | Local Port | Description |
| :--- | :--- | :--- | :--- |
| **API Gateway** | `default` | `8081` | Single entry point for the client. Routes requests to the corresponding microservices. |
| **Monolith App** | `default` | `8082` | Main service managing tasks, users, and conversations. Contains the core business logic. |
| **Notification Service** | `default` | `8083` | Microservice responsible for managing and sending real-time notifications via WebSockets and RabbitMQ. |
| **Common** | `N/A` | `N/A` | Shared library containing common configurations (JWT security, RabbitMQ), base DTOs, utilities, and global exceptions. |
| **Docker Init** | `default` | `N/A` | Scripts for PostgreSQL database initialization. |
| **RabbitMQ** | `default` | `5672` (AMQP), `15672` (UI) | Message broker for asynchronous communication. |
| **PostgreSQL** | `default` | `5433` | Relational database. |
| **Jenkins** | `ops` | `8080` | Automation server for CI/CD. |
| **SonarQube** | `ops` | `9000` | Platform for code quality and security analysis. |
| **Client (Next.js)** | `frontend` | `3000` | Frontend client application. |

## 🔄 CI/CD Pipeline

The project features a continuous integration pipeline defined in `Jenkinsfile` that automates the following stages:

1.  **Setup Code:** Environment cleanup and source code download.
2.  **CI Flow (Develop):** Runs on the `develop` branch or Pull Requests targeting it.
    *   **Parallel Analysis:** Build and static analysis with **SonarQube** for Monolith and Notification Service in parallel.
    *   **Quality Gate:** Waits for SonarQube Quality Gate approval.
3.  **Staging Checks:** Runs on Pull Requests targeting `staging`.
    *   **Security Scan:** Vulnerability analysis (SAST/DAST).
    *   **Performance Tests:** Load and performance testing.

## 🚀 Prerequisites

*   **Java JDK 21**
*   **Docker** & **Docker Compose**
*   **Maven**
*   **Git**

## 🛠️ Installation and Setup

Follow these steps to bring up the complete environment locally.

### 1. Clone the repository
```bash
git clone https://github.com/kimyork32/fast-tasker-backend.git
cd fast-tasker-backend
```

### 2. Environment Variables Configuration
Create a `.env` file in the project root. You can use the `.env.example` file as a reference for the necessary variables.

```bash
cp .env.example .env
```

Ensure you correctly configure credentials and ports before starting the services.

### 3. Running with Docker Compose

The project uses **Docker Compose Profiles** to manage service groups.

#### 🔹 Start Backend (Core Services)
Starts Database, RabbitMQ, API Gateway, Monolith, and Notification Service.
```bash
docker compose up -d
```
*If you only want to start the infrastructure (DB and RabbitMQ):*
```bash
docker compose up -d db rabbitmq
```

#### 🔹 Start Operations Tools (Jenkins, SonarQube)
To start the CI/CD and code analysis environment.
```bash
docker compose --profile ops up -d
```

#### 🔹 Start Frontend Client
To start the client application (Next.js).
```bash
docker compose --profile frontend up -d
```

#### 🔹 Stop All
To stop and remove containers, networks, and volumes for all profiles.
```bash
docker compose down -v
```

### 4. Service Access

| Service | Local URL | Credentials (Default) |
| :--- | :--- | :--- |
| **API Gateway** | `http://localhost:8081` | - |
| **RabbitMQ UI** | `http://localhost:15672` | `guest` / `guest` |
| **Jenkins** | `http://localhost:8080` | Configure on first launch |
| **SonarQube** | `http://localhost:9000` | `admin` / `admin` |
| **Client** | `http://localhost:3000` | - |
