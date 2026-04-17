# Backend Tests - JUnit 5 + Mockito + Testcontainers

> Spring Boot 测试基础设施。分层测试：单元 → 集成 → E2E。

---

## OVERVIEW

JUnit 5 + Spring Boot Test + Mockito + Testcontainers。H2 内存数据库默认，Testcontainers 提供真实 MySQL。

**测试金字塔**: 70% 单元 (Service) + 20% 集成 (Controller/Repository) + 10% E2E

---

## STRUCTURE

```
com.coffeecookies.homepage/
├── BaseIntegrationTest.java           # MockMvc + @Transactional 基类
├── TestcontainersIntegrationTest.java # MySQL 容器基类
├── TestDataBuilder.java               # Builder 模式测试数据工厂
├── JwtTestUtils.java                  # JWT Token 生成工具
├── controller/                        # Controller 集成测试
├── service/                           # Service 单元测试
├── repository/                        # Repository 测试 (Testcontainers)
├── security/                          # JWT/Security 测试
├── config/                            # 配置测试
├── dto/                               # DTO 校验测试
└── integration/                       # 全栈集成测试
```

---

## BASE CLASSES

**BaseIntegrationTest**: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional`。提供 `mockMvc`, `objectMapper`, `createTestUser()`, `toJson()`。

**TestcontainersIntegrationTest**: MySQL 8.0 容器，随机端口，`@DynamicPropertySource` 动态配置。

---

## CONVENTIONS

**单元测试**: `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks`

**集成测试**: `extends BaseIntegrationTest` + `mockMvc.perform()`

**测试数据**: `TestDataBuilder.user().username("test").build()`

**命名**: `*Test.java`, `*ConfigurationTest`, `*SuccessTest`, `*FailureTest`。AssertJ 流式断言。

---

## COMMANDS

```bash
mvn test                                    # 全部测试
mvn test -Dtest=AuthServiceTest            # 指定测试
mvn test jacoco:report                     # 覆盖率报告
mvn test -Dspring.profiles.active=testcontainers  # 真实 MySQL
```

---

## NOTES

- **H2**: 默认测试环境，速度快
- **Testcontainers**: 需 Docker，验证真实 SQL
- **JWT 测试密钥**: `JwtTestUtils` 固定测试密钥，与生产隔离
- **数据清理**: `@Transactional` 自动回滚
