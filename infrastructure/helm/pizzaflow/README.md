# PizzaFlow Helm Chart

A Helm chart for deploying the PizzaFlow microservices platform to Kubernetes.

## Prerequisites

- Kubernetes 1.28+
- Helm 3.13+
- PV provisioner support (for persistent volumes)

## Installation

### Quick Start (Development)

```bash
# Add Bitnami repo for dependencies
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Install with default values (development)
helm install pizzaflow ./infrastructure/helm/pizzaflow
```

### Production Deployment

```bash
# Install with production values
helm install pizzaflow ./infrastructure/helm/pizzaflow \
  --namespace pizzaflow-prod \
  --create-namespace \
  -f ./infrastructure/helm/pizzaflow/values-production.yaml
```

### Upgrade

```bash
helm upgrade pizzaflow ./infrastructure/helm/pizzaflow \
  --namespace pizzaflow-prod \
  -f ./infrastructure/helm/pizzaflow/values-production.yaml
```

### Rollback

```bash
# View history
helm history pizzaflow -n pizzaflow-prod

# Rollback to previous revision
helm rollback pizzaflow -n pizzaflow-prod

# Rollback to specific revision
helm rollback pizzaflow 2 -n pizzaflow-prod
```

### Uninstall

```bash
helm uninstall pizzaflow -n pizzaflow-prod
```

## Configuration

### Global Settings

| Parameter | Description | Default |
|-----------|-------------|---------|
| `global.imageRegistry` | Global image registry | `""` |
| `global.imagePullSecrets` | Global image pull secrets | `[]` |
| `global.environment` | Environment (dev/staging/prod) | `dev` |

### Service Configuration

Each service supports the following parameters:

| Parameter | Description | Default |
|-----------|-------------|---------|
| `<service>.enabled` | Enable the service | `true` |
| `<service>.replicaCount` | Number of replicas | `2` |
| `<service>.image.repository` | Image repository | `pizzaflow/<service>` |
| `<service>.image.tag` | Image tag | `latest` |
| `<service>.service.type` | Service type | `ClusterIP` |
| `<service>.service.port` | Service port | varies |
| `<service>.resources` | CPU/Memory resources | see values.yaml |

### Infrastructure Dependencies

The chart includes Bitnami subcharts for:

- **PostgreSQL**: Primary database
- **MongoDB**: Document store for catalog
- **Redis**: Caching layer
- **Kafka**: Event streaming
- **Keycloak**: Identity management (optional)

Enable/disable with:

```yaml
postgresql:
  enabled: true
mongodb:
  enabled: true
redis:
  enabled: true
kafka:
  enabled: true
keycloak:
  enabled: false
```

### Autoscaling

Enable HPA for business services:

```yaml
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
```

### Ingress

Configure API Gateway ingress:

```yaml
apiGateway:
  ingress:
    enabled: true
    className: nginx
    hosts:
      - host: api.pizzaflow.com
        paths:
          - path: /
            pathType: Prefix
    tls:
      - secretName: pizzaflow-tls
        hosts:
          - api.pizzaflow.com
```

## Environment-Specific Values

### Development
```bash
helm install pizzaflow . \
  --set global.environment=dev \
  --set autoscaling.enabled=false
```

### Staging
```bash
helm install pizzaflow . \
  --set global.environment=staging \
  --set autoscaling.enabled=true \
  --set autoscaling.minReplicas=2
```

### Production
```bash
helm install pizzaflow . \
  -f values-production.yaml
```

## Monitoring

All services expose Prometheus metrics. Enable ServiceMonitor:

```yaml
monitoring:
  serviceMonitor:
    enabled: true
    interval: 30s
```

## Troubleshooting

### Check pod status
```bash
kubectl get pods -n pizzaflow
```

### View logs
```bash
kubectl logs -f deployment/pizzaflow-order-service -n pizzaflow
```

### Describe pod for events
```bash
kubectl describe pod <pod-name> -n pizzaflow
```

### Test database connectivity
```bash
kubectl exec -it pizzaflow-order-service-xxx -n pizzaflow -- \
  curl -s localhost:8081/actuator/health/db
```

## Services Overview

| Service | Port | Database |
|---------|------|----------|
| Discovery Service | 8761 | - |
| Config Service | 8888 | - |
| API Gateway | 8080 | Redis |
| Catalog Service | 8084 | MongoDB |
| Order Service | 8081 | PostgreSQL |
| Payment Service | 8082 | PostgreSQL |
| Kitchen Service | 8083 | Redis |
| Inventory Service | 8085 | PostgreSQL |
| Booking Service | 8086 | PostgreSQL |
| Delivery Service | 8087 | PostgreSQL |
| Notification Service | 8088 | PostgreSQL |
