# API Testing Guide

## Overview
This directory contains `.rest` files for testing all API endpoints using HTTP clients like VS Code REST Client or IntelliJ IDEA HTTP Client.

---

## Setup

### VS Code (Recommended)
1. Install **REST Client** extension by Huachao Mao
2. Open any `.rest` file
3. Click "Send Request" above each request
4. View response in split pane

### IntelliJ IDEA
1. Built-in HTTP Client (no extension needed)
2. Open any `.rest` file
3. Click green arrow next to each request
4. View response in tool window

### Postman Alternative
While you can use Postman, `.rest` files are:
- ✅ Version controlled with code
- ✅ Easy to share with team
- ✅ No separate app needed
- ✅ Better for CI/CD automation

---

## Available Test Files

### 1. `catalog-service.rest`
**Service:** Catalog Service (MongoDB)  
**Port:** 8084  
**Endpoints:**
- Menu retrieval (full, by category, featured)
- Menu item CRUD operations
- Search functionality
- Cache testing

**Total Requests:** 20+

### 2. `order-service.rest`
**Service:** Order Service (PostgreSQL + Kafka)  
**Port:** 8081  
**Endpoints:**
- Order creation (all types: DELIVERY, PICKUP, DINE_IN, SCHEDULED, HYBRID)
- Order retrieval (by ID, order number, customer)
- Order status updates (complete lifecycle)
- Order cancellation

**Total Requests:** 29+

### 3. `api-gateway.rest`
**Service:** API Gateway  
**Port:** 8080  
**Endpoints:**
- All services via gateway
- Rate limiting tests
- Circuit breaker tests
- Gateway health checks

**Total Requests:** 15+

---

## Quick Start

### 1. Start Infrastructure
```bash
cd infrastructure/docker
docker-compose up -d
```

### 2. Start Services (in order)
```bash
# Terminal 1: Discovery Service
cd services/discovery-service
mvn spring-boot:run

# Terminal 2: Config Service
cd services/config-service
mvn spring-boot:run

# Terminal 3: API Gateway
cd services/api-gateway
mvn spring-boot:run

# Terminal 4: Catalog Service
cd services/catalog-service
mvn spring-boot:run

# Terminal 5: Order Service
cd services/order-service
mvn spring-boot:run
```

### 3. Run Tests
Open `api-tests/catalog-service.rest` and click "Send Request"

---

## File Structure

```
api-tests/
├── README.md                 # This file
├── catalog-service.rest      # Catalog endpoints
├── order-service.rest        # Order endpoints
├── api-gateway.rest          # Gateway tests
├── payment-service.rest      # TODO: Add when implemented
├── kitchen-service.rest      # TODO: Add when implemented
└── inventory-service.rest    # TODO: Add when implemented
```

---

## Best Practices

### ✅ DO
1. **Always add .rest files for new endpoints**
2. Use variables for base URLs and IDs
3. Include comments explaining request purpose
4. Group related requests with section headers
5. Add error case tests (404, 400, etc.)
6. Include example request/response bodies
7. Update this README when adding new files

### ❌ DON'T
1. Hardcode sensitive data (API keys, passwords)
2. Use production URLs in test files
3. Skip validation/error tests
4. Forget to document variables

---

## Variables

### Global Variables (defined in each file)
```http
@baseUrl = http://localhost:8084/api/v1/catalog
@gatewayUrl = http://localhost:8080/api/v1/catalog
@restaurantId = RESTAURANT-001
@customerId = 1
```

### Using Variables
```http
GET {{baseUrl}}/menu/{{restaurantId}}
```

### Dynamic Variables (VS Code REST Client)
```http
@orderId = {{createOrder.response.body.data.id}}
GET {{baseUrl}}/orders/{{orderId}}
```

---

## Testing Scenarios

### Basic CRUD Flow
1. Create resource (POST)
2. Read resource (GET by ID)
3. Update resource (PUT/PATCH)
4. Delete resource (DELETE)
5. Verify deletion (GET should return 404)

### Complete Business Flow Example

#### Order Lifecycle Test (order-service.rest)
```http
# 1. Create order
POST {{baseUrl}}
{ "customerId": 1, "items": [...] }

# 2. Confirm order
PATCH {{baseUrl}}/1/status?status=CONFIRMED

# 3. Start preparing
PATCH {{baseUrl}}/1/status?status=PREPARING

# 4. Mark ready
PATCH {{baseUrl}}/1/status?status=READY

# 5. Out for delivery
PATCH {{baseUrl}}/1/status?status=OUT_FOR_DELIVERY

# 6. Delivered
PATCH {{baseUrl}}/1/status?status=DELIVERED
```

### Error Testing
```http
# Test validation errors
POST {{baseUrl}}/orders
{
  "customerId": null,  # Invalid
  "items": []          # Invalid
}

# Test 404
GET {{baseUrl}}/orders/99999

# Test invalid enum
PATCH {{baseUrl}}/orders/1/status?status=INVALID
```

---

## Adding New Endpoints

### When you implement a new endpoint, follow this checklist:

1. **Create/Update .rest file**
   ```http
   ### New Endpoint Description
   POST {{baseUrl}}/new-endpoint
   Content-Type: application/json
   
   {
     "field": "value"
   }
   ```

2. **Add success case test**
3. **Add error case tests (400, 404, 500)**
4. **Add authentication test (if secured)**
5. **Document expected response**
6. **Update this README if new file created**

### Template for New Endpoint
```http
###############################################
# Feature Name
###############################################

### 1. Success Case - Description
POST {{baseUrl}}/endpoint
Content-Type: application/json

{
  "exampleField": "value"
}

### 2. Error Case - Validation
POST {{baseUrl}}/endpoint
Content-Type: application/json

{
  "exampleField": null
}

### 3. Error Case - Not Found
GET {{baseUrl}}/endpoint/99999

### 4. Error Case - Unauthorized (if applicable)
GET {{baseUrl}}/endpoint
# Remove Authorization header
```

---

## Troubleshooting

### Connection Refused
**Problem:** `Connection refused on localhost:8084`  
**Solution:**
1. Check service is running: `curl http://localhost:8084/actuator/health`
2. Check logs for startup errors
3. Verify port isn't already in use: `netstat -ano | findstr :8084`

### 404 Not Found
**Problem:** Service running but endpoint returns 404  
**Solution:**
1. Verify correct base path: `/api/v1/catalog` not `/catalog`
2. Check controller `@RequestMapping` annotations
3. Verify service registered with Eureka (if using gateway)

### Empty Response
**Problem:** 200 OK but no data  
**Solution:**
1. Check database has data
2. Run data initializer (catalog-service auto-loads on startup)
3. Check service logs for errors

### Rate Limiting (429 Too Many Requests)
**Problem:** Gateway returns 429 after multiple requests  
**Solution:**
1. Expected behavior - rate limiter working
2. Wait a few seconds and retry
3. Increase rate limit in `api-gateway/application.yml` for testing

### Circuit Breaker Open (503 Service Unavailable)
**Problem:** Gateway returns 503 with fallback message  
**Solution:**
1. Backend service is down or slow
2. Circuit breaker opened after multiple failures
3. Wait 10 seconds for circuit to half-open
4. Fix backend service and retry

---

## Integration with CI/CD

### Running Tests in Pipeline

```bash
# Using newman (Postman CLI) with .rest files
npm install -g newman

# Convert .rest to Postman collection (manual step)
# Then run: newman run catalog-service.postman_collection.json

# Or use httpyac (CLI for .rest files)
npm install -g httpyac

httpyac send api-tests/catalog-service.rest --all
```

### GitHub Actions Example
```yaml
- name: Run API Tests
  run: |
    npx httpyac send api-tests/catalog-service.rest --all
    npx httpyac send api-tests/order-service.rest --all
```

---

## Performance Testing

### Load Testing with .rest files

```bash
# Using httpyac with iterations
httpyac send catalog-service.rest --repeat 100

# Or use Apache Bench
ab -n 1000 -c 10 http://localhost:8084/api/v1/catalog/menu/RESTAURANT-001
```

### Caching Verification
Run the same request multiple times and observe response times:
- First request: ~50-100ms (database hit)
- Subsequent requests: ~5-10ms (Redis cache hit)

---

## Security Testing

### JWT Token Testing (when Keycloak configured)

```http
### Get Access Token
POST http://localhost:9090/realms/pizzaflow/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
&client_id=pizzaflow-client
&username=testuser
&password=testpass

### Use Token in Request
GET {{baseUrl}}/secure-endpoint
Authorization: Bearer {{token}}
```

---

## Monitoring Requests

### View Logs
```bash
# Service logs
tail -f services/catalog-service/logs/application.log

# Gateway logs
tail -f services/api-gateway/logs/application.log

# Kafka events (for order-service)
docker exec -it pizzaflow-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order.created \
  --from-beginning
```

### Prometheus Metrics
```http
GET http://localhost:8084/actuator/prometheus
```

---

## Summary

✅ **Always** add `.rest` files when creating new endpoints  
✅ **Test** success and error cases  
✅ **Document** variables and expected responses  
✅ **Version control** all test files  
✅ **Update** this README when adding new test files  

**This practice ensures:**
- Easy API testing without external tools
- Self-documenting API endpoints
- Quick onboarding for new developers
- Automated testing capabilities
- Consistent testing across team
