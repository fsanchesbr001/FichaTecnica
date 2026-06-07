# syntax=docker/dockerfile:1

# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080

# Defaults for local container runs. docker-compose can override these.
ENV APP_PROFILE=development
ENV DB_IP=mysql
ENV DB_PORT=3306
ENV DB_NAME=FichaTecnica
ENV DB_USER=root
ENV DB_PWD=root

ENTRYPOINT ["docker-entrypoint.sh"]

