# 🚀 Flyway Quick Reference - PizzaFlow

## Migration File Naming
```
V{version}__{description}.sql

✅ V1__initial_schema.sql
✅ V2__add_order_notes.sql
✅ V10__add_customer_loyalty_table.sql
❌ V1_initial_schema.sql          (single underscore)
❌ v2__add-notes.sql               (lowercase 'v', hyphens)
❌ 2__add_notes.sql                (missing 'V')
```

## Essential Commands

### Check Migration Status
```bash
mvn flyway:info
```

### Validate Migrations
```bash
mvn flyway:validate
```

### Repair Failed Migration (Dev Only)
```bash
mvn flyway:repair
```

### View Database History
```sql
SELECT version, description, installed_on, success 
FROM flyway_schema_history 
ORDER BY installed_rank;
```

## Configuration (application.yml)
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public
  
  jpa:
    hibernate:
      ddl-auto: validate  # NEVER use 'update' or 'create'
```

## Migration Template
```sql
-- V{N}__{description}.sql
-- Description: [What this migration does]
-- Rollback: [How to undo this change manually if needed]

BEGIN;

-- Your changes here
ALTER TABLE orders ADD COLUMN notes TEXT;

-- Add comments for documentation
COMMENT ON COLUMN orders.notes IS 'Customer special instructions';

COMMIT;
```

## Common Patterns

### Add Column
```sql
ALTER TABLE orders ADD COLUMN notes TEXT;
UPDATE orders SET notes = '' WHERE notes IS NULL;
ALTER TABLE orders ALTER COLUMN notes SET NOT NULL;
```

### Add Index
```sql
CREATE INDEX IF NOT EXISTS idx_orders_customer_id 
ON orders(customer_id);
```

### Add Constraint
```sql
ALTER TABLE orders 
    ADD CONSTRAINT chk_amount_positive 
    CHECK (total_amount > 0);
```

### Create Table
```sql
CREATE TABLE IF NOT EXISTS table_name (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Do's and Don'ts

### ✅ DO
- One change per migration
- Use transactions (BEGIN/COMMIT)
- Add comments
- Test with clean DB
- Include rollback plan in comments
- Use IF NOT EXISTS for idempotent operations

### ❌ DON'T
- Modify existing migrations (after commit)
- Use `hibernate.ddl-auto: update`
- Skip version numbers
- Make manual schema changes in production
- Use `SELECT *` in data migrations
- Forget to test with real data volumes

## Troubleshooting

### Migration Failed in Dev
```bash
# Clean slate approach
docker-compose down -v
docker-compose up -d postgres
mvn spring-boot:run
```

### Check What Failed
```sql
SELECT * FROM flyway_schema_history 
WHERE success = FALSE 
ORDER BY installed_rank DESC;
```

### Service Won't Start (Migration Issue)
```bash
# 1. Check logs for Flyway error
# 2. Fix the migration file
# 3. Drop and recreate database (dev only)
# 4. Restart service
```

## File Locations
```
services/
├── order-service/src/main/resources/db/migration/
├── payment-service/src/main/resources/db/migration/
├── inventory-service/src/main/resources/db/migration/
└── ...
```

## Testing Locally
```bash
# 1. Start infrastructure
.\start-infrastructure.bat

# 2. Navigate to service
cd services/order-service

# 3. Run service (migrations auto-apply)
mvn spring-boot:run

# 4. Check migration status in logs
# Look for: "Successfully applied N migrations"

# 5. Verify in database
docker exec -it pizzaflow-postgres psql -U postgres -d pizzaflow_orders
SELECT * FROM flyway_schema_history;
```

## Quick Links
- 📚 [Full Guide](FLYWAY_MIGRATION_GUIDE.md)
- 🔗 [Flyway Docs](https://flywaydb.org/documentation/)
- 📖 [PostgreSQL Docs](https://www.postgresql.org/docs/current/)

---
**Remember:** All database changes go through Flyway. No exceptions! 🔒
