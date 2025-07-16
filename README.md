# FX Service API

A Spring Boot-based foreign exchange microservice with real-time conversion rates via Fixer.io.

---

## 🚀 Features

- 💱 Real-time exchange rates from [Fixer.io](https://fixer.io)
- 🗃️ Automatic in-memory caching of exchange rates using Caffeine
- 🔐 Optional JWT-based authentication
- 📂 Bulk CSV file upload for conversions
- 📜 Swagger API documentation
- 🧪 Unit and integration tests
- 🐳 Docker support
- 🔧 Toggle authentication on/off via config
- 🧾 Structured logging

---

## 🛠️ Requirements

- Java 17+ (Java 21 compatible)
- Maven
- (Optional) Docker

---

## ⚙️ Running the Application

```bash
# Build
mvn clean install

# Run
java -jar target/*.jar
```

---

## 🔐 Toggle Authentication

In `application.yml`:

```yaml
fx:
  security:
    enabled: true  # set to false to disable authentication
```

- When `enabled: true`: all endpoints require a valid JWT token
- When `enabled: false`: all endpoints are open (useful for local dev/test)

Use `/auth/login` with username `user` and password `pass` to get a JWT token.
- When security is enabled, obtain a JWT by sending a POST request to /auth/login with valid credentials,
- then include the token in the Authorization header as Bearer <token> for all secured API calls.

---

## 🔑 Fixer.io API Setup

1. Sign up at [Fixer.io](https://apilayer.com/marketplace/fixer-api)
2. Add your API key to `application.yml`:

```yaml
fixer:
  url: https://api.apilayer.com/fixer/latest
  key: your_api_key_here
```

---

## 🗃️ Caching Strategy (Exchange Rate Caching)

To reduce API calls to Fixer.io and improve performance, FX rates are cached in memory using **Caffeine**:

- **Cache name:** `exchangeRates`
- **Expiry:** 1 minute after write
- **Eviction:** Least Recently Used (up to 1000 entries)

To customize cache behavior, see `CacheConfig.java`.  
You can clear cache manually  by restarting the application.

---

## 📮 Postman Collection

Import `src/main/resources/sample/FX-Service-Postman-Collection.json` to test the following endpoints:
Import `src/main/resources/sample/sample_bulk.csv` to test the /api/bulk end point.

| Method | Endpoint             | Description                         |
|--------|----------------------|-------------------------------------|
| GET    | `/api/rate`          | Get FX rate (`from`, `to` query params) |
| POST   | `/api/convert`       | Convert currency (JSON body)        |
| GET    | `/api/history`       | Fetch conversion history (by ID or date) |
| POST   | `/api/bulk`          | Upload CSV for batch conversion     |
| POST   | `/auth/login`        | Get JWT (if auth is enabled)        |

---

## 🔎 Access Swagger UI

To explore the API interactively:

📍 `http://localhost:8080/swagger-ui/index.html`

All endpoints are documented with sample inputs and outputs.

---

## 🧪 Running Tests

```bash
mvn test
```

Unit tests cover:
- Rate fetching (with cache mocking)
- Currency conversion
- File parsing and error handling
- History queries

---

## 🐳 Docker

```bash
docker build -t fx-service .
docker run -p 8080:8080 fx-service
```

---

## 🧾 Logs

Logs follow SLF4J format with output to:
```
logs/fx-service.log
```

Log levels:
- `INFO`: business events (e.g., "Fetched rate", "Conversion saved")
- `WARN`: fallback behavior or skipped rows
- `ERROR`: unexpected conditions (e.g., API failures)

---

## 📫 Contact

Maintainer: Mehmet Vahit ŞENTÜRK  
Email: mehmetvahit@gmail.com