FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN mvn -q -DskipTests dependency:go-offline

COPY src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S finance && adduser -S finance -G finance
COPY --from=build /workspace/target/*.jar app.jar
USER finance

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
