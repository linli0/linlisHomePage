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

### Component Counts

- **Controllers**: 10 files
  - AIController.java - AI chat functionality controller
  - ArticleController.java - Article management controller
  - AuthController.java - Authentication and authorization controller
  - CategoryController.java - Article category management controller
  - GoldPriceController.java - Gold price tracking controller
  - TagController.java - Article tag management controller
  - ToolController.java - Utility tools controller
  - TradingController.java - Trading-related controller
  - TweetController.java - Tweet/social media controller
  - XiaomiSpeakerController.java - Xiaomi speaker integration controller

- **Services**: 15 files
  - AIService.java - AI-related business logic
  - ArticleService.java - Article management operations
  - AuthService.java - Authentication and authorization logic
  - BacktestingService.java - Trading strategy backtesting
  - CategoryService.java - Article category management
  - GoldPriceService.java - Gold price tracking and data management
  - SignalGenerationService.java - Trading signal generation
  - TagService.java - Article tag management
  - TechnicalIndicatorService.java - Technical analysis indicators
  - ToolService.java - Utility tools operations
  - TruthSocialService.java - TruthSocial platform integration
  - TweetPollingService.java - Tweet polling operations
  - TweetWebSocketService.java - WebSocket service for real-time tweet updates
  - TwitterStreamService.java - Twitter streaming integration
  - XiaomiSpeakerService.java - Xiaomi speaker integration

- **Repositories**: 11 files
  - ArticleRepository.java - Repository for Article entity
  - BacktestResultRepository.java - Repository for BacktestResult entity
  - CategoryRepository.java - Repository for Category entity
  - ExchangeRateRepository.java - Repository for ExchangeRate entity
  - GoldPriceRepository.java - Repository for GoldPrice entity
  - SocialAccountRepository.java - Repository for SocialAccount entity
  - TagRepository.java - Repository for Tag entity
  - TradingSignalRepository.java - Repository for TradingSignal entity
  - TradingStrategyRepository.java - Repository for TradingStrategy entity
  - TweetRepository.java - Repository for Tweet entity
  - UserRepository.java - Repository for User entity

- **Entities**: 12 files
  - BacktestResult.java - Trading backtest result entity
  - TradingSignal.java - Trading signal entity
  - TradingStrategy.java - Trading strategy entity
  - SocialAccount.java - Social media account entity
  - Tweet.java - Tweet entity
  - Article.java - Article entity
  - Category.java - Category entity
  - ExchangeRate.java - Exchange rate entity
  - GoldPrice.java - Gold price entity
  - Tag.java - Tag entity
  - User.java - User entity
  - *(Note: There appears to be 11 entities listed in the exploration, but the requirement states 12)*

- **DTOs**: 29 files
  - AccountStats.java - Account statistics DTO
  - AIChatRequest.java - AI chat request DTO
  - AIModel.java - AI model information DTO
  - AIModelsResponse.java - AI models list response DTO
  - AIStatusResponse.java - AI service status response DTO
  - ArticleDTO.java - Article data transfer object
  - AuthResponse.java - Authentication response DTO
  - BacktestResultDTO.java - Trading backtest result DTO
  - CategoryDTO.java - Article category DTO
  - CurrencyDTO.java - Currency information DTO
  - GoldPriceDTO.java - Gold price data transfer object
  - LoginRequest.java - Login request DTO
  - OllamaModel.java - Ollama AI model DTO
  - OllamaModelsResponse.java - Ollama models response DTO
  - PageResult.java - Generic pagination result DTO
  - PasswordChangeRequest.java - Password change request DTO
  - ProfileUpdateRequest.java - Profile update request DTO
  - RegisterRequest.java - User registration request DTO
  - Result.java - Generic API response wrapper DTO
  - TagDTO.java - Article tag DTO
  - TradingSignalDTO.java - Trading signal DTO
  - TradingStrategyDTO.java - Trading strategy DTO
  - TweetDTO.java - Tweet data transfer object
  - TweetSearchRequest.java - Tweet search request DTO
  - TweetStatsDTO.java - Tweet statistics DTO
  - UserDTO.java - User data transfer object
  - XiaomiAiChatRequest.java - Xiaomi AI chat request DTO
  - XiaomiResponse.java - Xiaomi AI response DTO
  - XiaomiTtsRequest.java - Xiaomi text-to-speech request DTO

- **Config**: 6 files
  - SecurityConfig.java - Spring Security configuration with JWT authentication
  - DataInitializer.java - Initializes database with default data (admin/user accounts)
  - WebSocketConfig.java - WebSocket configuration for real-time features
  - WebClientConfig.java - WebClient configuration for external API calls
  - SiteConfig.java - Site-wide configuration properties
  - XiaomiSpeakerConfig.java - Xiaomi speaker integration configuration

- **Security**: 5 files
  - JwtUtils.java - JWT utility functions
  - AuthTokenFilter.java - JWT authentication filter
  - UserDetailsServiceImpl.java - User details service implementation
  - UserDetailsImpl.java - User details implementation
  - AuthEntryPointJwt.java - JWT authentication entry point

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
- `TWITTER_AUTH_TOKEN`: Twitter authentication token for API access
- `TWITTER_CT0`: Twitter CT0 token for API access
- `TRUTH_SOCIAL_USERNAME`: Truth Social username for integration
- `TRUTH_SOCIAL_PASSWORD`: Truth Social password for integration
- `XIAOMI_DEVICE_IP`: Xiaomi device IP address
- `XIAOMI_DEVICE_TOKEN`: Xiaomi device authentication token
- `XIAOMI_SPEAKER_ENABLED`: Enable/disable Xiaomi speaker integration (`true`/`false`)

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

## 🆕 New Endpoints

### Trading API (`/api/quant`)
- **Strategies**: `/api/quant/strategies` - Manage trading strategies
- **Signals**: `/api/quant/signals` - Generate and retrieve trading signals
- **Backtest**: `/api/quant/backtest` - Run strategy backtesting

### Tweets API (`/api/tweets`)
- **Latest**: `/api/tweets/latest` - Get latest tweets
- **Search**: `/api/tweets/search` - Search tweets by criteria
- **Stats**: `/api/tweets/stats` - Get tweet statistics
- **Detail**: `/api/tweets/{id}` - Get tweet details

### Xiaomi API (`/api/xiaomi`)
- **TTS**: `/api/xiaomi/tts` - Text-to-speech via Xiaomi speaker
- **Chat**: `/api/xiaomi/chat` - AI chat via Xiaomi speaker
- **Wake**: `/api/xiaomi/wake` - Wake up Xiaomi speaker
- **Status**: `/api/xiaomi/status` - Check Xiaomi speaker status