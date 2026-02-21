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

## 🚦 Getting Started

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

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---