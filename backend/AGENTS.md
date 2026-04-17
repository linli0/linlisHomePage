# Backend - Spring Boot 3.2 + Java 17

> 主后端实现。Spring Boot 全栈后端服务。

**Generated:** 2026-04-15

***

## OVERVIEW

Spring Boot 主后端。分层架构：Controller → Service → Repository → Entity。JWT 认证 + Spring Security + WebSocket + WebFlux。

**状态**: 主后端实现，端口 8080，使用 H2（开发）或 MySQL（生产）数据库。

***

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| 修改 API 端点 | `src/main/java/.../controller/*.java` | Spring Boot 控制器 (10个) |
| Service 层 | `src/main/java/.../service/*.java` | 业务逻辑层 (15个) |
| Repository 层 | `src/main/java/.../repository/*.java` | 数据访问层 (11个) |
| 实体类 | `src/main/java/.../entity/*.java` | JPA 实体 (12个) |
| DTO | `src/main/java/.../dto/*.java` | 数据传输对象 (29个) |
| 配置类 | `src/main/java/.../config/*.java` | 配置类 (6个, 包括WebSocketConfig) |
| 安全组件 | `src/main/java/.../security/*.java` | JWT组件 (5个) |

***

## ARCHITECTURE

```
com.coffeecookies.homepage/
├── config/          # DataInitializer, WebSocketConfig
├── controller/      # REST API (Auth, GoldPrice, Article, Tool, Category, Tag, Trading, Tweet, XiaomiSpeaker)
├── dto/            # Request/Response DTOs
├── entity/         # JPA 实体 (including Trading entities)
├── repository/     # Spring Data JPA (including Trading repositories)
├── security/       # JWT + Spring Security
└── service/        # 业务逻辑 (BacktestingService, SignalGenerationService, TechnicalIndicatorService, TruthSocialService, TweetPollingService, TweetWebSocketService, TwitterStreamService)
```

**New Technologies:**
- **WebSocket (STOMP)** for real-time updates
- **WebFlux** for reactive programming  
- **ZXing** for QR code generation

***

## CONVENTIONS

### Controller 模式

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<Result<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(Result.success(authService.login(request)));
    }
}
```

### Service 模式

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    
    @Transactional
    public AuthResponse login(LoginRequest request) { ... }
}
```

### Repository 模式

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

### Entity 模式

```java
@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
}
```

### DTO 模式

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type;
    private Long id;
    private String username;
    private String role;
}
```

**统一响应:**

```java
Result.success(data)  // { code: 200, message: "Success", data: ... }
Result.error(message) // { code: 400, message: "...", data: null }
```

***

## SECURITY

**JWT 过滤器链:**

1. `AuthTokenFilter` 解析 Bearer Token
2. `JwtUtils` 验证 Token，提取用户名
3. `UserDetailsServiceImpl` 加载用户信息
4. `SecurityContext` 设置认证

**端点权限:**

- `permitAll`: `/api/auth/**`, `/api/gold-price/**`, `/api/articles/public/**`, `/api/tools/**`
- `authenticated`: 其他所有
- `hasRole('ADMIN')`: 文章创建/更新/删除

***

## ANTI-PATTERNS

1. **不要在 Controller 写业务逻辑** — 委托给 Service
2. **不要直接返回 Entity** — 使用 DTO
3. **不要硬编码 JWT 密钥** — 使用 `application.yml` 或环境变量
4. **不要忘记数据库迁移** — 生产环境使用 Flyway 管理 schema 变更
5. **不要忽略事务边界** — 使用 `@Transactional` 确保数据一致性

***

## TESTING FRAMEWORK

### Test Structure
```
src/test/java/com/coffeecookies/homepage/
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

### Dependencies
- **Test Framework**: JUnit 5 + Spring Boot Test
- **Mocking**: Mockito  
- **Integration Testing**: Testcontainers 1.19.7 (MySQL container)
- **Coverage**: JaCoCo
- **WebSocket Testing**: STOMP over WebSocket testing considerations
- **Reactive Testing**: WebFlux reactive testing considerations

### Commands
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

### Test Patterns
- **Unit Tests**: Service layer with Mockito mocking
- **Integration Tests**: Controller layer with MockMvc
- **Repository Tests**: JPA repositories with Testcontainers
- **Security Tests**: JWT validation and endpoint permissions
- **Scheduled Task Tests**: Timer-based functionality
- **WebSocket Tests**: Real-time communication testing
- **Reactive Tests**: WebFlux stream processing validation

## COMMANDS

```bash
# 构建
mvn clean package -DskipTests

# 运行
java -jar target/homepage-backend-1.0.0.jar

# 开发模式
mvn spring-boot:run

# 测试
mvn test
```

***

## DATABASE

- **开发**: H2 内存数据库 (`jdbc:h2:mem:homepage`)
- **生产**: MySQL 8.0 (`jdbc:mysql://localhost:3306/homepage`)
- **DDL**: `ddl-auto: update`（自动更新表结构）

***

## NOTES

- **Lombok**: 使用 `@Data`, `@Builder`, `@RequiredArgsConstructor` 减少样板代码
- **审计**: `@EnableJpaAuditing` + `@CreatedDate`/`@LastModifiedDate`
- **定时任务**: `@EnableScheduling` + `@Scheduled` (GoldPriceService)
- **CORS**: 允许所有来源（生产环境需限制）
- **MetalpriceAPI 集成**: 支持真实金价数据，通过环境变量配置
- **WebSocket**: STOMP协议实现实时通信
- **WebFlux**: Reactive编程支持异步处理
- **ZXing**: QR码生成库集成

***

## METALPRICEAPI 集成

### 配置方式

**环境变量**:
```bash
export METALPRICE_API_KEY=your-api-key-here
export METALPRICE_ENABLED=true
export METALPRICE_SERVER=us  # 或 eu
```

**application.yml**:
```yaml
metalpriceapi:
  api-key: ${METALPRICE_API_KEY:}
  server: ${METALPRICE_SERVER:us}
  enabled: ${METALPRICE_ENABLED:false}
```

### 工作原理

1. **定时任务**: 每分钟调用 `updateGoldPrice()` 方法
2. **API 调用**: 必须启用 MetalpriceAPI 并配置有效的 API 密钥
3. **错误处理**: API 调用失败时记录错误日志，不会保存数据
4. **禁用行为**: 如果 MetalpriceAPI 未启用，定时任务会跳过更新
5. **数据存储**: 仅在 API 成功返回时保存价格数据到数据库

### 配置说明

**环境变量**:
```bash
# 必须设置为 true 才能启用金价更新
export METALPRICE_ENABLED=true

# 必须配置有效的 API 密钥
export METALPRICE_API_KEY=your-api-key-here

# 可选：选择服务器区域（us 或 eu，默认 us）
export METALPRICE_SERVER=us
```

**application.yml**:
```yaml
metalpriceapi:
  api-key: ${METALPRICE_API_KEY:}
  server: ${METALPRICE_SERVER:us}
  enabled: ${METALPRICE_ENABLED:false}
```

### 注意事项

- **必须配置 API 密钥**: 未配置 API 密钥时系统无法获取金价数据
- **必须启用服务**: 设置 `METALPRICE_ENABLED=true` 才会更新金价
- **免费层有限额**: 注意 API 调用次数限制
- **日志监控**: 查看日志了解 API 调用状态
- **生产环境**: 必须启用真实数据，系统不支持模拟数据
- **无降级机制**: API 不可用时不会自动降级，必须确保 API 可用