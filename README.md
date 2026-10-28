# Order Processing Service

A Spring Boot microservice that manages customer orders, publishes domain events asynchronously, and generates PDF invoices.

**Assignment** — Ikaansh Edutech Private Limited | Java Development | Oct 2026

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 + Spring Boot 3.3.5 |
| Database | PostgreSQL 16 (H2 for tests) |
| ORM | Spring Data JPA / Hibernate |
| PDF | Apache PDFBox 3.0.2 |
| CSV | Apache Commons CSV 1.11.0 |
| API Docs | SpringDoc OpenAPI 2.6.0 |
| Build | Maven 3.10 |
| Container | Docker + Docker Compose |

---

## Architecture

```
HTTP Request
    │
    ▼
OrderController / InvoiceController / ExportController
    │
    ▼
OrderService / InvoiceService / CsvExportService
    │                │
    │         ApplicationEventPublisher
    │                │
    │         OrderEventListener  ←── @Async (orderEventExecutor thread pool)
    │
    ▼
OrderRepository / CustomerRepository
    │
    ▼
PostgreSQL (orders, order_items, customers)
```

---

## Getting Started

### Prerequisites
- Java 21
- Maven 3.10+
- PostgreSQL 16 (or Docker)

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `orderdb` | Database name |
| `DB_USERNAME` | `postgres` | DB username |
| `DB_PASSWORD` | *(required)* | DB password — no default |
| `APP_ORDER_TAX_RATE` | `18` | GST percentage applied to orders |

### Run with Docker Compose

```bash
# Set DB password (required)
export DB_PASSWORD=yourpassword

# Build and start both PostgreSQL and the app
docker-compose up --build
```

App will be available at `http://localhost:8080`.

### Run Locally (without Docker)

```bash
cd backend/order-processing-service

# Set required env var
export DB_PASSWORD=yourpassword

# Run
./mvnw spring-boot:run
```

---

## API Reference

Base URL: `http://localhost:8080/api/v1`

### Orders

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/orders` | Create a new order |
| `GET` | `/orders` | List orders (paginated) |
| `GET` | `/orders/{id}` | Get order by ID |
| `PATCH` | `/orders/{id}/status` | Update order status |
| `GET` | `/orders/{id}/invoice` | Download PDF invoice |

### Export & Reporting

| Method | Endpoint | Description | Role |
|---|---|---|---|
| `GET` | `/export/orders/csv` | Download all orders as CSV | ADMIN |
| `GET` | `/export/revenue/csv?date=YYYY-MM-DD` | Download daily revenue CSV grouped by order status | ADMIN |
| `GET` | `/export/revenue/summary?date=YYYY-MM-DD` | Get daily revenue JSON summary grouped by status | ADMIN |
| `POST` | `/export/revenue/trigger?date=YYYY-MM-DD` | Manually trigger nightly revenue CSV export job | ADMIN |

### Notifications (Async Domain Events)

| Method | Endpoint | Description | Role |
|---|---|---|---|
| `GET` | `/notifications` | Get user notifications & unread badge count | All Authenticated |
| `PATCH` | `/notifications/{id}/read` | Mark a specific notification as read | All Authenticated |
| `PATCH` | `/notifications/read-all` | Mark all notifications as read | All Authenticated |

### Nightly Scheduled Job

- **Schedule:** Runs every night at midnight (`00:00:00`), configurable via `app.scheduling.nightly-cron` in `application.properties`.
- **Action:** Groups the preceding day's orders by `OrderStatus` (`PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`), aggregates subtotal, 18% GST tax, and total revenue, and writes the CSV to `exports/revenue-reports/daily-revenue-YYYY-MM-DD.csv`.
- **Manual Trigger:** Admins can trigger the job on demand via `POST /api/v1/export/revenue/trigger` or via the interactive Dashboard UI.

### Swagger UI

Interactive API docs available at: `http://localhost:8080/swagger-ui.html`

---

## Create Order — Request Example

```json
POST /api/v1/orders
{
  "customerName": "Pravin Mali",
  "customerEmail": "pravin@example.com",
  "customerPhone": "9876543210",
  "customerAddress": "123, Pune, Maharashtra",
  "items": [
    { "productName": "Laptop", "quantity": 1, "unitPrice": 50000.00 },
    { "productName": "Mouse",  "quantity": 2, "unitPrice": 500.00 }
  ]
}
```

Response (201 Created):
```json
{
  "id": "3f2a-...",
  "status": "PENDING",
  "customer": { "name": "Pravin Mali", "email": "pravin@example.com", ... },
  "items": [...],
  "subtotal": 51000.00,
  "taxRate": 18,
  "taxAmount": 9180.00,
  "total": 60180.00
}
```

---

## Order Status Transitions

```
PENDING → CONFIRMED → SHIPPED → DELIVERED
    └──────────────────────────────────→ CANCELLED
```

Any transition not shown above returns **422 Unprocessable Entity**.

---

## Running Tests

```bash
cd backend/order-processing-service

# All tests (unit + integration) — uses H2 in-memory DB, no PostgreSQL needed
./mvnw test
```

Tests: **25 tests — 6 unit, 19 integration** — all pass with H2.

---

## Project Structure

```
backend/order-processing-service/
├── src/main/java/com/pravinmali/orderprocessing/
│   ├── config/          # AsyncConfig, OpenApiConfig
│   ├── controller/      # OrderController, InvoiceController, ExportController
│   ├── dto/             # Request and response DTOs (Java records)
│   ├── entity/          # Order, OrderItem, Customer, OrderStatus
│   ├── event/           # Domain events + async listener
│   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│   ├── repository/      # OrderRepository, CustomerRepository
│   └── service/         # OrderService, InvoiceService, CsvExportService
└── src/test/
    ├── OrderServiceUnitTest.java       # Unit tests (Mockito, no Spring)
    └── OrderControllerIntegrationTest.java  # Full-stack tests (H2 + MockMvc)
```
