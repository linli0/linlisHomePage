# System Architecture Overview

## 🏗️ High-Level Architecture

CoffeeCookie's HomePage follows a **full-stack monorepo architecture** with clear separation of concerns between frontend and backend layers.

```
coffeeCookie'sHomePage/
├── backend/                # Spring Boot 3.2 + Java 17
│   ├── src/main/java/      # Layered architecture
│   │   ├── controller/     # REST API endpoints (10 controllers)
│   │   ├── service/        # Business logic layer (15 services)  
│   │   ├── repository/     # Data access layer (11 repositories)
│   │   ├── entity/         # JPA entities (12 entities including enums)
│   │   ├── dto/            # Data transfer objects (29 DTOs)
│   │   └── security/       # JWT authentication
│   └── pom.xml            # Maven build configuration
├── frontend/               # Vue 3 + TypeScript + Vite
│   ├── src/
│   │   ├── api/           # API service modules (8 modules)
│   │   ├── components/    # Reusable UI components (8 components)
│   │   ├── views/         # Page components (11 views)
│   │   ├── router/        # Vue Router configuration
│   │   ├── stores/        # Pinia state management (1 store)
│   │   ├── composables/   # Vue composables (1 composable)
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
- **Reactive Programming**: WebFlux
- **WebSocket**: STOMP for real-time communication
- **Build**: Maven 3.8+
- **External APIs**: MetalpriceAPI (gold prices), ExchangeRate-API (currencies), Ollama (AI)
- **QR Code Generation**: ZXing 3.5.2

### Component Counts
- **Controllers**: 10 (AIController, ArticleController, AuthController, CategoryController, GoldPriceController, TagController, ToolController, TradingController, TweetController, XiaomiSpeakerController)
- **Services**: 15 (AIService, ArticleService, AuthService, BacktestingService, CategoryService, GoldPriceService, SignalGenerationService, TagService, TechnicalIndicatorService, ToolService, TruthSocialService, TweetPollingService, TweetWebSocketService, TwitterStreamService, XiaomiSpeakerService)
- **Repositories**: 11 (ArticleRepository, BacktestResultRepository, CategoryRepository, ExchangeRateRepository, GoldPriceRepository, SocialAccountRepository, TagRepository, TradingSignalRepository, TradingStrategyRepository, TweetRepository, UserRepository)
- **Entities**: 12 (Article, BacktestResult, Category, ExchangeRate, GoldPrice, SocialAccount, Tag, TradingSignal, TradingStrategy, Tweet, User, enums/Platform)
- **DTOs**: 29 files

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
- **Real-time Tweet Updates**: WebSocket (STOMP) for live tweet streaming
- **Trading Analysis**: Technical indicators and backtesting capabilities
- **QR Code Generation**: ZXing integration for QR code creation

## 🎨 Frontend Architecture

### Technology Stack
- **Framework**: Vue 3.4 + Composition API
- **Language**: TypeScript 5.3 (strict mode)
- **Build**: Vite 5.0
- **State Management**: Pinia 2.1
- **Routing**: Vue Router 4.2
- **Styling**: Tailwind CSS 3.4 + custom design system
- **HTTP Client**: Axios 1.6 with JWT interceptor
- **Professional Charts**: lightweight-charts 5.1.0
- **Technical Analysis**: trading-signals 7.4.3
- **Traditional Charts**: Chart.js 4.4 + vue-chartjs

### Component Architecture
- **Views**: 11 (AIView, ArticleDetailView, ArticlesView, GoldPriceView, HomeView, LoginView, ProfileView, QuantView, RegisterView, ToolsView, TweetsView, XiaomiView)
- **Components**: 8 (AIChat, FooterBar, IndicatorOverlay, KLineChart, NavigationBar, PriceChart, TweetCard, TweetMetricsChart)
- **Stores**: 1 (auth.ts)
- **Composables**: 1 (useTweetWebSocket.ts)
- **API Modules**: 8 (ai.ts, article.ts, auth.ts, goldPrice.ts, tools.ts, tradingApi.ts, tweets.ts, xiaomi.ts)

### Key Patterns
- **Composition API Only**: All components use `<script setup>`
- **TypeScript Strict Mode**: Full type safety with no `any`
- **Lazy Loading**: Route-based code splitting
- **Global Error Handling**: Axios interceptors for API errors
- **Responsive Design**: Mobile-first with Tailwind breakpoints
- **Real-time Communication**: WebSocket integration for live data

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

### Real-time Tweet Flow
1. Backend establishes WebSocket connection to Twitter API
2. Tweet data processed and broadcast via STOMP over WebSocket
3. Frontend connects to `/topic/tweets` using useTweetWebSocket composable
4. Live tweet updates displayed in real-time without polling
5. Tweet metrics chart updates automatically with new data

### Trading Analysis Flow
1. User selects trading pair and timeframe in QuantView
2. Frontend requests technical indicators via tradingApi.ts
3. Backend processes data using TechnicalIndicatorService and trading-signals library
4. Results returned as structured data for KLineChart component
5. Backtesting available through BacktestingService for strategy validation

## 🧪 Testing Strategy

### Backend Testing
- **Unit Tests**: Service layer with Mockito mocking
- **Integration Tests**: Controller layer with MockMvc
- **Repository Tests**: JPA repositories with Testcontainers
- **Security Tests**: JWT validation and endpoint permissions
- **Scheduled Task Tests**: Timer-based functionality
- **WebSocket Tests**: STOMP messaging and real-time communication

### Frontend Testing
- **Unit Tests**: Utilities and composables with Vitest
- **Component Tests**: Views and components with Vue Test Utils
- **Store Tests**: Pinia store actions and getters
- **API Tests**: HTTP modules with MSW mocking
- **E2E Tests**: Critical user flows with Playwright
- **WebSocket Tests**: Real-time communication scenarios

## 🚀 Deployment Architecture

### Local Development
- Backend: `mvn spring-boot:run` (port 8080)
- Frontend: `npm run dev` (port 3000 with proxy to backend)

### Production Deployment
- Frontend built to `frontend/dist/`
- Spring Boot serves static files from `dist/` directory
- Docker Compose for containerized deployment
- Cloudflare Tunnel for public access
- WebSocket support enabled for real-time features

## 🔒 Security Considerations

- **JWT Secret**: Must be configured via environment variable in production
- **CORS**: Configured to allow only frontend origins
- **Input Validation**: DTO validation with Spring validation annotations
- **SQL Injection**: Prevented by JPA parameterized queries
- **XSS Protection**: DOMPurify used for markdown content rendering
- **WebSocket Security**: STOMP authentication and authorization
- **Rate Limiting**: Applied to sensitive endpoints