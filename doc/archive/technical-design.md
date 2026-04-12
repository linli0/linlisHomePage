# Technical Design

## 🏗️ Architecture Overview

This document outlines the technical design and implementation approach for the comprehensive test frameworks and documentation architecture for CoffeeCookie's HomePage.

### System Context
- **Backend**: Spring Boot 3.2 + Java 17 + Maven
- **Frontend**: Vue 3.4 + TypeScript + Vite  
- **Database**: H2 (development) / MySQL 8 (production)
- **Build Tools**: Maven (backend), npm/Vite (frontend)

## 🔧 Backend Test Framework Design

### Technology Selection Rationale

#### JUnit 5 + Spring Boot Test
- **Why**: Industry standard for Spring Boot applications, excellent integration with Spring ecosystem
- **Benefits**: Annotation-driven testing, parameterized tests, dynamic tests, excellent Spring integration

#### Mockito
- **Why**: De facto standard mocking framework for Java, excellent integration with JUnit 5
- **Benefits**: Clean mocking syntax, verification capabilities, spy support, argument captors

#### Testcontainers
- **Why**: Provides real database instances for integration testing, eliminates H2 compatibility issues
- **Benefits**: Real MySQL database, consistent behavior between test and production, supports complex database scenarios

#### JaCoCo
- **Why**: Industry standard code coverage tool for Java applications
- **Benefits**: Line, branch, and method coverage metrics, integration with Maven, HTML reports

### Test Organization Structure

```
backend/src/test/java/com/coffeecookies/homepage/
├── BaseIntegrationTest.java     # Base class with MockMvc, cleanup utilities
├── TestcontainersIntegrationTest.java  # Testcontainers base class for MySQL tests
├── TestDataBuilder.java         # Builder pattern for test data creation
├── JwtTestUtils.java           # JWT token generation utilities for security tests
├── controller/                 # Controller integration tests (@WebMvcTest)
├── service/                    # Service unit tests (Mockito mocks)
├── repository/                 # Repository integration tests (Testcontainers)
├── security/                   # Security component tests (JWT validation)
├── config/                     # Configuration class tests
├── dto/                        # DTO validation tests
└── integration/                # Full-stack integration tests
```

### Key Design Patterns

#### Base Integration Test Class
- Extends SpringBootTest with AutoConfigureMockMvc
- Provides beforeEach cleanup of test data
- Includes utility methods for creating test entities
- Supports @WithMockUser annotations for security testing

#### Testcontainers Integration
- Uses @ServiceConnection for automatic connection configuration
- Configures MySQL 8 container with proper credentials
- Uses DynamicPropertySource for runtime property configuration
- Ensures clean database state for each test

#### Test Data Builder Pattern
- Implements builder pattern for all entity types
- Provides default values with override capabilities
- Supports creation of lists/arrays of test data
- Reduces test setup boilerplate code

#### JWT Test Utilities
- Provides methods to generate valid/invalid/expired JWT tokens
- Uses same signing algorithm as production (HS512)
- Enables comprehensive security testing scenarios

### Build Configuration Design

#### Maven Dependencies
- Added spring-boot-testcontainers, mysql, junit-jupiter Testcontainers dependencies
- Configured testcontainers-bom dependency management for version consistency
- Maintained existing spring-boot-starter-test and spring-security-test dependencies

#### Test Profiles
- Created application-test.yml with H2 database configuration for fast unit tests
- Configured Testcontainers profile for real MySQL integration tests
- Set up proper logging levels for test debugging

## 🎨 Frontend Test Framework Design

### Technology Selection Rationale

#### Vitest
- **Why**: Native Vite integration, extremely fast test execution, TypeScript support
- **Benefits**: Hot module replacement for tests, browser mode, coverage reporting, Jest-compatible API

#### Vue Test Utils
- **Why**: Official Vue testing library, excellent Composition API support
- **Benefits**: Shallow/deep mounting, event triggering, props/events validation, stubbing

#### Mock Service Worker (MSW)
- **Why**: Intercepts HTTP requests at network level, realistic API mocking
- **Benefits**: No code changes needed, works with any HTTP client, supports REST and GraphQL

#### Playwright
- **Why**: Modern E2E testing framework, cross-browser support, reliable automation
- **Benefits**: Auto-waiting, network interception, screenshot/video recording, mobile emulation

### Test Organization Structure

```
frontend/tests/
├── setup.ts                    # Global test setup with MSW initialization
├── mocks/
│   ├── handlers.ts            # API mock handlers for all endpoints
│   ├── browser.ts             # MSW browser setup for E2E tests
│   └── node.ts                # MSW node setup for unit/integration tests
├── unit/                      # Pure function and composable tests
├── components/                # Component interaction and rendering tests
├── stores/                    # Pinia store state and action tests
├── api/                       # API module request/response tests
├── integration/               # Multi-component workflow tests
└── e2e/                       # End-to-end user journey tests
```

### Key Design Patterns

#### Global Test Setup
- Initializes Pinia store for all tests
- Sets up MSW server for API mocking
- Configures Vue Test Utils global settings
- Mocks localStorage for auth state persistence

#### API Mock Handlers
- Comprehensive mock handlers for all backend endpoints
- Realistic response data matching production API contracts
- Error scenario handlers for 401, 403, 404, 500 responses
- Parameter validation in mock handlers

#### Component Testing Strategy
- Uses Vue Test Utils mount function for component isolation
- Tests both shallow (default) and deep mounting scenarios
- Validates props, events, slots, and internal state
- Mocks child components and external dependencies

#### E2E Testing Strategy
- Uses Playwright page object model for maintainable tests
- Tests critical user journeys: login → gold price → AI chat
- Cross-browser testing with Chromium, Firefox, WebKit
- Automatic screenshot capture on test failures

### Build Configuration Design

#### Package.json Dependencies
- Added vitest, @vitest/coverage-v8, happy-dom for test environment
- Added @vue/test-utils, @testing-library/vue for component testing
- Added msw for API mocking
- Added playwright for E2E testing

#### Vitest Configuration
- Configured with Vue plugin and path aliases
- Set up happy-dom test environment for DOM APIs
- Configured coverage reporting with V8 provider
- Set up file patterns and exclusions

#### Playwright Configuration
- Configured multi-browser testing projects
- Set up web server management for development server
- Configured reporters and output directories
- Set up trace and screenshot capture on failures

## 📚 Documentation Architecture Design

### Centralized Documentation Structure

```
doc/
├── index.md                   # Documentation hub and navigation
├── architecture/
│   └── overview.md           # System architecture and design decisions
├── backend/
│   └── index.md              # Backend implementation details
├── frontend/
│   └── index.md              # Frontend implementation details
├── design/
│   └── index.md              # UI/UX design system and guidelines  
├── testing/
│   ├── index.md              # Testing strategy overview
│   └── test-strategy.md      # Detailed test framework documentation
├── deployment/
│   └── index.md              # Deployment guides and production setup
└── archive/
    ├── requirement-design.md # Requirement design documentation
    ├── technical-design.md   # Technical design documentation
    ├── code-development.md   # Code development documentation  
    ├── white-box-testing.md  # White-box testing documentation
    └── automated-testing.md  # Automated testing documentation
```

### Migration Strategy

#### File Identification
- Identified all markdown documentation files in project root and subdirectories
- Excluded third-party documentation in node_modules
- Categorized files by content type and audience

#### Content Reorganization
- Created logical categories based on documentation purpose
- Mapped existing files to appropriate new locations
- Updated internal cross-references to reflect new file paths

#### Navigation Updates
- Updated main README.md to reference new documentation structure
- Created documentation hub (doc/index.md) with comprehensive navigation
- Maintained backward compatibility during transition period

### Integration with Development Workflow

#### Documentation Standards
- Established guidelines for future documentation creation
- Defined contribution process for documentation updates
- Integrated documentation review into pull request process

#### Build Process Integration
- Ensured documentation builds are part of CI/CD pipeline
- Configured documentation linting and validation
- Set up automated documentation deployment

## 🔧 Implementation Approach

### Phased Implementation Strategy

#### Phase 1: Infrastructure Setup
- Implement backend test framework infrastructure
- Implement frontend test framework infrastructure  
- Create documentation directory structure
- Update build configuration files

#### Phase 2: Sample Implementation
- Create sample tests demonstrating framework functionality
- Verify test framework execution and reporting
- Validate documentation structure and navigation

#### Phase 3: Comprehensive Coverage
- Implement comprehensive test suites for all modules
- Execute full test suite and generate coverage reports
- Archive test results and documentation

#### Phase 4: Final Integration
- Update all AGENTS.md files with comprehensive information
- Create complete functional change archive
- Verify all requirements are fully implemented

### Risk Mitigation Strategies

#### Dependency Management
- Used dependency management (BOM) for Testcontainers versions
- Pinned exact versions for all test dependencies
- Verified compatibility between all test framework components

#### Backward Compatibility
- Maintained existing test functionality (GoldPriceService tests)
- Ensured new test infrastructure doesn't break existing code
- Provided migration path for existing test practices

#### Performance Considerations
- Optimized Testcontainers usage with container reuse
- Configured Vitest for maximum parallelization
- Set reasonable timeout values for all test types

This technical design provides a comprehensive foundation for implementing the requested test frameworks and documentation architecture while ensuring maintainability, performance, and developer experience.