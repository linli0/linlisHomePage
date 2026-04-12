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
- **Integration Testing**: Testcontainers (MySQL)
- **Coverage Reporting**: JaCoCo

### Test Organization
```
backend/src/test/java/com/coffeecookies/homepage/
├── controller/          # Controller integration tests
├── service/             # Service unit tests  
├── repository/          # Repository integration tests
├── security/            # Security component tests
├── config/              # Configuration tests
├── dto/                 # DTO validation tests
└── integration/         # Full-stack integration tests
```

### Key Test Patterns
- **Service Layer**: Unit tests with Mockito mocking of dependencies
- **Controller Layer**: Integration tests with MockMvc
- **Repository Layer**: Integration tests with Testcontainers
- **Security**: JWT validation and endpoint permission tests
- **Scheduled Tasks**: Timer-based functionality tests

## 🎨 Frontend Testing Framework

### Technology Stack
- **Test Framework**: Vitest
- **Component Testing**: Vue Test Utils + @testing-library/vue
- **API Mocking**: Mock Service Worker (MSW)
- **E2E Testing**: Playwright
- **Coverage Reporting**: V8 coverage provider

### Test Organization
```
frontend/tests/
├── unit/                # Utility and composable tests
├── components/          # Component interaction tests  
├── stores/              # Pinia store tests
├── api/                 # API module tests
├── integration/         # Multi-component integration tests
└── e2e/                 # End-to-end user journey tests
```

### Key Test Patterns
- **Utilities**: Pure function unit tests
- **Composables**: Composition API logic tests
- **Components**: User interaction and rendering tests
- **Stores**: State management and action tests
- **API Modules**: HTTP request/response tests with MSW
- **E2E**: Critical user flows with Playwright page objects

## 📊 Test Execution

### Backend Commands
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Generate coverage report
mvn test jacoco:report
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
```

## 🚀 CI/CD Integration

Tests are integrated into the continuous integration pipeline:

1. **Pull Request**: Unit and integration tests run on every PR
2. **Main Branch**: Full test suite including E2E tests
3. **Release**: Code coverage thresholds enforced
4. **Deployment**: Tests must pass before production deployment

## 📈 Test Results Archive

All test results are archived with:
- Coverage reports (HTML + JSON)
- Performance metrics (execution times)
- Failure analysis (screenshots, logs, stack traces)
- Trend analysis (coverage over time)

Test results are stored in the `doc/testing/results/` directory and updated after each test execution.