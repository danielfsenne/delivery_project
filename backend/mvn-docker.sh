#!/usr/bin/env bash
# Executa o Maven dentro de um container com JDK 21, sem precisar do Java instalado localmente.
# Uso: ./mvn-docker.sh clean verify
set -euo pipefail

cd "$(dirname "$0")"

docker run --rm \
  -v "$(pwd)":/workspace \
  -v rota-m2:/root/.m2 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal \
  -w /workspace \
  maven:3.9-eclipse-temurin-21 \
  mvn -B "$@"
