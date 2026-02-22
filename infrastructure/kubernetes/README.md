# PizzaFlow Kubernetes Deployment

This directory contains Kubernetes manifests for deploying the PizzaFlow microservices platform.

## Directory Structure

```
kubernetes/
├── base/                          # Base manifests (Kustomize)
│   ├── namespace.yaml
│   ├── kustomization.yaml
│   ├── configmaps/
│   │   └── common-config.yaml
│   ├── secrets/
│   │   └── credentials.yaml
│   ├── infrastructure/            # Databases, messaging, etc.
│   │   ├── postgres.yaml
│   │   ├── mongodb.yaml
│   │   ├── redis.yaml
│   │   ├── zookeeper.yaml
│   │   ├── kafka.yaml
│   │   └── keycloak.yaml
│   └── services/                  # Application services
│       ├── discovery-service.yaml
│       ├── config-service.yaml
│       ├── api-gateway.yaml
│       ├── catalog-service.yaml
│       ├── order-service.yaml
│       ├── payment-service.yaml
│       ├── kitchen-service.yaml
│       ├── inventory-service.yaml
│       ├── booking-service.yaml
│       ├── delivery-service.yaml
│       └── notification-service.yaml
└── overlays/                      # Environment-specific patches
    ├── dev/
    ├── staging/
    └── production/
        ├── kustomization.yaml
        ├── hpa.yaml              # Horizontal Pod Autoscalers
        ├── pdb.yaml              # Pod Disruption Budgets
        └── network-policies.yaml # Network security policies
```

## Prerequisites

- Kubernetes cluster (1.28+)
- kubectl configured
- kustomize (or kubectl with built-in kustomize)

## Deployment

### Using Kustomize

Deploy to development environment:
```bash
kubectl apply -k overlays/dev
```

Deploy to staging environment:
```bash
kubectl apply -k overlays/staging
```

Deploy to production environment:
```bash
kubectl apply -k overlays/production
```

### View Generated Manifests

Preview what will be deployed:
```bash
kubectl kustomize overlays/dev
```

## Environment Differences

| Feature | Dev | Staging | Production |
|---------|-----|---------|------------|
| Replicas | 1 | 2 | 3 |
| Resource Limits | Low | Medium | High |
| Storage Size | 1-2 Gi | 5-10 Gi | 50-100 Gi |
| HPA | No | No | Yes |
| PDB | No | No | Yes |
| Network Policies | No | No | Yes |
| Log Level | DEBUG | INFO | WARN |

## Services Overview

### Infrastructure Services
- **Discovery Service (Eureka)**: Service registry at port 8761
- **Config Service**: Centralized configuration at port 8888
- **API Gateway**: Entry point at port 8080

### Business Services
- **Catalog Service**: Menu management (8084) - MongoDB
- **Order Service**: Order processing (8081) - PostgreSQL
- **Payment Service**: Payment processing (8082) - PostgreSQL
- **Kitchen Service**: Kitchen operations (8083) - PostgreSQL
- **Inventory Service**: Stock management (8085) - PostgreSQL
- **Booking Service**: Table reservations (8086) - PostgreSQL
- **Delivery Service**: Order delivery (8087) - PostgreSQL
- **Notification Service**: Alerts & notifications (8088) - PostgreSQL

### External Dependencies
- **PostgreSQL**: Primary relational database
- **MongoDB**: Document store for catalog
- **Redis**: Caching and rate limiting
- **Kafka**: Event streaming
- **Keycloak**: Identity and access management

## Monitoring

All services expose Prometheus metrics at `/actuator/prometheus`.

Prometheus scraping is configured via pod annotations:
```yaml
annotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "<service-port>"
  prometheus.io/path: "/actuator/prometheus"
```

## Secrets Management

For production, replace the base64-encoded secrets with proper secret management:
- HashiCorp Vault
- AWS Secrets Manager
- Azure Key Vault
- Kubernetes External Secrets
