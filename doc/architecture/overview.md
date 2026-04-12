# System Architecture Overview

## 🏗️ High-Level Architecture

CoffeeCookie's HomePage follows a **full-stack monorepo architecture** with clear separation of concerns between frontend and backend layers.

```
coffeeCookie'sHomePage/
├── backend/                # Spring Boot 3.2 + Java 17
│   ├── src/main/java/      # Layered architecture
│   │   ├── controller/     # REST API endpoints
│   │   ├── service/        # Business logic layer  
│   │   ├── repository/     # Data access layer
│   │   ├── entity/         # JPA entities
│   │   ├── dto/            # Data transfer objects
│   │   └── security/       # JWT authentication
│   └── pom.xml            # Maven build configuration
├── frontend/               # Vue 3 + TypeScript + Vite
│   ├── src/
│   │   ├── api/           # API service modules
│   │   ├── components/    # Reusable UI components
│   │   ├── views/         # Page components
│   │   ├── router/        # Vue Router configuration
│   │   ├── stores/        # Pinia state management
│   │   └── utils/         # Utility functions
│   └── package.json       # npm dependencies
└── doc/                   # Centralized documentation (this directory)
```

## 🔌 Backend Architecture

### Technology Stack
- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Database**: H2 (development) / MySQL 8 (production)
- **ORM**: Spring Data JPA + Hibernate
- **Security**: Spring Security + JWT (JJWT 0.12.3)
- **Build**: Maven 3.8+
- **External APIs**: MetalpriceAPI (gold prices), ExchangeRate-API (currencies), Ollama (AI)

### Layered Architecture
1. **Controller Layer**: REST API endpoints with DTO validation
2. **Service Layer**: Business logic with transaction management
3. **Repository Layer**: Data access with Spring Data JPA
4. **Entity Layer**: JPA entities with relationships
5. **Security Layer**: JWT token generation/validation and authentication filters

### Key Features
- **Scheduled Tasks**: Automatic gold price updates every minute
- **Exchange Rate Updates**: Currency conversion rates updated hourly
- **Role-Based Access**: ADMIN vs USER role permissions
- **Password-Only Login**: Simplified authentication flow
- **Streaming AI**: Server-Sent Events for real-time AI responses

## 🎨 Frontend Architecture

### Technology Stack
- **Framework**: Vue 3.4 + Composition API
- **Language**: TypeScript 5.3 (strict mode)
- **Build**: Vite 5.0
- **State Management**: Pinia 2.1
- **Routing**: Vue Router 4.2
- **Styling**: Tailwind CSS 3.4 + custom design system
- **HTTP Client**: Axios 1.6 with JWT interceptor
- **Charts**: Chart.js 4.4 + vue-chartjs

### Component Architecture
1. **Views**: Page-level components (9 total)
2. **Components**: Reusable UI components (4 total)
3. **Stores**: Pinia store for authentication state
4. **API Modules**: Feature-based API service modules (5 total)
5. **Utilities**: Shared utility functions and HTTP client

### Key Patterns
- **Composition API Only**: All components use `<script setup>`
- **TypeScript Strict Mode**: Full type safety with no `any`
- **Lazy Loading**: Route-based code splitting
- **Global Error Handling**: Axios interceptors for API errors
- **Responsive Design**: Mobile-first with Tailwind breakpoints

## 🔄 Data Flow

### Authentication Flow
1. User enters password on login page
2. Frontend sends POST `/api/auth/login` with password
3. Backend validates credentials, generates JWT token
4. Frontend stores token in localStorage
5. Subsequent requests include `Authorization: Bearer <token>` header
6. Backend validates token via AuthTokenFilter

### Gold Price Flow
1. Backend scheduled task calls MetalpriceAPI every minute
2. Price data stored in GoldPrice entity with timestamp
3. Frontend calls GET `/api/gold-price/current` on page load
4. Real-time updates via setInterval every 60 seconds
5. Historical data available via GET `/api/gold-price/history`

### Article Flow
1. Admin creates articles via POST `/api/articles` (ADMIN only)
2. Articles stored with published flag, categories, and tags
3. Public users can view published articles via GET `/api/articles/public/list`
4. View counts tracked on article access

## 🧪 Testing Strategy

### Backend Testing
- **Unit Tests**: Service layer with Mockito mocking
- **Integration Tests**: Controller layer with MockMvc
- **Repository Tests**: JPA repositories with Testcontainers
- **Security Tests**: JWT validation and endpoint permissions
- **Scheduled Task Tests**: Timer-based functionality

### Frontend Testing
- **Unit Tests**: Utilities and composables with Vitest
- **Component Tests**: Views and components with Vue Test Utils
- **Store Tests**: Pinia store actions and getters
- **E2E Tests**: Critical user flows with Playwright

## 🚀 Deployment Architecture

### Local Development
- Backend: `mvn spring-boot:run` (port 8080)
- Frontend: `npm run dev` (port 3000 with proxy to backend)

### Production Deployment
- Frontend built to `frontend/dist/`
- Spring Boot serves static files from `dist/` directory
- Docker Compose for containerized deployment
- Cloudflare Tunnel for public access

## 🔒 Security Considerations

- **JWT Secret**: Must be configured via environment variable in production
- **CORS**: Configured to allow only frontend origins
- **Input Validation**: DTO validation with Spring validation annotations
- **SQL Injection**: Prevented by JPA parameterized queries
- **XSS Protection**: DOMPurify used for markdown content rendering