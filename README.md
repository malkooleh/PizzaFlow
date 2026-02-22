# 🍕 PizzaFlow: Cloud-Native Pizzeria Ecosystem

## 📖 Overview

**PizzaFlow** is a high-performance, event-driven ecosystem designed to manage multi-location pizzeria chains. This project serves as a showcase for building scalable, resilient systems using **Java 21**, **Spring Boot 3.4**, and modern cloud-native patterns.

The system handles the entire lifecycle of a pizza—from inventory management and customer booking to smart kitchen queuing and delivery logistics.

---

## 🏗️ Architecture

PizzaFlow follows an **Event-Driven Microservices** architecture to ensure loose coupling and high availability.

### System Landscape

* **Infrastructure Suite:** 
* *Service Discovery (Eureka):* Dynamic service registration.
* *Config Server:* Centralized, environment-specific configurations.
* *API Gateway:* Single entry point with rate limiting and routing.


* **Core Domains:**
* **Catalog & Inventory:** Real-time ingredient tracking and menu management.
* **Sales (Order/Payment/Booking):** Transactional integrity for diverse order types.
* **Operations (Kitchen/Delivery):** Smart dispatching and queue optimization.


* **Cross-cutting Concerns:**
* **Notification Service:** Real-time updates via WebSockets/Email.
* **Audit Log:** Comprehensive tracking of all critical system mutations.



---

## 🚀 Key Features

* **Virtual Threads (Project Loom):** Optimized for high-concurrency I/O operations without the overhead of traditional platform threads.
* **Event-Driven Communication:** Utilizing **Apache Kafka** for reliable asynchronous data flow between domains.
* **High-Performance RPC:** **gRPC** implemented for low-latency synchronous internal service calls.
* **Intelligent Kitchen Distribution:** Algorithms to balance workload across multiple restaurant locations.
* **Identity Management:** Integrated with **Keycloak** for OAuth2/OIDC, supporting Role-Based and Attribute-Based Access Control (RBAC/ABAC).

---

## 🛠️ Tech Stack

| Component | Technology |
| --- | --- |
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.4.x, Spring Cloud |
| **Persistence** | PostgreSQL, Redis (Caching) |
| **Messaging** | Apache Kafka |
| **Communication** | gRPC, REST, WebSockets |
| **Security** | Keycloak, Spring Security, JWT |
| **Observability** | Prometheus, Grafana, Micrometer |
| **Testing** | JUnit 5, Testcontainers, Mockito |
| **Infrastructure** | Docker, Kubernetes, Helm, Terraform |

---

## � Project Structure

```
pizza-flow/
├── services/                       # Microservices
│   ├── discovery-service/          # Eureka Service Discovery
│   ├── config-service/             # Spring Cloud Config Server
│   ├── gateway-service/            # API Gateway
│   ├── catalog-service/            # Menu & Product Management
│   ├── inventory-service/          # Ingredient Tracking
│   ├── order-service/              # Order Processing
│   ├── payment-service/            # Payment Processing
│   ├── kitchen-service/            # Kitchen Operations
│   ├── booking-service/            # Table Reservations
│   ├── delivery-service/           # Delivery Management
│   └── notification-service/       # Notifications (Email, SMS, Push)
│
├── common-libs/                    # Shared Libraries
│   ├── common-domain/              # Domain Models & Events
│   ├── common-web/                 # Web Utilities & Exception Handling
│   ├── common-security/            # Security Configuration
│   ├── common-kafka/               # Kafka Producers & Consumers
│   ├── common-resilience/          # Circuit Breakers & Retry Patterns
│   ├── common-grpc/                # gRPC Proto Definitions
│   └── common-observability/       # Tracing, Metrics & Logging
│
├── infrastructure/
│   ├── docker/                     # Docker Compose Configurations
│   ├── k8s/                        # Kubernetes Manifests
│   ├── helm/                       # Helm Charts
│   └── terraform/                  # AWS Infrastructure as Code
│
├── monitoring/
│   └── grafana/                    # Dashboards & Provisioning
│
├── api-tests/                      # Integration Tests (REST Assured)
└── docs/                           # Documentation
```

---

## �🚦 Getting Started

### Prerequisites

* Docker & Docker Compose
* JDK 21
* Maven 3.9+

### Quick Start

1. **Clone the repository:**
```bash
git clone https://github.com/malkooleh/PizzaFlow.git
cd PizzaFlow
```


2. **Build the project:**
```bash
./mvnw clean package -DskipTests
```


3. **Spin up the infrastructure:**
```bash
docker-compose up -d
```

4. **Access the services:**
   - API Gateway: http://localhost:8080
   - Eureka Dashboard: http://localhost:8761
   - Keycloak Admin: http://localhost:9090 (admin/admin)
   - Prometheus: http://localhost:9091
   - Grafana: http://localhost:3000 (admin/admin)

---

## ☁️ Cloud Deployment

### Kubernetes (Helm)

```bash
# Add Helm repo and update
cd infrastructure/helm/pizza-flow

# Install in development
helm install pizza-flow . -f values-dev.yaml -n pizza-flow --create-namespace

# Install in production
helm install pizza-flow . -f values-prod.yaml -n pizza-flow --create-namespace
```

### AWS Infrastructure (Terraform)

```bash
cd infrastructure/terraform

# Initialize
terraform init

# Deploy to development
terraform workspace new dev
terraform apply -var-file=environments/dev.tfvars

# Deploy to production
terraform workspace new production
terraform apply -var-file=environments/production.tfvars
```

The Terraform configuration provisions:
- **VPC** with public/private subnets across 3 AZs
- **EKS** cluster with managed node groups
- **RDS PostgreSQL** with Multi-AZ for production
- **ElastiCache Redis** for caching
- **MSK Kafka** for event streaming

---

## 📊 Observability

### Distributed Tracing
- Trace correlation across all services via Micrometer Tracing
- Zipkin integration for trace visualization
- Custom business spans with TracingUtils

### Metrics
- Prometheus metrics for all services
- Custom business metrics (orders, payments, deliveries)
- JVM, HTTP, Kafka consumer metrics

### Centralized Logging
- **Loki** for log aggregation (lightweight, Prometheus-like)
- **Promtail** for log collection from all services
- JSON structured logging with trace correlation (traceId, spanId)
- LogQL queries for searching and filtering

### Pre-built Grafana Dashboards
- **Service Health**: Request rates, latencies, error rates, JVM stats
- **Business Metrics**: Orders, payments, inventory, delivery KPIs
- **Kafka Consumer Lag**: Consumer lag monitoring per topic/group
- **Log Explorer**: Unified log search, error analysis, trace correlation

---

## 🛡️ Resilience Patterns

The platform implements comprehensive resilience patterns via Resilience4j:

| Pattern | Purpose |
|---------|---------|
| **Circuit Breaker** | Prevent cascading failures |
| **Retry** | Handle transient failures |
| **Rate Limiter** | Protect from overload |
| **Bulkhead** | Isolate failures |
| **Time Limiter** | Prevent slow calls from blocking |

Feign clients include automatic fallbacks for graceful degradation.

---

## 🔌 gRPC Services

Internal service communication uses gRPC for low-latency calls:

| Service | Proto File | Operations |
|---------|------------|------------|
| Catalog | `catalog.proto` | GetMenuItem, SearchMenu, ListCategories |
| Inventory | `inventory.proto` | CheckStock, ReserveIngredients |
| Order | `order.proto` | CreateOrder, StreamOrders, KitchenQueue |
| Payment | `payment.proto` | ProcessPayment, Refund, GetStatus |
| Kitchen | `kitchen.proto` | CreateTicket, UpdateStatus, GetMetrics |
| Delivery | `delivery.proto` | AssignDriver, TrackDelivery, UpdateStatus |

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.