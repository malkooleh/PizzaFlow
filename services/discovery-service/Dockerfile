# Build stage -- context must be the repo root
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Step 1: Copy only the POMs needed -- enables Docker layer caching
COPY pom.xml ./pom.xml
COPY common-libs/common-dto/pom.xml ./common-libs/common-dto/pom.xml
COPY common-libs/common-security/pom.xml ./common-libs/common-security/pom.xml
COPY common-libs/common-observability/pom.xml ./common-libs/common-observability/pom.xml
COPY services/discovery-service/pom.xml ./services/discovery-service/pom.xml

# Step 2: Install Maven, install parent POM (-N = non-recursive) and common-libs
RUN apk add --no-cache maven && \
    mvn install -N --no-transfer-progress && \
    mvn install -f common-libs/common-dto/pom.xml -N --no-transfer-progress && \
    mvn install -f common-libs/common-security/pom.xml -N --no-transfer-progress && \
    mvn install -f common-libs/common-observability/pom.xml -N --no-transfer-progress

# Step 3: Pre-download service dependencies (cached unless service POM changes)
RUN mvn dependency:go-offline -f services/discovery-service/pom.xml --no-transfer-progress || true

# Step 4: Copy sources and build
COPY common-libs/common-dto/src ./common-libs/common-dto/src
COPY common-libs/common-security/src ./common-libs/common-security/src
COPY common-libs/common-observability/src ./common-libs/common-observability/src
COPY services/discovery-service/src ./services/discovery-service/src
RUN mvn install -f common-libs/common-dto/pom.xml --no-transfer-progress && \
    mvn install -f common-libs/common-security/pom.xml --no-transfer-progress && \
    mvn install -f common-libs/common-observability/pom.xml --no-transfer-progress && \
    mvn package -f services/discovery-service/pom.xml -Dmaven.test.skip=true --no-transfer-progress

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S pizzaflow && adduser -S pizzaflow -G pizzaflow && mkdir -p /tmp/pizzaflow-logs && chown -R pizzaflow:pizzaflow /app /tmp/pizzaflow-logs

COPY --from=builder --chown=pizzaflow:pizzaflow /app/services/discovery-service/target/*.jar app.jar

ENV LOG_PATH="/tmp/pizzaflow-logs"
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseZGC"

USER pizzaflow

EXPOSE 8761

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
