# PizzaFlow Keycloak Configuration

This directory contains the Keycloak realm configuration for the PizzaFlow pizzeria ecosystem.

## Realm Overview

- **Realm Name**: pizzaflow
- **Keycloak URL**: http://localhost:9090/
- **Admin Console**: http://localhost:9090/admin
- **Admin Credentials**: admin / admin

## Roles

| Role | Description |
|------|-------------|
| `CUSTOMER` | Standard customer role for placing orders |
| `KITCHEN_STAFF` | Kitchen staff role for managing order preparation |
| `COURIER` | Delivery courier role for order delivery |
| `RESTAURANT_MANAGER` | Restaurant manager with elevated permissions (includes KITCHEN_STAFF) |
| `SYSTEM_ADMIN` | Full system access (includes all other roles) |

## Test Users

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | SYSTEM_ADMIN |
| manager | manager123 | RESTAURANT_MANAGER |
| kitchen | kitchen123 | KITCHEN_STAFF |
| courier | courier123 | COURIER |
| customer | customer123 | CUSTOMER |

## Clients

### pizzaflow-api (Bearer-only)
- **Type**: Confidential client for backend services
- **Client Secret**: pizzaflow-api-secret
- **Authorization**: Enabled with resource-based permissions
- **Use Case**: Service-to-service authentication

### pizzaflow-web (Public)
- **Type**: Public client for web application
- **Root URL**: http://localhost:4200
- **PKCE**: Enabled (S256)
- **Use Case**: Customer-facing web application

### pizzaflow-mobile (Public)
- **Type**: Public client for mobile application
- **Redirect URI**: pizzaflow://callback/*
- **PKCE**: Enabled (S256)
- **Use Case**: iOS and Android mobile apps

### pizzaflow-kds (Public)
- **Type**: Public client for Kitchen Display System
- **Root URL**: http://localhost:4201
- **PKCE**: Enabled (S256)
- **Use Case**: Kitchen Display System web app

## Authorization Resources

| Resource | URI Pattern | Scopes |
|----------|-------------|--------|
| Orders | /api/orders/* | create, read, update, delete |
| Menu | /api/catalog/* | read, manage |
| Inventory | /api/inventory/* | read, manage |
| Kitchen | /api/kitchen/* | read, update |
| Delivery | /api/delivery/* | read, update |

## Getting Started

### Auto-import (Docker Compose)
The realm is automatically imported when starting Keycloak with Docker Compose:

```bash
cd infrastructure/docker
docker-compose up keycloak
```

### Manual Import
If you need to manually import the realm:

1. Access Keycloak Admin Console at http://localhost:9090/admin
2. Login with admin / admin
3. Click "Create Realm"
4. Click "Browse" and select `pizzaflow-realm.json`
5. Click "Create"

## Getting Tokens

### Using Direct Access Grant (Password Flow)

```bash
# Get token for customer user
curl -X POST "http://localhost:9090/realms/pizzaflow/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=pizzaflow-web" \
  -d "username=customer" \
  -d "password=customer123"
```

### Using Authorization Code Flow with PKCE

For web/mobile applications, use the standard OAuth 2.0 Authorization Code flow with PKCE:

1. Generate code verifier and code challenge
2. Redirect to authorization endpoint:
   ```
   http://localhost:9090/realms/pizzaflow/protocol/openid-connect/auth
   ?client_id=pizzaflow-web
   &response_type=code
   &redirect_uri=http://localhost:4200/callback
   &scope=openid profile email
   &code_challenge=<code_challenge>
   &code_challenge_method=S256
   ```
3. Exchange authorization code for tokens at token endpoint

## Token Information

- **Access Token Lifetime**: 5 minutes (300 seconds)
- **SSO Session Idle**: 30 minutes
- **SSO Session Max**: 10 hours
- **Offline Session Idle**: 30 days

## Spring Security Integration

Add to your Spring Boot service's `application.yml`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9090/realms/pizzaflow
          jwk-set-uri: http://localhost:9090/realms/pizzaflow/protocol/openid-connect/certs
```

Example controller with role-based access:

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_MANAGER', 'SYSTEM_ADMIN')")
    public List<OrderDTO> getOrders(Authentication auth) {
        // ...
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderDTO createOrder(@RequestBody CreateOrderRequest request) {
        // ...
    }
}
```

## Customization

To modify the realm configuration:

1. Make changes in Keycloak Admin Console
2. Export the realm: Realm Settings → Action → Partial Export
3. Replace `pizzaflow-realm.json` with the exported file
4. Restart Keycloak to apply changes
