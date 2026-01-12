#!/bin/bash

# Fast Tasker Notification Service Setup Script
# This script creates the complete microservice structure

set -e  # Exit on error

echo "======================================"
echo "Fast Tasker Notification Service Setup"
echo "======================================"

# Check if we're in the right directory
if [ ! -d "monolith-app" ]; then
    echo "ERROR: Please run this script from the fast-tasker root directory"
    exit 1
fi

echo ""
echo "Creating directory structure..."
mkdir -p fast-tasker-notification/src/main/java/com/fasttasker/notification/{config,domain,repository,service/impl,listener,controller,dto}
mkdir -p fast-tasker-notification/src/main/resources
mkdir -p fast-tasker-notification/src/test/java/com/fasttasker/notification

echo "Creating pom.xml..."
cat > fast-tasker-notification/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.1</version>
        <relativePath/>
    </parent>
    
    <groupId>com.fasttasker</groupId>
    <artifactId>fast-tasker-notification</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>fast-tasker-notification</name>
    <description>Notification Microservice for Fast Tasker</description>
    
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.amqp</groupId>
            <artifactId>spring-rabbit-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.12</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.12</version>
                <executions>
                    <execution>
                        <id>prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
EOF

echo "Creating Dockerfile..."
cat > fast-tasker-notification/Dockerfile << 'EOF'
# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

echo "Creating application.yml..."
cat > fast-tasker-notification/src/main/resources/application.yml << 'EOF'
spring:
  application:
    name: fast-tasker-notification
  
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5433/fast_tasker}
    username: ${DB_USERNAME:back_fast_tasker}
    password: ${DB_PASSWORD:Fasttaskerbackpassword-123666}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:fast_tasker_user}
    password: ${RABBITMQ_PASSWORD:RabbitFastTasker-123}
    virtual-host: ${RABBITMQ_VHOST:/fast-tasker}
    listener:
      simple:
        acknowledge-mode: auto
        retry:
          enabled: true
          initial-interval: 3000
          max-attempts: 3
          max-interval: 10000
          multiplier: 2

server:
  port: ${SERVER_PORT:8081}
  error:
    include-message: always
    include-binding-errors: always

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    root: INFO
    com.fasttasker.notification: DEBUG
    org.springframework.amqp: DEBUG

notification:
  queue:
    name: notification.queue
    exchange: notification.exchange
    routing-key: notification.routing.key
  websocket:
    endpoint: /ws
    topic: /topic/notifications
EOF

echo "Creating .gitignore..."
cat > fast-tasker-notification/.gitignore << 'EOF'
HELP.md
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/
build/
!**/src/main/**/build/
!**/src/test/**/build/

### VS Code ###
.vscode/

### Maven ###
.mvn/
mvnw
mvnw.cmd

### SonarQube ###
.scannerwork/

### Logs ###
*.log

### OS ###
.DS_Store
Thumbs.db
EOF

echo "Creating README.md..."
cat > fast-tasker-notification/README.md << 'EOF'
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
EOF

echo "Creating Main Application class..."
cat > fast-tasker-notification/src/main/java/com/fasttasker/notification/NotificationServiceApplication.java << 'EOF'
package com.fasttasker.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
EOF

echo "Creating Basic Test..."
cat > fast-tasker-notification/src/test/java/com/fasttasker/notification/NotificationServiceApplicationTests.java << 'EOF'
package com.fasttasker.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}
EOF

echo "Updating docker-compose.yml..."
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  db:
    image: postgres:15-alpine
    container_name: ft-postgres
    restart: always
    environment:
      POSTGRES_DB: fast_tasker
      POSTGRES_USER: back_fast_tasker
      POSTGRES_PASSWORD: Fasttaskerbackpassword-123666
    ports:
      - "5433:5432"
    volumes:
      - fasttasker_data:/var/lib/postgresql/data
    networks:
      - fasttasker-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U back_fast_tasker -d fast_tasker"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: ft-rabbitmq
    restart: always
    environment:
      RABBITMQ_DEFAULT_USER: fast_tasker_user
      RABBITMQ_DEFAULT_PASS: RabbitFastTasker-123
      RABBITMQ_DEFAULT_VHOST: /fast-tasker
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - fasttasker-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./monolith-app
    container_name: ft-backend
    restart: always
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    environment:
      DB_URL: jdbc:postgresql://db:5432/fast_tasker
      DB_USERNAME: back_fast_tasker
      DB_PASSWORD: Fasttaskerbackpassword-123666
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: fast_tasker_user
      RABBITMQ_PASSWORD: RabbitFastTasker-123
      RABBITMQ_VHOST: /fast-tasker
    networks:
      - fasttasker-network

  notification-service:
    build: ./fast-tasker-notification
    container_name: ft-notification-service
    restart: always
    ports:
      - "8081:8081"
    depends_on:
      db:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    environment:
      DB_URL: jdbc:postgresql://db:5432/fast_tasker
      DB_USERNAME: back_fast_tasker
      DB_PASSWORD: Fasttaskerbackpassword-123666
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: fast_tasker_user
      RABBITMQ_PASSWORD: RabbitFastTasker-123
      RABBITMQ_VHOST: /fast-tasker
      SERVER_PORT: 8081
    networks:
      - fasttasker-network

  client:
    build: ./client
    container_name: ft-client
    restart: always
    ports:
      - "3000:3000"
    depends_on:
      - backend
      - notification-service
    networks:
      - fasttasker-network

volumes:
  fasttasker_data:
    driver: local
  rabbitmq_data:
    driver: local

networks:
  fasttasker-network:
    driver: bridge
EOF

echo "Creating Jenkinsfile-notification..."
cat > Jenkinsfile-notification << 'EOF'
pipeline {
    agent any
    
    tools { 
        maven 'maven-3'
    }
    
    environment {
        DOCKER_IMAGE = 'fast-tasker-notification'
        DOCKER_TAG = "${BUILD_NUMBER}"
    }
    
    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
                checkout scm
            }
        }
        
        stage('Build & Test') {
            steps {
                dir('fast-tasker-notification') {
                    sh 'rm -rf .scannerwork target'
                    sh 'mvn clean test'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                dir('fast-tasker-notification') {
                    withSonarQubeEnv('sonar-server') {
                        sh 'mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=fast-tasker-notification -Dsonar.ws.timeout=300'
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Build JAR') {
            steps {
                dir('fast-tasker-notification') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                dir('fast-tasker-notification') {
                    script {
                        docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                        docker.build("${DOCKER_IMAGE}:latest")
                    }
                }
            }
        }
    }
    
    post {
        always {
            sh "docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true"
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
EOF

echo ""
echo "======================================"
echo "Setup Complete!"
echo "======================================"
echo ""
echo "Next steps:"
echo "1. Test the build:"
echo "   cd fast-tasker-notification"
echo "   mvn clean package"
echo ""
echo "2. Start RabbitMQ:"
echo "   docker-compose up -d rabbitmq"
echo "   Access UI: http://localhost:15672"
echo "   User: fast_tasker_user"
echo "   Pass: RabbitFastTasker-123"
echo ""
echo "3. Build all services:"
echo "   docker-compose build"
echo ""
echo "4. Run everything:"
echo "   docker-compose up"
echo ""
echo "======================================"
