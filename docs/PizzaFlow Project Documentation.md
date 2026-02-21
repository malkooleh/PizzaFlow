# Project Documentation: PizzaFlow

## 1. High-Level Overview

**PizzaFlow** is a cloud-based ecosystem designed for managing a network of pizzerias. The project was developed to demonstrate skills in building high-load Java 21 systems and implementing modern DevOps practices.

### Business Goals:

* **Scalability:** The ability to add new locations (restaurants) without code modifications.
* **Order Flexibility:** Support for "asap," "scheduled," "table service," and "delivery" orders.
* **Kitchen Efficiency:** Minimizing wait times through intelligent queue distribution.

### Proposed Folder Structure:

```
pizza-flow/
├── .github/workflows/       <-- CI/CD pipelines (GitHub Actions)
├── infrastructure/          <-- Docker Compose, K8s manifests, Terraform, Keycloak/Spring Security + JWT configs
├── common-libs/             <-- Shared modules (Shared Security, Error Handling)
│   ├── common-dto/
│   └── common-security/
├── services/                <-- Core microservices
│   ├── api-gateway/         <-- Spring Cloud Gateway
│   ├── config-service/      <-- Spring Cloud Config
│   ├── catalog-service/     <-- Java 21, Spring Boot 3.4
│   ├── order-service/       <-- Java 21, Spring Boot 3.4
│   └── ...
├── frontend/                <-- React (Vite) project
├── pom.xml                  <-- Root Maven POM
└── README.md
```


---

## 2. System Landscape

The system is built on an **Event-Driven Architecture (EDA)** with elements of **Domain-Driven Design (DDD)**.

### Overall Interaction Schema

1. **Client Layer:** Web (React), Mobile (future).
2. **Gateway Layer:** Routing, Rate Limiting, Centralized Auth.
3. **Service Layer:** Isolated business domains.
4. **Data Layer:** Polyglot Persistence (different databases for different needs).
5. **Event Layer:** Asynchronous bus for inter-service communication.


### Core Components:

1. **Infrastructure Suite:** Service Discovery (Eureka), Config Server, API Gateway.
2. **Product Domain:** Catalog Service, Inventory Service.
3. **Sales Domain:** Order Service, Payment Service, Booking Service.
4. **Operational Domain:** Kitchen Service, Delivery Service.
5. **Cross-cutting:** Notification Service, Audit Log Service.

#### Responsibility and Tech Stack Table

| **Service**            | **Primary Purpose**                                       | **Technology Stack**                                                                                 | **Database**           |
| --------------------- | ------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ | ------------------------ |
| **API Gateway**       | Entry point, security, API aggregation.                         | Spring Cloud Gateway                                                                                   | Redis (Rate limiting)    |
| **Identity/Auth Service**  | Auth/Authz, user management.                          | Keycloak (or Spring Auth Server)                                                                      | PostgreSQL               |
| **Catalog Service**   | Menu, ingredients, and pricing management.                      | Spring Boot 3.4, gRPC                                                                                  | MongoDB (JSON flexibility) |
| **Order Service**     | Order lifecycle, calculation.                         | Spring Boot 3.4, Kafka Producer                                                                        | PostgreSQL               |
| **Kitchen Service**   | Kitchen Display queue management.             | Spring Boot, WebSockets                                                                                | Redis                    |
| **Booking Service**   | Table booking, load management.                   | Spring Boot, Virtual Threads                                                                           | PostgreSQL               |
| **Delivery Service**  | Logistics, couriers, delivery time calculation.                 | Spring Boot                                                                                            | PostgreSQL + PostGIS     |
| **Notification**      | Notifications (Email, Push, SMS).                                | Spring Boot, Kafka Consumer                                                                            | -                        |
| **Inventory Service** | **Warehouse:** Deducts grams of flour, cheese, and box counts. | Implements the **Outbox** pattern to ensure that deduction messages reliably reach Kafka. | PostgreSQL               |


---

## 3. Technical Stack

| **Layer** | **Technology** | **Justification** |
| --- | --- | --- |
| **Runtime** | Java 21 | Virtual Threads (Project Loom) for high-traffic processing. |
| **Framework** | Spring Boot 3.4.x | Latest stable features and enhanced GraalVM support. |
| **Inter-service** | gRPC / Kafka | gRPC for low-latency internal requests; Kafka for asynchronous communication. |
| **Security** | Keycloak (OIDC) | Robust Identity Provider for RBAC/ABAC. |
| **DevOps** | Docker, K8s, Helm | Industry standard for containerization and orchestration. |
| **CI/CD** | GitHub Actions | Automated build and testing workflows utilizing Testcontainers. |

---

## 4. Detailed Architecture Patterns & Decisions

### 4.1 Microservices Communication Patterns

#### Synchronous Communication (gRPC)
**Use Cases:**
- **Catalog Service → Inventory Service:** Checking ingredient availability during menu browsing (read-heavy, low-latency required).
- **Order Service → Payment Service:** Payment processing confirmation (transactional, requires immediate response).
- **Kitchen Service → Catalog Service:** Retrieving recipe details for order preparation.

**Implementation Details:**
```
Technology: gRPC with Protobuf
Load Balancing: Client-side load balancing via Spring Cloud LoadBalancer
Resilience: Circuit Breaker (Resilience4j), Retry with exponential backoff
Timeout Strategy: 3 seconds for critical paths, 5 seconds for non-critical
Service Mesh: Istio/Linkerd for advanced routing and observability (Phase 3)
```

#### Asynchronous Communication (Kafka)
**Event Topics Structure:**

| **Topic Name** | **Producer** | **Consumer(s)** | **Event Type** | **Retention** |
| --- | --- | --- | --- | --- |
| `order.created` | Order Service | Kitchen, Inventory, Notification | OrderCreatedEvent | 7 days |
| `order.confirmed` | Payment Service | Kitchen, Delivery, Audit | OrderConfirmedEvent | 7 days |
| `order.preparing` | Kitchen Service | Notification, Delivery | OrderPreparingEvent | 3 days |
| `order.ready` | Kitchen Service | Delivery, Notification | OrderReadyEvent | 3 days |
| `order.delivered` | Delivery Service | Order, Notification, Audit | OrderDeliveredEvent | 30 days |
| `inventory.depleted` | Inventory Service | Notification, Catalog | InventoryDepletedEvent | 1 day |
| `booking.confirmed` | Booking Service | Notification, Kitchen | BookingConfirmedEvent | 7 days |

**Kafka Configuration:**
```
Partitioning Strategy: By restaurant_id for locality
Replication Factor: 3 (production), 1 (dev)
Consumer Groups: One per service instance pool
Idempotency: Enabled on producers, transactional.id configured
Error Handling: Dead Letter Queue (DLQ) for failed messages
Monitoring: Kafka lag monitoring via Prometheus
```

### 4.2 Data Consistency Patterns

#### Saga Pattern Implementation
**Order Processing Saga (Choreography-based):**

```
1. Order Service: Create Order (status=PENDING)
   ↓ publishes: order.created
2. Inventory Service: Reserve Ingredients
   ↓ publishes: inventory.reserved OR inventory.reservation.failed
3. Payment Service: Process Payment
   ↓ publishes: payment.completed OR payment.failed
4. Kitchen Service: Queue Order
   ↓ publishes: order.queued
   
Compensation Flow (if payment fails):
- Inventory Service listens to payment.failed
- Releases reserved ingredients
- Order Service updates status to CANCELLED
```

**Booking + Pre-Order Saga (Hybrid Order):**
```
1. Booking Service: Reserve Table (status=RESERVED)
   ↓ publishes: booking.reserved
2. Order Service: Create Pre-Order (linked to booking_id)
   ↓ publishes: preorder.created
3. Inventory Service: Reserve Ingredients (scheduled)
   ↓ publishes: inventory.scheduled.reserved
4. At scheduled time - 30 mins:
   Kitchen Service: Start preparation
   ↓ publishes: order.preparing

Compensation: If customer cancels booking > 2 hours before:
- Full refund via payment.refund.initiated
- Inventory releases scheduled reservation
```

#### Outbox Pattern (Transactional Outbox)
**Implementation in Inventory Service:**

```java
@Entity
@Table(name = "inventory_outbox")
public class OutboxEvent {
    @Id
    private UUID id;
    private String aggregateId;      // ingredient_id or order_id
    private String eventType;         // "INGREDIENT_RESERVED"
    private String payload;           // JSON serialized event
    private LocalDateTime createdAt;
    private boolean published;
}

// Transaction: Both inventory deduction and outbox entry happen atomically
@Transactional
public void reserveIngredients(ReservationCommand cmd) {
    inventoryRepository.deduct(cmd.getIngredientId(), cmd.getQuantity());
    
    OutboxEvent event = new OutboxEvent(
        UUID.randomUUID(),
        cmd.getIngredientId(),
        "INGREDIENT_RESERVED",
        serializeEvent(cmd),
        LocalDateTime.now(),
        false
    );
    outboxRepository.save(event);
}

// Background job: Relay outbox events to Kafka
@Scheduled(fixedDelay = 5000)
public void publishOutboxEvents() {
    List<OutboxEvent> unpublished = outboxRepository.findUnpublished();
    unpublished.forEach(event -> {
        kafkaTemplate.send(event.getEventType(), event.getPayload());
        event.setPublished(true);
        outboxRepository.save(event);
    });
}
```

### 4.3 Database Design Strategy

#### Database per Service Pattern

| **Service** | **Database** | **Schema Highlights** |
| --- | --- | --- |
| **Order Service** | PostgreSQL | `orders`, `order_items`, `order_saga_state` (for tracking) |
| **Catalog Service** | MongoDB | Collections: `menu_items`, `recipes`, `modifiers` (flexible schema) |
| **Inventory Service** | PostgreSQL | `ingredients`, `stock_levels`, `inventory_outbox`, `reservations` |
| **Booking Service** | PostgreSQL | `bookings`, `tables`, `restaurant_capacity`, `availability_slots` |
| **Kitchen Service** | Redis + PostgreSQL | Redis: Active queues; PostgreSQL: Historical records |
| **Delivery Service** | PostgreSQL + PostGIS | `delivery_orders`, `couriers`, `routes` (geospatial data) |
| **Payment Service** | PostgreSQL | `transactions`, `payment_methods`, `refunds` (ACID critical) |
| **Notification Service** | MongoDB | `notification_logs`, `templates` (high-write, flexible) |

#### Read Models (CQRS Pattern)
**Order History View (Read Model):**
```
Materialized View: Combines data from Order, Payment, Delivery
Sync Mechanism: Kafka consumers update denormalized tables
Technology: PostgreSQL materialized views + periodic refresh
Access Pattern: High-read, low-write (customer order history)
```

### 4.4 Security Architecture

#### Authentication & Authorization Flow

**OAuth2/OIDC with Keycloak:**

```
1. User authenticates via Keycloak (Username/Password, Google, Facebook)
2. Keycloak issues JWT with:
   - sub: user_id
   - roles: [CUSTOMER, ADMIN, KITCHEN_STAFF, COURIER]
   - custom claims: restaurant_id (for staff)
3. API Gateway validates JWT signature (RSA public key from Keycloak)
4. Gateway forwards JWT to downstream services
5. Services extract roles/claims for authorization
```

**Role-Based Access Control (RBAC):**

| **Role** | **Permissions** |
| --- | --- |
| **CUSTOMER** | Browse menu, place orders, view own orders, create bookings |
| **KITCHEN_STAFF** | View kitchen queue, update order status, mark orders complete |
| **COURIER** | View assigned deliveries, update delivery status, navigate routes |
| **RESTAURANT_MANAGER** | Manage menu, view analytics, configure tables, manage staff |
| **SYSTEM_ADMIN** | Full access, service configuration, user management |

**Service-to-Service Security:**
```
Mechanism: mTLS (Mutual TLS) within service mesh
Certificate Management: cert-manager (Kubernetes)
API Keys: For external integrations (payment gateways, SMS providers)
Secret Management: Kubernetes Secrets (Phase 1), HashiCorp Vault (Phase 3)
```

### 4.5 Resilience Patterns

#### Circuit Breaker Configuration (Resilience4j)

```yaml
resilience4j.circuitbreaker:
  instances:
    inventoryService:
      registerHealthIndicator: true
      slidingWindowSize: 10
      minimumNumberOfCalls: 5
      failureRateThreshold: 50
      waitDurationInOpenState: 10000  # 10 seconds
      permittedNumberOfCallsInHalfOpenState: 3
      automaticTransitionFromOpenToHalfOpenEnabled: true
    paymentService:
      slidingWindowSize: 20
      failureRateThreshold: 30
      waitDurationInOpenState: 30000  # 30 seconds
```

#### Retry Strategy
```java
@Retry(name = "catalogService", fallbackMethod = "getCatalogFallback")
public MenuResponse getMenu(String restaurantId) {
    return catalogClient.fetchMenu(restaurantId);
}

// Exponential backoff: 1s, 2s, 4s
resilience4j.retry:
  instances:
    catalogService:
      maxAttempts: 3
      waitDuration: 1000
      enableExponentialBackoff: true
      exponentialBackoffMultiplier: 2
```

#### Bulkhead Pattern
```
Purpose: Isolate thread pools for different external calls
Implementation: Semaphore-based bulkheads for Virtual Threads
Configuration: Max concurrent calls = 10 for payment, 20 for catalog
```

### 4.6 Observability & Monitoring

#### Three Pillars Implementation

**1. Metrics (Prometheus + Micrometer)**
```
Custom Metrics to Track:
- Business: orders_created_total, revenue_by_restaurant, avg_delivery_time
- Technical: jvm_threads_virtual_count, kafka_consumer_lag, grpc_server_latency
- Infrastructure: db_connection_pool_usage, redis_cache_hit_ratio

Scraping: Prometheus pulls metrics from /actuator/prometheus every 15s
Alerting: AlertManager for SLA violations (latency > 1s, error rate > 1%)
```

**2. Logging (ELK Stack)**
```
Log Aggregation: Filebeat → Logstash → Elasticsearch → Kibana
Structured Logging: JSON format with trace_id, span_id, user_id, restaurant_id
Retention: 7 days hot storage, 30 days warm, 90 days cold (AWS S3/Glacier)
Critical Logs: ERROR level triggers Slack/PagerDuty notifications
```

**3. Distributed Tracing (Jaeger/Zipkin)**
```
Instrumentation: Spring Cloud Sleuth (auto-instruments Spring components)
Sampling: 10% in production, 100% in dev/staging
Trace Propagation: B3 format in HTTP headers, gRPC metadata
Span Tags: restaurant_id, order_id, user_id for filtering
```

#### Health Checks
```java
@Component
public class KafkaHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check Kafka broker connectivity
        // Check consumer lag < threshold
        return isHealthy ? Health.up() : Health.down();
    }
}

// Kubernetes probes:
livenessProbe:  /actuator/health/liveness
readinessProbe: /actuator/health/readiness
startupProbe:   /actuator/health/startup
```

---

## 5. Service-Level Deep Dive

### 5.1 Order Service (Core Business Logic)

**Responsibilities:**
- Order lifecycle management (PENDING → CONFIRMED → PREPARING → READY → DELIVERED/COMPLETED)
- Order validation (menu items exist, restaurant open, delivery address valid)
- Price calculation with promotions/discounts
- Saga orchestration for distributed transactions

**API Design (REST + gRPC):**

**REST Endpoints:**
```
POST   /api/v1/orders                    - Create order
GET    /api/v1/orders/{orderId}          - Get order details
GET    /api/v1/orders?userId={id}        - List user orders
PATCH  /api/v1/orders/{orderId}/cancel   - Cancel order
GET    /api/v1/orders/{orderId}/track    - Real-time tracking (WebSocket upgrade)
```

**gRPC Services:**
```protobuf
service OrderService {
  rpc CreateOrder(CreateOrderRequest) returns (OrderResponse);
  rpc GetOrder(GetOrderRequest) returns (OrderResponse);
  rpc ValidateOrderItems(ValidateItemsRequest) returns (ValidationResponse);
}
```

**Domain Model:**
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    private UUID id;
    private UUID customerId;
    private UUID restaurantId;
    private OrderType type; // DELIVERY, TAKEAWAY, DINE_IN, SCHEDULED
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime scheduledTime;
    private UUID bookingId; // nullable, for hybrid orders
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
    @Embedded
    private DeliveryAddress deliveryAddress;
    
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
}

public enum OrderStatus {
    PENDING,           // Initial state
    PAYMENT_PENDING,   // Awaiting payment
    CONFIRMED,         // Payment successful
    PREPARING,         // Kitchen started
    READY,             // Ready for pickup/delivery
    OUT_FOR_DELIVERY,  // Courier dispatched
    DELIVERED,         // Customer received
    COMPLETED,         // Finalized (feedback collected)
    CANCELLED,         // User/system cancelled
    FAILED             // Payment/validation failed
}
```

**State Machine (Spring State Machine):**
```java
@Configuration
@EnableStateMachine
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderStatus, OrderEvent> {
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> transitions) {
        transitions
            .withExternal().source(PENDING).target(PAYMENT_PENDING).event(PAYMENT_INITIATED)
            .withExternal().source(PAYMENT_PENDING).target(CONFIRMED).event(PAYMENT_COMPLETED)
            .withExternal().source(CONFIRMED).target(PREPARING).event(KITCHEN_STARTED)
            .withExternal().source(PREPARING).target(READY).event(KITCHEN_COMPLETED)
            .withExternal().source(READY).target(OUT_FOR_DELIVERY).event(COURIER_ASSIGNED)
            .withExternal().source(OUT_FOR_DELIVERY).target(DELIVERED).event(DELIVERY_CONFIRMED)
            .withExternal().source(DELIVERED).target(COMPLETED).event(FEEDBACK_RECEIVED);
    }
}
```

### 5.2 Kitchen Service (Real-Time Queue Management)

**Responsibilities:**
- Intelligent order distribution across multiple kitchen stations
- Queue optimization (minimize prep time, balance workload)
- Real-time updates to Kitchen Display System (KDS) via WebSockets
- Scheduled order preparation (start 30 mins before pickup time)

**Queue Distribution Algorithm:**
```java
@Service
public class KitchenQueueOptimizer {
    
    // Priority scoring: ASAP orders > Scheduled (near deadline) > Pre-orders
    public KitchenStation assignOrder(Order order, List<KitchenStation> stations) {
        return stations.stream()
            .filter(KitchenStation::isActive)
            .min(Comparator.comparingInt(station -> 
                calculateScore(station, order)
            ))
            .orElseThrow();
    }
    
    private int calculateScore(KitchenStation station, Order order) {
        int currentLoad = station.getQueueSize();
        int estimatedTime = station.getEstimatedCompletionTime();
        int skillMatch = station.specializes(order.getCategory()) ? -10 : 0;
        return currentLoad * 5 + estimatedTime + skillMatch;
    }
}
```

**WebSocket Protocol:**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/kitchen")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}

// Broadcasting order updates
@Controller
public class KitchenWebSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    public void notifyKitchenDisplay(UUID restaurantId, OrderUpdateEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/kitchen/" + restaurantId, 
            event
        );
    }
}
```

### 5.3 Booking Service (Table Reservation & Capacity Management)

**Responsibilities:**
- Table availability calculation (considering restaurant capacity, operating hours)
- Reservation conflict prevention (double-booking)
- Integration with Order Service for hybrid orders
- Automated reminders (1 hour before reservation)

**Capacity Model:**
```java
@Entity
public class Restaurant {
    @Id
    private UUID id;
    private String name;
    
    @OneToMany(mappedBy = "restaurant")
    private List<TableConfiguration> tables;
    
    @Embedded
    private OperatingHours hours; // open/close times per day
    
    private int maxPartySize;
}

@Entity
public class TableConfiguration {
    @Id
    private UUID id;
    private UUID restaurantId;
    private String tableNumber;
    private int capacity;
    private TableType type; // INDOOR, OUTDOOR, PRIVATE_ROOM
    private boolean isActive;
}

@Entity
public class Booking {
    @Id
    private UUID id;
    private UUID customerId;
    private UUID restaurantId;
    private UUID tableId;
    private LocalDateTime reservationTime;
    private int partySize;
    private BookingStatus status; // PENDING, CONFIRMED, SEATED, COMPLETED, CANCELLED
    private UUID preOrderId; // nullable
    private String specialRequests;
}
```

**Availability Algorithm (Pessimistic Locking):**
```java
@Transactional
public List<AvailableSlot> findAvailability(UUID restaurantId, LocalDate date, int partySize) {
    Restaurant restaurant = restaurantRepository.findById(restaurantId);
    List<TableConfiguration> suitableTables = restaurant.getTables().stream()
        .filter(t -> t.getCapacity() >= partySize)
        .collect(Collectors.toList());
    
    // Generate time slots (e.g., every 30 mins)
    List<LocalDateTime> slots = generateTimeSlots(date, restaurant.getHours());
    
    return slots.stream()
        .filter(slot -> hasAvailableTable(suitableTables, slot))
        .map(slot -> new AvailableSlot(slot, suitableTables.size()))
        .collect(Collectors.toList());
}

@Lock(LockModeType.PESSIMISTIC_WRITE)
private boolean hasAvailableTable(List<TableConfiguration> tables, LocalDateTime slot) {
    // Check for existing bookings with overlap (assume 2-hour duration)
    LocalDateTime slotEnd = slot.plusHours(2);
    
    return tables.stream().anyMatch(table -> 
        bookingRepository.countOverlappingBookings(
            table.getId(), slot, slotEnd
        ) == 0
    );
}
```

### 5.4 Delivery Service (Logistics & Route Optimization)

**Responsibilities:**
- Courier assignment (nearest available, capacity-based)
- Delivery time estimation (using PostGIS for distance calculation)
- Real-time location tracking
- Multi-order batching (if courier delivers multiple orders on same route)

**Geospatial Queries (PostGIS):**
```sql
-- Find nearest available courier
SELECT courier_id, 
       ST_Distance(
           current_location::geography, 
           ST_SetSRID(ST_MakePoint($restaurantLon, $restaurantLat), 4326)::geography
       ) as distance_meters
FROM couriers
WHERE status = 'AVAILABLE'
ORDER BY distance_meters
LIMIT 1;

-- Calculate delivery route
SELECT ST_AsGeoJSON(
    ST_ShortestLine(
        ST_SetSRID(ST_MakePoint($restaurantLon, $restaurantLat), 4326),
        ST_SetSRID(ST_MakePoint($customerLon, $customerLat), 4326)
    )
) as route;
```

**Courier Assignment Algorithm:**
```java
@Service
public class CourierAssignmentService {
    
    @Autowired
    private CourierRepository courierRepository;
    
    public DeliveryAssignment assignCourier(Order order, Location restaurantLocation) {
        List<Courier> available = courierRepository.findAvailable();
        
        Courier bestCourier = available.stream()
            .min(Comparator.comparingDouble(courier -> 
                calculateCost(courier, order, restaurantLocation)
            ))
            .orElseThrow(() -> new NoCourierAvailableException());
        
        bestCourier.setStatus(CourierStatus.ASSIGNED);
        courierRepository.save(bestCourier);
        
        return new DeliveryAssignment(bestCourier.getId(), order.getId());
    }
    
    private double calculateCost(Courier courier, Order order, Location restaurant) {
        double distance = geoService.distance(courier.getCurrentLocation(), restaurant);
        double rating = courier.getRating(); // Higher rating = lower cost
        int currentLoad = courier.getCurrentOrders().size();
        
        return (distance * 1.0) + (currentLoad * 50) - (rating * 10);
    }
}
```

---

## 6. DevOps & CI/CD Pipeline

### 6.1 CI/CD Workflow (GitHub Actions)

**Pipeline Stages:**

```yaml
# .github/workflows/microservice-pipeline.yml
name: Microservice CI/CD

on:
  push:
    branches: [main, develop]
    paths:
      - 'services/**'
  pull_request:
    branches: [main, develop]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [order-service, catalog-service, kitchen-service, booking-service]
    
    steps:
      - name: Checkout
        uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Cache Maven dependencies
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Build with Maven
        run: |
          cd services/${{ matrix.service }}
          mvn clean package -DskipTests
      
      - name: Run Unit Tests
        run: |
          cd services/${{ matrix.service }}
          mvn test
      
      - name: Run Integration Tests (Testcontainers)
        run: |
          cd services/${{ matrix.service }}
          mvn verify -Pintegration-tests
        env:
          TESTCONTAINERS_RYUK_DISABLED: false
      
      - name: SonarQube Analysis
        run: |
          cd services/${{ matrix.service }}
          mvn sonar:sonar \
            -Dsonar.projectKey=pizzaflow-${{ matrix.service }} \
            -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }} \
            -Dsonar.login=${{ secrets.SONAR_TOKEN }}
      
      - name: Build Docker Image
        run: |
          cd services/${{ matrix.service }}
          docker build -t pizzaflow/${{ matrix.service }}:${{ github.sha }} .
      
      - name: Push to Docker Registry
        if: github.ref == 'refs/heads/main'
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker push pizzaflow/${{ matrix.service }}:${{ github.sha }}
          docker tag pizzaflow/${{ matrix.service }}:${{ github.sha }} pizzaflow/${{ matrix.service }}:latest
          docker push pizzaflow/${{ matrix.service }}:latest
  
  deploy-to-k8s:
    needs: build-and-test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout
        uses: actions/checkout@v3
      
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1
      
      - name: Update kubeconfig
        run: |
          aws eks update-kubeconfig --name pizzaflow-cluster --region us-east-1
      
      - name: Deploy with Helm
        run: |
          helm upgrade --install pizzaflow ./infrastructure/helm/pizzaflow \
            --set image.tag=${{ github.sha }} \
            --namespace production \
            --create-namespace
      
      - name: Run Smoke Tests
        run: |
          kubectl wait --for=condition=ready pod -l app=order-service -n production --timeout=300s
          curl -f https://api.pizzaflow.com/health || exit 1
```

### 6.2 Docker Strategy

**Multi-Stage Dockerfile (Example for Order Service):**

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY ../pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

# JVM Tuning for containerized environments
ENV JAVA_OPTS="-XX:+UseZGC \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseContainerSupport \
               -XX:+EnableDynamicAgentLoading \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 6.3 Kubernetes Deployment

**Deployment Manifest (Order Service):**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
  namespace: production
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
        version: v1
    spec:
      containers:
      - name: order-service
        image: pizzaflow/order-service:latest
        ports:
        - containerPort: 8080
          name: http
        - containerPort: 9090
          name: grpc
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: order-service-secrets
              key: database-url
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-cluster:9092"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: order-service
  namespace: production
spec:
  selector:
    app: order-service
  ports:
  - name: http
    port: 80
    targetPort: 8080
  - name: grpc
    port: 9090
    targetPort: 9090
  type: ClusterIP
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### 6.4 Infrastructure as Code (Terraform)

**EKS Cluster Provisioning:**

```hcl
# infrastructure/terraform/eks-cluster.tf
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 19.0"

  cluster_name    = "pizzaflow-cluster"
  cluster_version = "1.28"

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  eks_managed_node_groups = {
    general = {
      desired_size = 3
      min_size     = 2
      max_size     = 10

      instance_types = ["t3.large"]
      capacity_type  = "ON_DEMAND"
      
      labels = {
        role = "general"
      }
    }
    
    high-memory = {
      desired_size = 2
      min_size     = 1
      max_size     = 5

      instance_types = ["r6i.xlarge"]
      capacity_type  = "SPOT"
      
      labels = {
        role = "high-memory"
      }
      
      taints = {
        dedicated = {
          key    = "high-memory"
          value  = "true"
          effect = "NoSchedule"
        }
      }
    }
  }

  tags = {
    Environment = "production"
    Project     = "PizzaFlow"
  }
}
```

### 6.5 Monitoring & Alerting Setup

**Prometheus Configuration:**

```yaml
# infrastructure/monitoring/prometheus-config.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'kubernetes-pods'
    kubernetes_sd_configs:
      - role: pod
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
        action: replace
        target_label: __metrics_path__
        regex: (.+)
      - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
        action: replace
        regex: ([^:]+)(?::\d+)?;(\d+)
        replacement: $1:$2
        target_label: __address__

rule_files:
  - '/etc/prometheus/alerts.yml'

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']
```

**Alert Rules:**

```yaml
# infrastructure/monitoring/alerts.yml
groups:
  - name: pizzaflow_alerts
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected in {{ $labels.service }}"
          description: "Service {{ $labels.service }} has error rate above 5% for 5 minutes"
      
      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1.0
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High latency in {{ $labels.service }}"
          description: "95th percentile latency is {{ $value }}s"
      
      - alert: KafkaConsumerLag
        expr: kafka_consumer_lag > 1000
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Kafka consumer lag high for {{ $labels.topic }}"
          description: "Consumer group {{ $labels.group }} has lag of {{ $value }}"
      
      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool near exhaustion"
          description: "Service {{ $labels.service }} is using {{ $value }}% of connections"
```

---

## 7. Testing Strategy

### 7.1 Test Pyramid

```
           /\
          /  \  E2E (5%)        - Critical user journeys
         /____\
        /      \  Integration (25%) - Service interactions, DB, Kafka
       /________\
      /          \  Unit (70%)      - Business logic, edge cases
     /____________\
```

### 7.2 Unit Testing (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    @DisplayName("Should create order and publish event when all validations pass")
    void testCreateOrder_Success() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(new OrderItem("PIZZA_MARGHERITA", 2)),
            OrderType.DELIVERY
        );
        
        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setStatus(OrderStatus.PENDING);
        
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        // When
        OrderResponse response = orderService.createOrder(request);
        
        // Then
        assertNotNull(response.getOrderId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(
            eq("order.created"),
            any(OrderEvent.class)
        );
    }
    
    @Test
    @DisplayName("Should throw exception when restaurant is closed")
    void testCreateOrder_RestaurantClosed() {
        // Test implementation...
    }
}
```

### 7.3 Integration Testing (Testcontainers)

```java
@SpringBootTest
@Testcontainers
class OrderServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("orders")
        .withUsername("test")
        .withPassword("test");
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    @DisplayName("Should persist order to database and publish to Kafka")
    void testOrderCreationIntegration() {
        // Given
        CreateOrderRequest request = buildValidRequest();
        
        // When
        OrderResponse response = orderService.createOrder(request);
        
        // Then
        Optional<Order> savedOrder = orderRepository.findById(response.getOrderId());
        assertTrue(savedOrder.isPresent());
        assertEquals(OrderStatus.PENDING, savedOrder.get().getStatus());
        
        // Verify Kafka message (using embedded consumer)
        // Implementation...
    }
}
```

### 7.4 Contract Testing (Spring Cloud Contract)

```groovy
// order-service/src/test/resources/contracts/shouldReturnOrderById.groovy
Contract.make {
    description "Should return order details by ID"
    request {
        method GET()
        url("/api/v1/orders/123e4567-e89b-12d3-a456-426614174000")
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status 200
        body([
            orderId: "123e4567-e89b-12d3-a456-426614174000",
            customerId: "987e6543-e21b-12d3-a456-426614174000",
            status: "CONFIRMED",
            totalAmount: 29.99,
            items: [
                [itemId: "PIZZA_MARGHERITA", quantity: 1, price: 12.99],
                [itemId: "COKE", quantity: 2, price: 3.50]
            ]
        ])
        headers {
            contentType(applicationJson())
        }
    }
}
```

### 7.5 Performance Testing (Gatling)

```scala
// performance-tests/src/test/scala/OrderServiceSimulation.scala
class OrderServiceSimulation extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
  
  val createOrderScenario = scenario("Create Order")
    .exec(
      http("Create Order")
        .post("/api/v1/orders")
        .body(StringBody("""
          {
            "customerId": "${customerId}",
            "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
            "items": [
              {"itemId": "PIZZA_MARGHERITA", "quantity": 2}
            ],
            "type": "DELIVERY"
          }
        """))
        .check(status.is(201))
        .check(jsonPath("$.orderId").saveAs("orderId"))
    )
    .pause(1)
    .exec(
      http("Get Order")
        .get("/api/v1/orders/${orderId}")
        .check(status.is(200))
    )
  
  setUp(
    createOrderScenario.inject(
      rampUsersPerSec(10) to 100 during (60 seconds),
      constantUsersPerSec(100) during (5 minutes)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.percentile3.lt(1000),
     global.successfulRequests.percent.gt(99)
   )
}
```

---

## 8. Implementation Roadmap

### Phase 1: Foundation (Weeks 1-4) - MVP Core

**Goal:** Establish infrastructure and core order flow.

**Week 1-2: Infrastructure Setup**
- [ ] Initialize Maven multi-module project structure
- [ ] Set up GitHub repository with branch protection rules
- [ ] Configure Keycloak instance (Docker Compose)
- [ ] Deploy PostgreSQL, MongoDB, Redis via Docker Compose
- [ ] Set up Kafka cluster (single broker for dev)
- [ ] Create common-libs (common-dto, common-security)
- [ ] Implement API Gateway (Spring Cloud Gateway) with JWT validation
- [ ] Set up Config Server with Git backend
- [ ] Implement Service Discovery (Eureka Server)

**Week 3: Core Services Development**
- [ ] **Catalog Service:**
  - REST APIs: GET /menu, GET /menu/{itemId}
  - MongoDB schema for menu items
  - Basic caching with Redis
- [ ] **Order Service:**
  - REST APIs: POST /orders, GET /orders/{id}
  - PostgreSQL schema with Flyway migrations
  - Kafka producer for order.created events
  - Basic order validation

**Week 4: Payment & Kitchen**
- [ ] **Payment Service:**
  - Mock payment gateway integration
  - Kafka consumer for order.created
  - Kafka producer for payment.completed
- [ ] **Kitchen Service:**
  - Kafka consumer for order.confirmed
  - WebSocket endpoint for real-time updates
  - Redis-based queue management
- [ ] **Integration Testing:**
  - End-to-end flow: Create order → Payment → Kitchen

**Deliverables:**
- Working order flow: Customer → Order → Payment → Kitchen
- Docker Compose setup for entire stack
- Basic monitoring with Spring Boot Actuator

---

### Phase 2: Advanced Features (Weeks 5-8)

**Goal:** Add booking, delivery, and resilience patterns.

**Week 5: Booking Service**
- [ ] Table configuration management
- [ ] Availability algorithm with pessimistic locking
- [ ] REST APIs: POST /bookings, GET /availability
- [ ] Integration with Order Service (hybrid orders)
- [ ] Scheduled jobs for reminders (Spring Scheduler)

**Week 6: Delivery Service**
- [ ] PostgreSQL with PostGIS extension
- [ ] Courier management APIs
- [ ] Geospatial queries for nearest courier
- [ ] Kafka integration (order.ready → assign courier)
- [ ] Real-time tracking WebSocket

**Week 7: Inventory Service**
- [ ] Ingredient stock management
- [ ] Outbox pattern implementation
- [ ] Kafka producer for inventory events
- [ ] Low-stock alerts
- [ ] Saga compensation logic

**Week 8: Resilience & Observability**
- [ ] Implement Circuit Breaker (Resilience4j) across services
- [ ] Add retry and bulkhead patterns
- [ ] Set up Prometheus + Grafana dashboards
- [ ] Configure distributed tracing (Jaeger)
- [ ] Centralized logging (ELK stack or Loki)

**Deliverables:**
- Full feature parity: Orders, bookings, delivery
- Resilience patterns protecting critical paths
- Comprehensive observability stack

---

### Phase 3: Cloud-Native & DevOps (Weeks 9-12)

**Goal:** Production-ready deployment on Kubernetes.

**Week 9: Kubernetes Migration**
- [ ] Create Helm charts for each microservice
- [ ] Set up Kubernetes cluster (Minikube/EKS)
- [ ] Deploy infrastructure (Kafka, PostgreSQL via Operators)
- [ ] Configure Ingress Controller (NGINX)
- [ ] Implement HPA (Horizontal Pod Autoscaler)

**Week 10: CI/CD Pipeline**
- [ ] GitHub Actions workflows per service
- [ ] Automated testing (unit, integration, contract)
- [ ] Docker image building and pushing
- [ ] Helm deployment automation
- [ ] Smoke tests post-deployment

**Week 11: Advanced DevOps**
- [ ] Infrastructure as Code with Terraform (EKS cluster)
- [ ] GitOps with ArgoCD
- [ ] Secret management (Sealed Secrets or Vault)
- [ ] Service Mesh (Istio) for mTLS and traffic management
- [ ] Chaos Engineering experiments (Chaos Mesh)

**Week 12: Optimization & Tuning**
- [ ] JVM tuning for Virtual Threads (ZGC, container-aware heap)
- [ ] Database query optimization (EXPLAIN ANALYZE)
- [ ] Kafka producer/consumer tuning
- [ ] Load testing with Gatling (target: 1000 req/s)
- [ ] Cost optimization (right-sizing pods, spot instances)

**Deliverables:**
- Production-grade Kubernetes deployment
- Fully automated CI/CD pipeline
- Performance benchmarks documented
- Runbook for operations

---

### Phase 4: Advanced Topics (Weeks 13-16) - Optional

**Goal:** Showcase cutting-edge practices.

**Week 13: Advanced Patterns**
- [ ] Implement CQRS with separate read models
- [ ] Event Sourcing for Order Service (audit trail)
- [ ] API versioning strategy (v1, v2 coexistence)
- [ ] Rate limiting per customer tier (Redis Lua scripts)

**Week 14: Machine Learning Integration**
- [ ] Demand forecasting (predict busy hours)
- [ ] Personalized menu recommendations (Python service via gRPC)
- [ ] Delivery time prediction (using historical data)

**Week 15: Mobile & Frontend**
- [ ] React frontend (Vite) with real-time updates
- [ ] WebSocket integration for order tracking
- [ ] Mobile app (React Native) - basic prototype

**Week 16: Documentation & Portfolio**
- [ ] Comprehensive API documentation (OpenAPI/Swagger)
- [ ] Architecture Decision Records (ADRs)
- [ ] Video demo of key features
- [ ] Blog post series on key learnings

**Deliverables:**
- Portfolio-ready project with advanced features
- Comprehensive documentation for interviews
- Public demo environment

---

## 9. Key Architectural Decisions (ADRs)

### ADR-001: Choosing Event-Driven Architecture

**Status:** Accepted

**Context:**
PizzaFlow requires loose coupling between services, asynchronous processing of orders, and the ability to scale services independently.

**Decision:**
Adopt Event-Driven Architecture (EDA) using Apache Kafka as the message broker.

**Consequences:**
- **Positive:** Services can evolve independently; easy to add new consumers.
- **Negative:** Increased complexity in debugging; eventual consistency challenges.
- **Mitigation:** Implement distributed tracing; use Saga pattern for transactions.

---

### ADR-002: Database per Service Pattern

**Status:** Accepted

**Context:**
Microservices should own their data to prevent tight coupling.

**Decision:**
Each service has its own database (PostgreSQL, MongoDB, Redis based on needs).

**Consequences:**
- **Positive:** True service autonomy; optimized schema per domain.
- **Negative:** Cross-service queries difficult; data duplication.
- **Mitigation:** Use CQRS for read models; API composition for queries.

---

### ADR-003: gRPC for Internal Communication

**Status:** Accepted

**Context:**
Need low-latency, high-throughput communication for synchronous calls (e.g., Catalog → Inventory).

**Decision:**
Use gRPC with Protobuf for internal service-to-service communication.

**Consequences:**
- **Positive:** 5-7x faster than REST; strong typing; streaming support.
- **Negative:** Less human-readable; requires Protobuf schema management.
- **Mitigation:** Version Protobuf schemas carefully; provide REST gateway for external APIs.

---

### ADR-004: Virtual Threads (Project Loom)

**Status:** Accepted

**Context:**
Java 21 introduces Virtual Threads, which can handle millions of concurrent tasks without the overhead of platform threads.

**Decision:**
Leverage Virtual Threads for I/O-bound operations (DB queries, HTTP calls, Kafka).

**Configuration:**
```java
@Bean
public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
    return protocolHandler -> {
        protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    };
}
```

**Consequences:**
- **Positive:** Simplified concurrency model; better resource utilization.
- **Negative:** Still experimental; potential issues with thread-local variables.
- **Mitigation:** Thorough load testing; avoid synchronized blocks in hot paths.

---

## 10. Non-Functional Requirements

### 10.1 Performance Targets

| **Metric** | **Target** | **Measurement Method** |
| --- | --- | --- |
| API Response Time (P95) | < 500ms | Prometheus histogram |
| Order Creation Latency | < 200ms | Custom metric |
| Kafka End-to-End Latency | < 100ms | Offset-to-commit tracking |
| Database Query Time (P99) | < 50ms | Slow query log |
| Throughput | 1000 orders/sec | Gatling load test |
| Concurrent Users | 10,000 | WebSocket connection pool |

### 10.2 Availability & Resilience

- **Target Uptime:** 99.9% (8.76 hours downtime/year)
- **RTO (Recovery Time Objective):** < 15 minutes
- **RPO (Recovery Point Objective):** < 5 minutes (Kafka replication)
- **Disaster Recovery:** Multi-region deployment (Phase 4)

### 10.3 Scalability

- **Horizontal Scaling:** All services stateless (session in Redis/JWT)
- **Database Scaling:** Read replicas for heavy-read services (Catalog)
- **Kafka Partitioning:** By restaurant_id for parallel consumption
- **Caching Strategy:** Redis for hot data (menu items, active orders)

### 10.4 Security

- **Authentication:** OAuth2/OIDC via Keycloak
- **Authorization:** Role-based + attribute-based (restaurant_id scope)
- **Data Encryption:** TLS 1.3 for all external communication, mTLS for internal
- **Secrets Management:** Kubernetes Secrets (Phase 1), Vault (Phase 3)
- **Compliance:** GDPR-compliant data handling (right to erasure)

---

## 11. Cost Estimation (AWS EKS Deployment)

**Monthly Cost Breakdown (Production Environment):**

| **Component** | **Configuration** | **Monthly Cost (USD)** |
| --- | --- | --- |
| EKS Cluster | Control plane | $73 |
| EC2 Instances | 5x t3.large (on-demand) | $370 |
| RDS PostgreSQL | db.t3.medium (Multi-AZ) | $130 |
| ElastiCache Redis | cache.t3.medium | $75 |
| MSK (Kafka) | 3 brokers, kafka.m5.large | $450 |
| Load Balancer | ALB | $25 |
| Data Transfer | 1TB outbound | $90 |
| S3 Storage | Logs, backups (500GB) | $12 |
| **Total** |  | **~$1,225/month** |

**Cost Optimization Strategies:**
- Use Spot Instances for non-critical services (50-70% savings)
- Implement auto-scaling to reduce idle capacity
- Use S3 Intelligent-Tiering for log archives
- Right-size database instances based on metrics

---

## 12. Learning Outcomes & Interview Talking Points

### For Java Developer Role:
✅ **Spring Boot 3.4 & Java 21:** Virtual Threads, Records, Pattern Matching  
✅ **Microservices Patterns:** Saga, Outbox, CQRS, Event Sourcing  
✅ **gRPC & Kafka:** High-performance inter-service communication  
✅ **Resilience4j:** Circuit Breaker, Retry, Bulkhead in production  
✅ **Spring Cloud:** Gateway, Config Server, Service Discovery  
✅ **Testing:** Unit (JUnit 5), Integration (Testcontainers), Contract (Spring Cloud Contract)  

### For DevOps/SRE Role:
✅ **Kubernetes:** Deployments, Services, HPA, StatefulSets  
✅ **Helm:** Templating, dependency management, release management  
✅ **CI/CD:** GitHub Actions, multi-stage pipelines, automated testing  
✅ **Observability:** Prometheus, Grafana, Jaeger, ELK Stack  
✅ **IaC:** Terraform for AWS infrastructure provisioning  
✅ **GitOps:** ArgoCD for declarative deployments  

### For System Design Interview:
✅ **Scalability:** Horizontal scaling, database sharding strategies  
✅ **Consistency:** CAP theorem trade-offs, eventual consistency handling  
✅ **Fault Tolerance:** Graceful degradation, circuit breakers  
✅ **Real-time Systems:** WebSockets, event-driven updates  
✅ **Geospatial:** PostGIS for location-based services  

---

## 13. Next Steps

1. **Review & Validate:** Go through this architecture plan and validate against your requirements.
2. **Set Up Repository:** Initialize Git repository with proposed folder structure.
3. **Phase 1 Kickoff:** Start with infrastructure setup (Docker Compose for local dev).
4. **Iterative Development:** Follow the roadmap, commit frequently, document decisions.
5. **Continuous Learning:** Experiment with new patterns, refactor as you learn.

---

**Document Version:** 2.0  
**Last Updated:** December 29, 2025  
**Author:** Oleh Malko (with AI-assisted architectural guidance)  
**Status:** Ready for Implementation
