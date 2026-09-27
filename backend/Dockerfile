# syntax=docker/dockerfile:1.7
# Dockerfile único para todos os serviços. Uso:
#   docker build --build-arg MODULE=auth-service -t rota/auth-service backend/

FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl ${MODULE} -am package -DskipTests \
    && cp ${MODULE}/target/${MODULE}-*.jar /app.jar

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S rota && adduser -S rota -G rota
USER rota
WORKDIR /app
COPY --from=build /app.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
