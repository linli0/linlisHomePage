# Testing Documentation

## 📋 Table of Contents

- [Test Strategy](test-strategy.md)
- [Backend Testing](backend-testing.md)
- [Frontend Testing](frontend-testing.md)
- [Test Results](test-results.md)
- [CI/CD Integration](ci-cd-integration.md)

## 🎯 Testing Strategy

### Testing Pyramid
Following industry best practices, CoffeeCookie's HomePage implements a comprehensive testing strategy:

| Test Type | Percentage | Purpose | Tools |
|-----------|------------|---------|-------|
| **Unit Tests** | 70% | Pure logic, utilities, services | JUnit 5 (backend), Vitest (frontend) |
| **Integration Tests** | 20% | Component interactions, API contracts | Spring Boot Test (backend), Vue Test Utils (frontend) |
| **E2E Tests** | 10% | Critical user journeys | Playwright |

### Quality Gates
- **Code Coverage**: >80% line coverage for business logic
- **Build Pipeline**: All tests must pass before merge
- **Performance**: Test suite execution time < 5 minutes

## 🔧 Backend Testing Framework

### Technology Stack
- **Test Framework**: JUnit 5 + Spring Boot Test
- **Mocking**: Mockito
- **Assertions**: AssertJ + Hamcrest
- **Integration Testing**: Testcontainers 1.19.7 (MySQL)
- **Coverage Reporting**: JaCoCo
- **Reactive Testing**: WebFlux test support
- **WebSocket Testing**: STOMP/WebSocket test utilities

### Test Organization
```
backend/src/test/java/com/coffeecookies/homepage/
├── BaseIntegrationTest.java     # Base class with MockMvc, cleanup
├── TestcontainersIntegrationTest.java  # Testcontainers base class  
├── TestDataBuilder.java         # Test data factory methods
├── JwtTestUtils.java           # JWT token generation for tests
├── TestConfiguration.java      # Test configuration
├── GoldPriceServiceTestHelper.java # Test helper utilities
├── config/                     # Configuration tests (currently empty)
├── controller/                 # Controller integration tests (currently empty)
├── dto/                        # DTO validation tests (currently empty)
├── integration/                # Full-stack integration tests (currently empty)
├── repository/                 # Repository integration tests (currently empty)
├── security/                   # Security component tests (currently empty)
└── service/                    # Service unit tests
    ├── AuthServiceTest.java
    ├── GoldPriceServiceConfigurationTest.java
    ├── GoldPriceServiceFailureTest.java (placeholder)
    └── GoldPriceServiceSuccessTest.java (placeholder)
```

### Key Test Patterns
- **Service Layer**: Unit tests with Mockito mocking of dependencies
- **Controller Layer**: Integration tests with MockMvc (not yet implemented)
- **Repository Layer**: Integration tests with Testcontainers (not yet implemented)
- **Security**: JWT validation and endpoint permission tests (not yet implemented)
- **Scheduled Tasks**: Timer-based functionality tests
- **WebSocket Services**: Real-time message broadcasting tests (not yet implemented)
- **WebFlux Services**: Reactive stream testing with StepVerifier (not yet implemented)
- **Trading Modules**: BacktestingService and SignalGenerationService tests (not yet implemented)

### Specialized Testing Considerations

#### WebSocket Testing
- **TweetWebSocketService**: Broadcasts real-time tweets, updates, status, and statistics
- **STOMP Endpoints**: `/ws/tweets` with SockJS fallback
- **Message Broker**: `/topic` prefix for broadcasting to clients
- **Testing Gap**: No WebSocket integration tests currently exist

#### WebFlux Reactive Testing
- **AIService**: Uses WebClient with Mono/Flux for Ollama API streaming
- **TwitterStreamService**: Real-time tweet streaming with Flux<ByteBuffer>
- **TruthSocialService**: Reactive API calls with rate limiting
- **Testing Gap**: No reactive stream tests currently exist

#### Trading Module Testing
- **BacktestingService**: Historical trading strategy backtesting
- **SignalGenerationService**: Technical indicator-based signal generation
- **TechnicalIndicatorService**: Various technical analysis indicators
- **Testing Gap**: No trading module tests currently exist

## 🎨 Frontend Testing Framework

### Technology Stack
- **Test Framework**: Vitest 1.6.0
- **Component Testing**: Vue Test Utils + @testing-library/vue
- **API Mocking**: Mock Service Worker (MSW) 2.3.5
- **E2E Testing**: Playwright 1.43.0
- **Accessibility Testing**: @axe-core/playwright 4.11.1
- **Coverage Reporting**: V8 coverage provider (@vitest/coverage-v8 1.6.0)

### Test Organization
```
frontend/tests/
├── setup.ts                    # Global test setup with MSW
├── mocks/
│   ├── handlers.ts            # API mock handlers
│   ├── browser.ts             # MSW browser setup
│   └── node.ts                # MSW node setup
├── accessibility/             # Accessibility test utilities
├── api/                       # API module tests
├── components/                # Component interaction tests
│   ├── AIChat.spec.ts
│   ├── FooterBar.spec.ts
│   ├── NavigationBar.spec.ts
│   └── PriceChart.spec.ts
├── e2e/                       # End-to-end user journey tests
│   ├── accessibility/
│   ├── responsive/
│   ├── accessibility.spec.ts
│   ├── responsive.spec.ts
│   └── user-flows.spec.ts
├── fixtures/                  # Test data fixtures
├── helpers/                   # Test helper utilities
├── integration/               # Multi-component integration tests
├── stores/                    # Pinia store tests
├── unit/                      # Utility and composable tests
│   ├── format.spec.ts
│   └── format.test.ts
├── views/                     # View component tests
│   ├── AIView.spec.ts
│   ├── ArticlesView.spec.ts
│   ├── GoldPriceView.spec.ts
│   ├── HomeView.spec.ts
│   ├── LoginView.spec.ts
│   ├── ProfileView.spec.ts
│   ├── ToolsView.spec.ts
│   ├── TweetsView.spec.ts
│   └── XiaomiView.spec.ts
└── visual/                    # Visual regression test utilities
```

### Key Test Patterns
- **Utilities**: Pure function unit tests (format utilities)
- **Composables**: Composition API logic tests (limited coverage)
- **Components**: User interaction and rendering tests (PriceChart, NavigationBar, etc.)
- **Stores**: State management and action tests (Pinia stores)
- **API Modules**: HTTP request/response tests with MSW
- **Views**: Page-level component tests with routing and store integration
- **E2E**: Critical user flows with Playwright page objects
- **Responsive Testing**: 5 viewport sizes (mobile, tablet, desktop variants)
- **Accessibility Testing**: WCAG compliance with axe-core integration
- **Visual Regression**: Screenshot comparison for UI consistency

### Specialized Testing Considerations

#### Chart Component Testing
- **PriceChart**: Interactive gold price charts with Chart.js integration
- **KLineChart**: Trading candlestick charts (referenced in requirements)
- **Testing Coverage**: Basic component rendering tests exist for PriceChart

#### WebSocket Composable Testing
- **Real-time Tweet Updates**: WebSocket connection for live tweet streaming
- **Connection Status**: WebSocket connection/disconnection handling
- **Message Broadcasting**: Real-time updates from backend WebSocket service
- **Testing Gap**: No explicit WebSocket composable tests currently exist

#### Responsive Testing
- **Viewport Sizes**: Comprehensive testing across 5 device sizes
- **Mobile-First**: Ensures mobile compatibility as primary concern
- **Desktop Variants**: Multiple desktop resolutions tested
- **Implementation**: Playwright viewport configuration with responsive assertions

#### Accessibility Testing
- **WCAG Compliance**: Automated accessibility rule checking
- **Screen Reader Compatibility**: Semantic HTML structure validation
- **Keyboard Navigation**: Focus management and keyboard operability
- **Color Contrast**: Sufficient contrast ratios for text and UI elements
- **Implementation**: @axe-core/playwright integration in E2E tests

## 📊 Test Execution

### Backend Commands
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Generate coverage report
mvn test jacoco:report

# Run with Testcontainers (real MySQL)  
mvn test -Dspring.profiles.active=testcontainers
```

### Frontend Commands
```bash
# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Generate coverage report  
npm test -- --coverage

# Run E2E tests
npm run test:e2e

# Run E2E tests in UI mode
npm run test:e2e:ui

# Run accessibility tests specifically
npm run test:a11y

# Run responsive tests specifically  
npm run test:responsive

# Run visual regression tests
npm run test:visual
```

## 🚀 CI/CD Integration

Tests are integrated into the continuous integration pipeline:

1. **Pull Request**: Unit and integration tests run on every PR
2. **Main Branch**: Full test suite including E2E tests
3. **Release**: Code coverage thresholds enforced
4. **Deployment**: Tests must pass before production deployment

### Test Coverage Requirements
- **Backend**: >80% line coverage for business logic (currently limited due to missing controller/repository tests)
- **Frontend**: >80% line coverage for business logic (well-covered for existing components)
- **Critical Paths**: 100% coverage required for authentication, payment, and security features

## 📈 Test Results Archive

All test results are archived with:
- Coverage reports (HTML + JSON)
- Performance metrics (execution times)
- Failure analysis (screenshots, logs, stack traces)
- Trend analysis (coverage over time)

Test results are stored in the `doc/testing/results/` directory and updated after each test execution.