# syntax=docker/dockerfile:1.7
# Dockerfile único para todos os serviços. Uso:
#   docker build --build-arg MODULE=auth-service -t rota/auth-service backend/

# 1) Compila só o módulo pedido e o que ele usa (common). O repositório Maven fica num
#    cache do BuildKit, então as dependências não são baixadas de novo a cada build.
FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl ${MODULE} -am package -DskipTests \
    && cp ${MODULE}/target/${MODULE}-*.jar /app.jar

# 2) Separa o jar em camadas: dependências (mudam pouco) e o código da aplicação (muda sempre).
FROM eclipse-temurin:21-jre-alpine AS layers
WORKDIR /layers
COPY --from=build /app.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted \
    && mkdir -p extracted/snapshot-dependencies

# 3) Imagem final só com a JRE, usuário sem privilégios e as camadas em ordem de estabilidade:
#    uma mudança no código troca só a última camada, e o push/pull das imagens fica pequeno.
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S rota && adduser -S rota -G rota
USER rota
WORKDIR /app
COPY --from=layers /layers/extracted/dependencies/ ./
COPY --from=layers /layers/extracted/spring-boot-loader/ ./
COPY --from=layers /layers/extracted/snapshot-dependencies/ ./
COPY --from=layers /layers/extracted/application/ ./
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
