# Test Strategy

## 🎯 Overview

CoffeeCookie's HomePage implements a comprehensive testing strategy following industry best practices for both Spring Boot backend and Vue 3 frontend applications.

### Testing Pyramid

| Test Type | Backend | Frontend | Purpose |
|-----------|---------|----------|---------|
| **Unit Tests** | 70% | 70% | Pure logic, utilities, services |
| **Integration Tests** | 20% | 20% | Component interactions, API contracts |
| **E2E Tests** | 10% | 10% | Critical user journeys |

## 🔧 Backend Test Framework

### Technology Stack
- **Test Framework**: JUnit 5 + Spring Boot Test
- **Mocking**: Mockito
- **Assertions**: AssertJ + Hamcrest  
- **Integration Testing**: Testcontainers (MySQL)
- **Coverage Reporting**: JaCoCo

### Test Organization
```
backend/src/test/java/com/coffeecookies/homepage/
├── BaseIntegrationTest.java     # Base class with MockMvc, cleanup
├── TestcontainersIntegrationTest.java  # Testcontainers base class
├── TestDataBuilder.java         # Test data factory methods
├── JwtTestUtils.java           # JWT token generation for tests
├── controller/                 # Controller integration tests
├── service/                    # Service unit tests (existing GoldPriceService tests)
├── repository/                 # Repository integration tests
├── security/                   # Security component tests
├── config/                     # Configuration tests
├── dto/                        # DTO validation tests
└── integration/                # Full-stack integration tests
```

### Key Test Patterns

#### Unit Tests (Service Layer)
- Use Mockito to mock dependencies
- Focus on business logic validation
- Test edge cases and error conditions
- Example: `AuthServiceTest` validates authentication logic

#### Integration Tests (Controller Layer)  
- Use `@WebMvcTest` for slice testing
- Mock service layer with `@MockBean`
- Test HTTP request/response contracts
- Validate status codes, headers, and response bodies

#### Repository Tests
- Use Testcontainers for real MySQL database
- Test JPA query methods and relationships
- Validate data persistence and retrieval
- Ensure database schema compatibility

#### Security Tests
- Test JWT token generation and validation
- Validate endpoint permissions (ADMIN vs USER)
- Test authentication failure scenarios
- Verify password-only login flow

#### Scheduled Task Tests
- Test timer-based functionality (gold price updates)
- Validate exchange rate update intervals
- Handle exception scenarios in scheduled methods

### Build Configuration
- **Dependencies**: Added Testcontainers, MySQL container, JUnit Jupiter
- **Maven Plugins**: Configured for test execution and coverage reporting
- **Test Profiles**: Separate `application-test.yml` configuration

## 🎨 Frontend Test Framework

### Technology Stack
- **Test Framework**: Vitest
- **Component Testing**: Vue Test Utils + @testing-library/vue
- **API Mocking**: Mock Service Worker (MSW)
- **E2E Testing**: Playwright
- **Coverage Reporting**: V8 coverage provider

### Test Organization
```
frontend/tests/
├── setup.ts                    # Global test setup with MSW
├── mocks/
│   ├── handlers.ts            # API mock handlers
│   ├── browser.ts             # MSW browser setup
│   └── node.ts                # MSW node setup
├── unit/                      # Utility and composable tests
├── components/                # Component interaction tests
├── stores/                    # Pinia store tests
├── api/                       # API module tests
├── integration/               # Multi-component integration tests
└── e2e/                       # End-to-end user journey tests
```

### Key Test Patterns

#### Unit Tests (Utilities & Composables)
- Test pure functions with various inputs
- Validate formatting and transformation logic
- Example: `format.spec.ts` tests date and price formatting

#### Component Tests
- Use Vue Test Utils to mount components
- Test props, events, and internal state
- Validate user interactions and rendering
- Mock child components and external dependencies

#### Store Tests (Pinia)
- Test state mutations and actions
- Validate getters and computed properties
- Mock API calls with MSW
- Test async actions and error handling

#### API Module Tests
- Use MSW to mock HTTP endpoints
- Test request parameters and response handling
- Validate error scenarios and retry logic
- Ensure proper JWT token injection

#### E2E Tests (Playwright)
- Test critical user flows end-to-end
- Validate navigation and page transitions
- Test form submissions and data persistence
- Cross-browser compatibility testing

### Build Configuration
- **Dependencies**: Added Vitest, Vue Test Utils, MSW, Playwright
- **Vitest Config**: Configured with coverage reporting and test environment
- **Playwright Config**: Multi-browser testing with web server management
- **Scripts**: Added comprehensive test commands to package.json

## 📊 Test Execution Commands

### Backend
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

### Frontend
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
```

## 🚀 CI/CD Integration

Tests are integrated into the continuous integration pipeline:

1. **Pull Request Validation**: Unit and integration tests run on every PR
2. **Main Branch**: Full test suite including E2E tests
3. **Release Gates**: Code coverage thresholds enforced (>80% line coverage)
4. **Deployment**: All tests must pass before production deployment

## 📈 Quality Metrics

### Coverage Targets
- **Line Coverage**: >80%
- **Branch Coverage**: >75%  
- **Function Coverage**: >80%
- **Statement Coverage**: >80%

### Performance Targets
- **Unit Tests**: <10ms per test
- **Integration Tests**: <5s per test
- **E2E Tests**: <30s per test
- **Full Suite**: <5 minutes total execution time

## 📋 Implementation Status

### ✅ Completed
- [x] Backend test framework infrastructure (Testcontainers, base classes, utilities)
- [x] Frontend test framework infrastructure (Vitest, MSW, Playwright, setup files)
- [x] Documentation architecture with centralized `doc/` directory
- [x] Sample tests demonstrating framework functionality

### 🔄 In Progress
- [ ] Comprehensive test suite implementation for all modules
- [ ] Test execution and results archiving
- [ ] CI/CD pipeline integration
- [ ] Final documentation updates

## 🎯 Next Steps

1. **Implement Comprehensive Test Suites**: Write tests for all backend services, controllers, and frontend components
2. **Execute Test Suite**: Run all tests and generate coverage reports
3. **Archive Test Results**: Store test results and coverage reports in `doc/testing/results/`
4. **Update AGENTS.md**: Document all test framework requirements and procedures
5. **Create Functional Change Archive**: Document all implementation changes with requirement design, technical design, and testing documentation

This test strategy ensures comprehensive coverage of CoffeeCookie's HomePage functionality while maintaining fast feedback loops and reliable automated testing.