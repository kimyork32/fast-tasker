FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# copy maven configuration
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# allowing exec permissions to the maven wrapper
RUN chmod +x mvnw

# download dependencies (caching)
RUN ./mvnw dependency:go-offline

COPY src ./src
EXPOSE 8081
# compile and run automatically
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments='-Dserver.port=8081'"]
