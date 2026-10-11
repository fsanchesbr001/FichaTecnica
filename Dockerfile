# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17-alpine AS deps
WORKDIR /workspace

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests dependency:go-offline

FROM deps AS build

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests clean package

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /workspace/target/*.jar /app/app.jar
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod 755 /usr/local/bin/docker-entrypoint.sh && chown -R appuser:appgroup /app

EXPOSE 8080

ENV APP_PROFILE=production \
    DB_IP=mysql \
    DB_PORT=3306 \
    DB_NAME=FichaTecnica \
    DB_USER=fichatecnica \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"

USER appuser
ENTRYPOINT ["docker-entrypoint.sh"]
