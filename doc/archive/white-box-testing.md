# White-Box Testing

## 🧪 Testing Strategy Overview

This document outlines the white-box testing approach implemented for CoffeeCookie's HomePage, focusing on internal code structure, logic paths, and implementation details rather than just external behavior.

## 🔍 Backend White-Box Testing Approach

### Service Layer Testing

#### Test Coverage Strategy
- **Branch Coverage**: Test all conditional branches in service methods
- **Exception Paths**: Verify proper exception handling and error propagation
- **Boundary Conditions**: Test edge cases and boundary values
- **State Transitions**: Validate state changes in entity objects

#### AuthService White-Box Tests
- **Authentication Logic**: 
  - Valid password scenarios (admin123, user123)
  - Invalid password scenarios (wrong passwords, empty passwords)
  - User not found scenarios
  - Password encoding verification
- **Token Generation**:
  - JWT token structure validation
  - Expiration time verification
  - Role-based claims validation
- **Error Handling**:
  - BadCredentialsException throwing
  - Proper error message formatting
  - Security logging verification

#### GoldPriceService White-Box Tests (Existing)
- **API Call Logic**:
  - MetalpriceAPI configuration validation
  - Server selection (US vs EU)
  - API key validation
- **JSON Parsing**:
  - Valid JSON response handling
  - Malformed JSON error handling
  - Missing field validation
- **Price Calculation**:
  - USD to troy ounce conversion
  - Rounding precision validation
  - Change amount/percentage calculation
- **Scheduled Tasks**:
  - Timer-based execution validation
  - Exception handling in scheduled methods
  - Database persistence verification

### Controller Layer Testing

#### Request Validation Testing
- **DTO Validation**:
  - @Valid annotation enforcement
  - Constraint violation handling
  - Error response formatting
- **Parameter Binding**:
  - Path variable validation
  - Query parameter parsing
  - Request body deserialization

#### Security Testing
- **Authentication Filtering**:
  - AuthTokenFilter token extraction
  - JWT validation success/failure paths
  - SecurityContext population
- **Authorization Testing**:
  - @PreAuthorize annotations
  - Role-based access control
  - ADMIN vs USER permission validation

### Repository Layer Testing

#### JPA Query Testing
- **Custom Query Methods**:
  - findByUsername method validation
  - existsByUsername method validation  
  - Time-based queries for GoldPrice
- **Entity Relationship Testing**:
  - Article-category relationships
  - Article-tag relationships
  - User-article ownership

### Security Component Testing

#### JWT Utilities Testing
- **Token Generation**:
  - HS512 algorithm validation
  - Subject claim correctness
  - Expiration claim accuracy
- **Token Validation**:
  - Signature verification
  - Expiration checking
  - Malformed token handling

## 🎨 Frontend White-Box Testing Approach

### Utility Function Testing

#### Format Utilities White-Box Tests
- **formatDate Function**:
  - Date object input validation
  - Null/undefined input handling
  - Various date format outputs
  - Timezone handling verification
- **formatPrice Function**:
  - Number input validation
  - Decimal precision handling
  - Zero value formatting
  - Large number formatting

### Composable Testing

#### Pinia Store White-Box Tests
- **Auth Store State Management**:
  - Token persistence in localStorage
  - isAuthenticated getter logic
  - Login action error handling
  - Logout action cleanup verification
- **State Mutation Testing**:
  - Direct state modification prevention
  - Action-based state changes only
  - Asynchronous action completion

### Component Internal Logic Testing

#### Component Lifecycle Testing
- **onMounted Hooks**:
  - Data fetching on component mount
  - Error handling in async operations
  - Loading state management
- **onUnmounted Hooks**:
  - Event listener cleanup
  - Interval/timer cleanup
  - Memory leak prevention

#### Reactive State Testing
- **ref() and computed()**:
  - Reactive dependency tracking
  - Computed cache invalidation
  - Async reactive updates
- **Watch Effects**:
  - Dependency change detection
  - Cleanup function execution
  - Immediate vs lazy execution

### API Module Testing

#### Axios Interceptor Testing
- **Request Interceptor**:
  - JWT token injection logic
  - Authorization header formatting
  - Token absence handling
- **Response Interceptor**:
  - 401 error handling
  - Automatic logout triggering
  - Response unwrapping logic

#### Streaming API Testing
- **AI Chat Streaming**:
  - SSE connection establishment
  - Chunked response processing
  - Error handling in streams
  - Connection cleanup

## 🧪 Test Implementation Details

### Backend Test Implementation

#### Mockito Mocking Strategy
- **Service Layer**: Mock repository dependencies to isolate business logic
- **Controller Layer**: Mock service dependencies to test HTTP layer only
- **Repository Layer**: Use real database (Testcontainers) for JPA query validation

#### Test Data Management
- **TestDataBuilder**: Builder pattern for consistent test data creation
- **Database Cleanup**: @BeforeEach cleanup to ensure test isolation
- **Transactional Tests**: @Transactional rollback for data integrity

#### Assertion Strategy
- **AssertJ**: Fluent assertions for complex object validation
- **Hamcrest**: Matcher-based assertions for collection validation
- **JUnit Assertions**: Standard assertions for simple validations

### Frontend Test Implementation

#### MSW Mocking Strategy
- **Network-Level Interception**: Intercept HTTP requests at network level
- **Realistic Responses**: Mock responses matching actual API contracts
- **Error Scenarios**: Mock 4xx/5xx error responses for error handling tests

#### Component Mounting Strategy
- **Shallow Mounting**: Default strategy to isolate component under test
- **Deep Mounting**: Used when child component interaction is required
- **Global Configuration**: Consistent global setup across all tests

#### State Management Testing
- **Pinia Store Isolation**: Create fresh store instance for each test
- **State Persistence**: Mock localStorage for auth state testing
- **Async Action Testing**: Wait for async actions to complete before assertions

## 📊 Coverage Metrics and Quality Gates

### Backend Coverage Targets
- **Line Coverage**: >80% for service layer methods
- **Branch Coverage**: >75% for conditional logic
- **Method Coverage**: >90% for public methods
- **Class Coverage**: >85% for business logic classes

### Frontend Coverage Targets  
- **Line Coverage**: >80% for utility functions and composables
- **Branch Coverage**: >75% for conditional rendering logic
- **Function Coverage**: >85% for exported functions
- **Statement Coverage**: >80% for all source files

### Quality Gate Enforcement
- **CI/CD Integration**: Coverage thresholds enforced in pull request validation
- **Build Failure**: Build fails if coverage drops below thresholds
- **Incremental Coverage**: New code must meet or exceed existing coverage levels

## 🔧 Testing Tools and Frameworks

### Backend Testing Stack
- **JUnit 5**: Primary test framework with parameterized and dynamic tests
- **Mockito**: Mocking framework for dependency isolation
- **AssertJ**: Fluent assertion library for readable test assertions
- **Testcontainers**: Real database containers for integration testing
- **JaCoCo**: Code coverage analysis and reporting

### Frontend Testing Stack
- **Vitest**: Ultra-fast test runner with Vite integration
- **Vue Test Utils**: Official Vue component testing utilities
- **MSW**: Network-level API mocking without code changes
- **Playwright**: Reliable E2E testing with auto-waiting
- **V8 Coverage**: Accurate code coverage reporting

## 🚀 Test Execution Strategy

### Backend Test Execution
- **Unit Tests**: Fast execution with mocked dependencies (<10ms per test)
- **Integration Tests**: Moderate execution with H2 database (<5s per test)
- **Testcontainers Tests**: Slower execution with real MySQL (<15s per test)
- **Parallel Execution**: Independent tests run in parallel for speed

### Frontend Test Execution  
- **Unit Tests**: Extremely fast execution with happy-dom (<5ms per test)
- **Component Tests**: Fast execution with Vue Test Utils (<50ms per test)
- **Integration Tests**: Moderate execution with MSW (<100ms per test)
- **E2E Tests**: Slower execution with Playwright (<30s per test)
- **Parallel Execution**: Vitest parallelizes tests automatically

## 📋 Test Maintenance and Evolution

### Test Refactoring Guidelines
- **DRY Principle**: Extract common test setup into utility methods
- **Descriptive Names**: Use clear, intention-revealing test method names
- **Single Responsibility**: Each test should verify one specific behavior
- **Fast Feedback**: Keep tests fast and reliable for developer productivity

### Test Documentation Standards
- **Test Comments**: Document complex test scenarios and edge cases
- **Test Tags**: Use @Tag annotations for test categorization
- **Test Descriptions**: Provide clear descriptions of what each test verifies
- **Failure Analysis**: Document expected failure modes and debugging steps

### Continuous Improvement Process
- **Test Reviews**: Include test quality in code review process
- **Coverage Monitoring**: Track coverage trends over time
- **Performance Optimization**: Continuously optimize slow tests
- **Framework Updates**: Keep testing frameworks up to date

This comprehensive white-box testing approach ensures deep validation of CoffeeCookie's HomePage internal logic, providing confidence in code quality, maintainability, and correctness while supporting rapid development cycles through fast, reliable test feedback.