# RautKart

An online grocery store — the My kirana shop, online. Built as a full-stack
portfolio project: React storefront, Spring Boot API, PostgreSQL, JWT auth, and a real
admin panel for products, stock and orders.

> Payments run against **Razorpay test mode**. No real money moves. If no Razorpay keys
> are configured the backend falls back to a mock-payment mode so the whole checkout flow
> still works on a fresh clone.

## Stack

| Layer | Choice |
|---|---|
| Frontend | React 18 + Vite + Tailwind CSS v4 + React Router |
| Backend | Java 17 + Spring Boot 3.3 (Web, Data JPA, Security, Validation) |
| Database | PostgreSQL 18 |
| Auth | JWT (JJWT), separate customer and admin logins |
| Payments | Razorpay Java SDK, test mode |
| Build | Maven (backend), npm (frontend) |

## Architecture note: thin client, thick backend

Business logic lives in Spring Boot, not React. The frontend renders what the API hands it
and does not recompute anything:

- cart line totals, subtotal, delivery fee and grand total → `CartService`
- "add ₹X more for free delivery" → `CartResponse.amountForFreeDelivery`
- discount percentage and the stock line ("Only 4 left in stock") → `Mappers.toProduct`
- stock validation, order numbering, status transitions, restock-on-cancel → `OrderService`

This keeps the two clients consistent and means pricing rules change in one place.

## Running it locally

### 1. Database

```sql
CREATE DATABASE rautkart;
```

### 2. Backend

```bash
cd backend
# password for the postgres user
$env:DB_PASSWORD = "your-password"      # PowerShell
mvn spring-boot:run
```

Runs on <http://localhost:8080>. Schema is created by Hibernate (`ddl-auto=update`) and
`DataSeeder` fills in 10 categories, ~55 products and two demo accounts on first boot.
It is idempotent — restarting never duplicates rows.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on <http://localhost:5173>. Vite proxies `/api` to port 8080, so the browser only
ever talks to one origin.

## Demo accounts

| Role | Email | Password |
|---|---|---|
| Admin | `admin@rautkart.in` | `admin123` |
| Customer | `customer@rautkart.in` | `customer123` |

Admin panel lives at `/admin` (sign in via `/admin/login`).

## Configuration

Everything has a working default; override with environment variables.

| Variable | Default | Purpose |
|---|---|---|
| `DB_PASSWORD` | `postgres` | PostgreSQL password |
| `JWT_SECRET` | dev value | Base64 256-bit HMAC key — **change for anything real** |
| `RAZORPAY_KEY_ID` | *(empty)* | Test key id; empty enables mock payments |
| `RAZORPAY_KEY_SECRET` | *(empty)* | Test key secret |

Store rules live in `application.properties`: `rautkart.delivery-fee` (₹30) and
`rautkart.free-delivery-above` (₹500).

## API surface

**Public**
```
POST   /api/auth/register        POST /api/auth/login       POST /api/auth/admin/login
GET    /api/categories
GET    /api/products?q=&category=&maxPrice=&inStockOnly=&sort=&page=&size=
GET    /api/products/featured    GET  /api/products/{slug}
```

**Customer (JWT)**
```
GET    /api/auth/me
GET    /api/cart                 POST /api/cart/items
PUT    /api/cart/items/{id}      DELETE /api/cart/items/{id}     DELETE /api/cart
GET    /api/addresses            POST /api/addresses
PUT    /api/addresses/{id}       DELETE /api/addresses/{id}
POST   /api/orders               POST /api/orders/payment/verify
GET    /api/orders               GET  /api/orders/{id}           POST /api/orders/{id}/cancel
```

**Admin (JWT + ROLE_ADMIN)**
```
GET    /api/admin/dashboard
GET    /api/admin/products       POST /api/admin/products
PUT    /api/admin/products/{id}  DELETE /api/admin/products/{id}   (soft delete)
POST   /api/admin/categories
GET    /api/admin/orders?status= PATCH /api/admin/orders/{id}/status
```

## Domain notes

- **Cart is server-side**, keyed by user — it survives refreshes and follows the customer
  across devices.
- **Addresses are snapshotted onto the order** at checkout, so editing a saved address
  never rewrites the history of a placed order.
- **Products are soft-deleted** (`active = false`) because historical orders reference them.
- **Stock is reserved at checkout** and returned if the order is cancelled by either the
  customer or an admin.
