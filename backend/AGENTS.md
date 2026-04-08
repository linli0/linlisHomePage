# Backend - Spring Boot 3.2 + Java 17

> 主后端实现。Spring Boot 全栈后端服务。

**Generated:** 2026-04-08

***

## OVERVIEW

Spring Boot 主后端。分层架构：Controller → Service → Repository → Entity。JWT 认证 + Spring Security。

**状态**: 主后端实现，端口 8080，使用 H2（开发）或 MySQL（生产）数据库。

***

## WHERE TO LOOK

| Task | Location                                    |
| ---- | ------------------------------------------- |
| 控制器  | `src/main/java/.../controller/*.java` (6 个) |
| 服务层  | `src/main/java/.../service/*.java` (6 个)    |
| 数据访问 | `src/main/java/.../repository/*.java` (6 个) |
| 实体类  | `src/main/java/.../entity/*.java` (6 个)     |
| DTO  | `src/main/java/.../dto/*.java` (11 个)       |
| 安全配置 | `src/main/java/.../security/*.java` (5 个)   |
| 应用入口 | `src/main/java/.../Application.java`        |
| 配置文件 | `src/main/resources/application.yml`        |

***

## ARCHITECTURE

```
com.coffeecookies.homepage/
├── config/          # DataInitializer
├── controller/      # REST API (Auth, GoldPrice, Article, Tool, Category, Tag)
├── dto/            # Request/Response DTOs
├── entity/         # JPA 实体
├── repository/     # Spring Data JPA
├── security/       # JWT + Spring Security
└── service/        # 业务逻辑
```

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

## COMMANDS

```bash
# 构建
mvn clean package -DskipTests

# 运行
java -jar target/homepage-backend-1.0.0.jar

# 开发模式
mvn spring-boot:run

# 测试（目前无测试文件）
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

