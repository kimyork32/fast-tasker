FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src

EXPOSE 8083

CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments='-Dserver.port=8083'"]
