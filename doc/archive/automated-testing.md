# Automated Testing

## 🤖 Test Automation Framework Overview

This document details the comprehensive automated testing framework implemented for CoffeeCookie's HomePage, covering both backend (Spring Boot) and frontend (Vue 3) applications with full CI/CD integration.

## 🔧 Backend Automated Testing Framework

### Test Automation Stack

#### Core Technologies
- **JUnit 5**: Primary test framework with advanced features (parameterized tests, dynamic tests, nested tests)
- **Spring Boot Test**: Spring-specific testing utilities and annotations
- **Mockito**: Mocking framework for dependency isolation
- **Testcontainers**: Real database containers for integration testing
- **JaCoCo**: Code coverage analysis and reporting

#### Build Integration
- **Maven Surefire Plugin**: Unit test execution during build process
- **Maven Failsafe Plugin**: Integration test execution with proper lifecycle management
- **Maven JaCoCo Plugin**: Code coverage reporting integrated into build

### Automated Test Types

#### Unit Tests (Service Layer)
- **Execution**: `mvn test` - Runs during standard build process
- **Speed**: <10ms per test (mocked dependencies)
- **Coverage**: >80% line coverage target
- **Automation**: Fully automated, no external dependencies

#### Integration Tests (Controller Layer)
- **Execution**: `mvn verify` - Runs during integration test phase
- **Speed**: <5s per test (H2 in-memory database)
- **Coverage**: Validates HTTP request/response contracts
- **Automation**: Fully automated with embedded H2 database

#### Repository Tests (JPA Layer)
- **Execution**: `mvn verify -Dspring.profiles.active=testcontainers`
- **Speed**: <15s per test (real MySQL via Testcontainers)
- **Coverage**: Validates real database interactions and queries
- **Automation**: Fully automated with Docker container orchestration

#### Security Tests
- **Execution**: Integrated into unit and integration test suites
- **Speed**: Varies by test type (unit vs integration)
- **Coverage**: JWT validation, endpoint permissions, authentication flows
- **Automation**: Fully automated with comprehensive security scenarios

### CI/CD Integration

#### GitHub Actions Workflow
```yaml
name: Backend Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: rootpassword
          MYSQL_DATABASE: testdb
        ports:
          - 3306:3306
        options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=3
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      - name: Run unit tests
        run: mvn test
      - name: Run integration tests with H2
        run: mvn verify
      - name: Run Testcontainers tests
        run: mvn verify -Dspring.profiles.active=testcontainers
      - name: Generate coverage report
        run: mvn test jacoco:report
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
```

#### Quality Gates
- **Pull Request Validation**: All tests must pass before merge
- **Coverage Thresholds**: Build fails if coverage drops below 80%
- **Dependency Scanning**: Vulnerability scanning on every build
- **Code Quality**: Static analysis integrated into test pipeline

## 🎨 Frontend Automated Testing Framework

### Test Automation Stack

#### Core Technologies
- **Vitest**: Ultra-fast test runner with native Vite integration
- **Vue Test Utils**: Official Vue component testing utilities
- **MSW (Mock Service Worker)**: Network-level API mocking
- **Playwright**: Cross-browser E2E testing framework
- **V8 Coverage**: Accurate code coverage reporting

#### Build Integration
- **npm Scripts**: Comprehensive test commands integrated into package.json
- **Vitest Configuration**: Optimized configuration for maximum performance
- **Playwright Configuration**: Multi-browser testing with web server management

### Automated Test Types

#### Unit Tests (Utilities & Composables)
- **Execution**: `npm test` - Runs all unit tests
- **Speed**: <5ms per test (happy-dom environment)
- **Coverage**: >80% line coverage target
- **Automation**: Fully automated with zero external dependencies

#### Component Tests (Views & Components)
- **Execution**: `npm test` - Runs as part of unit test suite
- **Speed**: <50ms per test (Vue Test Utils mounting)
- **Coverage**: Validates component rendering, props, events, and interactions
- **Automation**: Fully automated with comprehensive component scenarios

#### Store Tests (Pinia)
- **Execution**: `npm test` - Runs as part of unit test suite  
- **Speed**: <25ms per test (isolated store instances)
- **Coverage**: Validates state mutations, actions, getters, and async operations
- **Automation**: Fully automated with mock API responses via MSW

#### API Tests (HTTP Modules)
- **Execution**: `npm test` - Runs as part of unit test suite
- **Speed**: <100ms per test (MSW network interception)
- **Coverage**: Validates request parameters, response handling, error scenarios
- **Automation**: Fully automated with realistic API mock responses

#### E2E Tests (User Journeys)
- **Execution**: `npm run test:e2e` - Runs Playwright tests
- **Speed**: <30s per test (real browser automation)
- **Coverage**: Validates complete user workflows and cross-browser compatibility
- **Automation**: Fully automated with automatic screenshot/video capture on failures

### CI/CD Integration

#### GitHub Actions Workflow
```yaml
name: Frontend Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '20'
          cache: 'npm'
      - name: Install dependencies
        run: npm ci
      - name: Run unit and component tests
        run: npm test -- --coverage
      - name: Run E2E tests
        run: npm run test:e2e
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
      - name: Upload test artifacts
        uses: actions/upload-artifact@v3
        if: failure()
        with:
          name: playwright-report
          path: playwright-report/
```

#### Quality Gates
- **Pull Request Validation**: All tests must pass before merge
- **Coverage Thresholds**: Build fails if coverage drops below 80%
- **Visual Regression**: Screenshot comparison for critical UI components
- **Performance Budgets**: Bundle size and load time monitoring

## 🚀 End-to-End Test Automation

### Critical User Journey Tests

#### Authentication Flow
- **Test Steps**: Navigate to login → Enter credentials → Verify dashboard access → Logout
- **Validation Points**: JWT token generation, localStorage persistence, route protection
- **Automation**: Playwright with page object model
- **Browsers**: Chromium, Firefox, WebKit

#### Gold Price Tracking
- **Test Steps**: Navigate to gold page → Verify current price → Switch currencies → View historical chart
- **Validation Points**: Real-time price updates, currency conversion, chart rendering
- **Automation**: Playwright with API mocking via MSW
- **Data Validation**: Price format, change indicators, statistical metrics

#### Article Management (Admin)
- **Test Steps**: Login as admin → Create article → Publish → View on homepage → Delete
- **Validation Points**: Form validation, rich text rendering, category/tag assignment
- **Automation**: Playwright with comprehensive form interaction
- **Security Validation**: ADMIN role enforcement, unauthorized access prevention

### Cross-Browser Compatibility Testing

#### Browser Matrix
- **Chromium**: Latest stable version
- **Firefox**: Latest stable version  
- **WebKit**: Latest stable version (Safari equivalent)
- **Mobile Emulation**: iPhone 12, Pixel 5 screen sizes

#### Automated Validation
- **Layout Consistency**: Visual regression testing across browsers
- **Functionality Parity**: Feature availability and behavior consistency
- **Performance Metrics**: Load times and interaction responsiveness
- **Accessibility Compliance**: WCAG 2.1 AA compliance verification

## 📊 Test Reporting and Analytics

### Coverage Reporting

#### Backend Coverage Reports
- **HTML Reports**: Interactive coverage visualization with source code highlighting
- **JSON Reports**: Machine-readable coverage data for CI/CD integration
- **Line-by-Line Analysis**: Detailed coverage breakdown per source file
- **Trend Analysis**: Coverage history and improvement tracking

#### Frontend Coverage Reports
- **V8 Coverage**: Accurate coverage reporting with branch and function metrics
- **Interactive HTML**: Clickable source files with coverage overlays
- **Threshold Enforcement**: Build failure on coverage threshold violations
- **Incremental Coverage**: New code coverage requirements

### Test Results Dashboard

#### Test Execution Metrics
- **Pass/Fail Rates**: Overall test success rates and trends
- **Execution Times**: Performance metrics for test suite optimization
- **Flaky Test Detection**: Automatic identification of unstable tests
- **Failure Analysis**: Root cause categorization and debugging insights

#### Quality Metrics
- **Code Coverage Trends**: Coverage evolution over time
- **Defect Density**: Bugs per lines of code or test coverage
- **Technical Debt**: Code quality issues and remediation priorities
- **Release Readiness**: Quality gate status for production deployment

## 🔄 Continuous Testing Strategy

### Development Workflow Integration

#### Local Development
- **Hot Reloading Tests**: Vitest watch mode with instant feedback
- **Debugging Support**: IDE integration for test debugging
- **Selective Execution**: Run specific tests or test files
- **Coverage Analysis**: Real-time coverage feedback during development

#### Pull Request Process
- **Automated Validation**: All tests run automatically on every PR
- **Quality Gate Enforcement**: PR blocked if tests fail or coverage drops
- **Code Review Integration**: Test coverage and quality metrics in PR reviews
- **Merge Requirements**: All quality gates must pass before merge

### Production Monitoring Integration

#### Synthetic Monitoring
- **Automated Health Checks**: Scheduled E2E tests against production
- **Performance Baselines**: Response time and availability monitoring
- **Alert Integration**: Slack/email alerts on test failures
- **Incident Correlation**: Test failures correlated with production incidents

#### Feedback Loop
- **Production Bug Prevention**: Tests created for every production bug
- **Test Coverage Gaps**: Identified gaps addressed in development cycles
- **Quality Improvement**: Continuous refinement of test strategies and coverage
- **Team Metrics**: Quality metrics used for team performance and improvement

## 🛠️ Test Maintenance and Evolution

### Test Refactoring Guidelines

#### Performance Optimization
- **Parallel Execution**: Maximize test parallelization for speed
- **Resource Cleanup**: Proper cleanup of test resources and state
- **Mock Optimization**: Use appropriate mock granularity (unit vs integration)
- **Test Isolation**: Ensure tests don't depend on each other's state

#### Maintainability Standards
- **DRY Principle**: Extract common test logic into utilities
- **Descriptive Naming**: Clear, intention-revealing test names
- **Single Responsibility**: Each test verifies one specific behavior
- **Documentation**: Clear comments for complex test scenarios

### Framework Evolution Strategy

#### Version Management
- **Dependency Updates**: Regular framework version updates
- **Breaking Changes**: Proactive handling of breaking changes
- **Feature Adoption**: Early adoption of new testing features
- **Security Patches**: Immediate application of security updates

#### Continuous Improvement
- **Test Reviews**: Regular review of test quality and coverage
- **Performance Monitoring**: Continuous optimization of test execution times
- **Best Practices**: Adoption of industry best practices and patterns
- **Team Training**: Regular knowledge sharing and skill development

This comprehensive automated testing framework ensures CoffeeCookie's HomePage maintains high quality, reliability, and maintainability through systematic, reliable, and efficient test automation across the entire development lifecycle.