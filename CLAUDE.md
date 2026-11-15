# Backend context

Java 21, Spring Boot 4.1.0, Hibernate/JPA and PostgreSQL 17. This is the Java backend, not an Entity Framework application. Read `README.md`, `docs/API.md`, and `docs/DATABASE.md` before changing persistence or client contracts.

Keep the existing bounded contexts and `domain/application/infrastructure/interfaces` organization. Domain aggregates enforce stock, promotion, payment and order invariants. Cross-context operations use public application ports: `Catalog`, `ProductUsage`, `Inventory`, `Promotions`, `Payments`, `CustomerAccounts`, `OperatorAccounts`, `GoogleIdentityTokens`, and `StoreAccess`. Application code must not import REST resources or another context's repositories. SKU is the published language; sellable SKUs include their store suffix. Legacy five-part identifiers are retained for historical database references.

Keep `spring.jpa.hibernate.ddl-auto=validate`. Never use `create`, `update`, or `create-drop` against a working database. SQL migrations are manual, versioned and transactional; do not rewrite an applied migration. `db/baseline` is only for a new empty database. Stock moved to `inventory_stock` through migration 001; old warehouse/store stock tables are retained as legacy and are not maintained by the new application.

Checkout reserves inventory and promotion capacity. Payment consumes those reservations, increments sales and awards decimal points once in a transaction. Pending cancellations and expiry release reservations. Paid points are never reversed. POS orders are paid/delivered at creation, grant no operator points and use locked per-store receipt counters. Negotiated prices require a reason; automatic promotions do not. A static merchant QR is configured externally and payments are reviewed manually; do not fabricate banking confirmations or carrier tracking events.

No embedded production passwords or JWT secret. Keep `application-local.properties` ignored. Google sign-in verifies an ID token with the official verifier and never links an existing privileged/email account solely by matching email.

Use `scripts/Test-Backend.ps1` for full tests. It creates and stops an isolated PostgreSQL instance with synthetic migration fixtures. Plain Maven tests skip integration tests unless the isolated test URL is explicitly configured. Preserve local uncommitted work. Do not run database upgrades or deploy automatically.

Git workflow: work directly on develop and use Conventional Commits. Do not create branches unless the user explicitly changes this preference. Never force-push develop. Push to origin/develop when requested by the user.
