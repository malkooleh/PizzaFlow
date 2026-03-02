# Build stage -- context must be the repo root
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml ./pom.xml
COPY common-libs/common-dto/pom.xml ./common-libs/common-dto/pom.xml
COPY common-libs/common-security/pom.xml ./common-libs/common-security/pom.xml
COPY common-libs/common-observability/pom.xml ./common-libs/common-observability/pom.xml
COPY services/booking-service/pom.xml ./services/booking-service/pom.xml

RUN apk add --no-cache maven && \
    mvn install -N --no-transfer-progress && \
    mvn install -f common-libs/common-dto/pom.xml -N --no-transfer-progress && \
    mvn install -f common-libs/common-security/pom.xml -N --no-transfer-progress && \
    mvn install -f common-libs/common-observability/pom.xml -N --no-transfer-progress

RUN mvn dependency:go-offline -f services/booking-service/pom.xml --no-transfer-progress || true

COPY common-libs/common-dto/src ./common-libs/common-dto/src
COPY common-libs/common-security/src ./common-libs/common-security/src
COPY common-libs/common-observability/src ./common-libs/common-observability/src
COPY services/booking-service/src ./services/booking-service/src
RUN mvn install -f common-libs/common-dto/pom.xml --no-transfer-progress && \
    mvn install -f common-libs/common-security/pom.xml --no-transfer-progress && \
    mvn install -f common-libs/common-observability/pom.xml --no-transfer-progress && \
    mvn package -f services/booking-service/pom.xml -DskipTests --no-transfer-progress

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S pizzaflow && adduser -S pizzaflow -G pizzaflow
USER pizzaflow

COPY --from=builder /app/services/booking-service/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+EnableDynamicAgentLoading"

EXPOSE 8086

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
