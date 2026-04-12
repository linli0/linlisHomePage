# Backend Documentation

## 📋 Table of Contents

- [API Reference](api-reference.md)
- [Authentication](authentication.md)
- [Gold Price Service](gold-price-service.md)
- [Article Management](article-management.md)
- [Utility Tools](utility-tools.md)
- [AI Integration](ai-integration.md)
- [Database Schema](database-schema.md)
- [Configuration](configuration.md)

## 🏗️ Architecture Overview

The backend is built with **Spring Boot 3.2** and follows a layered architecture pattern:

- **Controller Layer**: REST API endpoints with request/response validation
- **Service Layer**: Business logic with transaction management  
- **Repository Layer**: Data access with Spring Data JPA
- **Entity Layer**: JPA entities representing database tables
- **Security Layer**: JWT-based authentication and authorization

## 🔧 Build and Run

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8 (for production) or H2 (development)

### Development
```bash
cd backend
mvn spring-boot:run
```

### Production Build
```bash
cd backend
mvn clean package -DskipTests
java -jar target/homepage-backend-1.0.0.jar
```

### Environment Variables
- `JWT_SECRET`: JWT signing key (required in production)
- `METALPRICE_API_KEY`: MetalpriceAPI key for gold price data
- `METALPRICE_ENABLED`: Enable/disable gold price updates (`true`/`false`)
- `METALPRICE_SERVER`: MetalpriceAPI server region (`us`/`eu`)

## 🧪 Testing

The backend includes comprehensive test coverage:

- **Unit Tests**: Service layer logic with Mockito
- **Integration Tests**: Controller endpoints with MockMvc
- **Repository Tests**: Database interactions with Testcontainers
- **Security Tests**: JWT validation and endpoint permissions

Run tests with:
```bash
mvn test
```

## 📊 Monitoring

- **Logging**: Structured JSON logging with request/response correlation
- **Health Checks**: `/actuator/health` endpoint for service status
- **Metrics**: Built-in Spring Boot Actuator metrics

## 🚨 Error Handling

All API responses follow a consistent error format:

```json
{
  "code": 400,
  "message": "Error description",
  "data": null
}
```

Common HTTP status codes:
- `200`: Success
- `400`: Bad Request (validation errors)
- `401`: Unauthorized (invalid/missing token)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found (resource doesn't exist)
- `500`: Internal Server Error