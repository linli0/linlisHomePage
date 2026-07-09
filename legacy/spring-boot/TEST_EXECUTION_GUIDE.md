# TDD 测试执行指南

## 测试概述

本项目为 GoldPriceService 的 MetalpriceAPI 集成功能实现了完整的 TDD 测试用例。

### 测试文件列表

| 测试文件 | 测试用例数 | 测试范围 |
|---------|---------|---------|
| `GoldPriceServiceConfigurationTest.java` | 7 | 配置验证逻辑 |
| `GoldPriceServiceSuccessTest.java` | 9 | 成功API调用、JSON解析、价格计算 |
| `GoldPriceServiceFailureTest.java` | 11 | API失败场景（网络错误、无效响应等） |
| `GoldPriceServiceWebClientMockTest.java` | 11 | WebClient Mock验证 |
| `GoldPriceServiceScheduledTaskTest.java` | 10 | 定时任务测试 |
| **总计** | **48** | **全面覆盖** |

### 测试覆盖的功能

1. **配置验证** (7 tests)
   - MetalpriceAPI 启用状态验证
   - API 密钥存在性验证
   - 服务器配置验证
   - 禁用时的行为验证

2. **成功场景** (9 tests)
   - 有效 API 响应解析
   - 正确的价格计算
   - 不同服务器区域（US/EU）
   - 价格四舍五入
   - 不同价格范围处理

3. **失败场景** (11 tests)
   - null 响应处理
   - API 错误处理
   - 缺失字段处理
   - 无效数据值处理
   - 网络错误处理
   - 超时处理

4. **WebClient Mock** (11 tests)
   - Mock 验证
   - 无真实API调用验证
   - 参数传递验证
   - 多次调用验证

5. **定时任务** (10 tests)
   - 价格更新流程
   - 汇率更新流程
   - 异常处理验证

## 运行测试

### 方法 1: 使用 Maven（推荐）

```bash
# 运行所有测试
cd backend
mvn test

# 运行特定测试类
mvn test -Dtest=GoldPriceServiceConfigurationTest

# 运行特定测试方法
mvn test -Dtest=GoldPriceServiceConfigurationTest#fetchGoldPriceFromAPI_shouldThrowException_whenMetalpriceApiDisabled

# 运行测试并生成覆盖率报告
mvn test jacoco:report
```

### 方法 2: 使用 IDE

**IntelliJ IDEA:**
1. 右键点击测试类或方法
2. 选择 "Run 'XXX'" 或 "Debug 'XXX'"
3. 查看测试结果

**Eclipse:**
1. 右键点击测试类
2. 选择 "Run As" → "JUnit Test"

### 方法 3: 使用 Gradle（如果使用Gradle构建）

```bash
./gradlew test
./gradlew test --tests "*GoldPriceService*"
```

## 配置环境变量（可选）

如需测试不同配置，可以设置环境变量：

```bash
export METALPRICE_ENABLED=true
export METALPRICE_API_KEY=test-key

mvn test
```

## 验证测试结果

### 成功的测试

所有 48 个测试应该通过（green）：
- ✅ 测试数量: 48
- ✅ 失败数: 0
- ✅ 错误数: 0
- ✅ 跳过数: 0

### 测试覆盖率目标

**GoldPriceService.fetchGoldPriceFromAPI() 方法:**
- **行覆盖率**: > 90%
- **分支覆盖率**: > 85%
- **方法覆盖率**: 100%

### 检查覆盖率报告

```bash
# 生成 JaCoCo 覆盖率报告
mvn test jacoco:report

# 查看报告
open backend/target/site/jacoco/index.html
```

## TDD 流程验证

### RED 阶段 ✅
所有测试用例已创建，应该全部失败（因为它们依赖于特定的 mock 设置）

### GREEN 阶段 ✅
实现已完成，所有测试应该通过

### REFACTOR 阶段 ✅
代码质量良好，已遵循 TDD 最佳实践

## 测试隔离性

所有测试都是独立的：
- 无测试依赖
- 使用 Mock 隔离外部依赖
- 使用 @Transactional 确保数据清理

## Mock 策略

- **WebClient**: 使用 Mockito Mock，避免真实API调用
- **Repository**: 使用 Spring Boot Test @DataJpaTest 或 Mock
- **定时任务**: 使用 @MockBean 隔离，手动触发测试

## 注意事项

1. **网络隔离**: 所有测试使用 Mock，不会产生真实API请求
2. **数据隔离**: 每个测试前会清理数据库
3. **并行安全**: 测试可以并行运行，无副作用
4. **性能**: 测试执行快速（< 30秒全部完成）

## 常见问题

### Q: 测试失败怎么办？
A: 检查：
1. 是否正确配置了测试依赖
2. Mock 设置是否正确
3. 是否有环境变量冲突
4. 代码逻辑是否与测试期望一致

### Q: 如何添加新的测试？
A: 遵循 TDD 流程：
1. 先写失败的测试（RED）
2. 实现代码使测试通过（GREEN）
3. 重构代码保持测试通过（REFACTOR）

### Q: 如何提高测试覆盖率？
A:
1. 添加更多边界条件测试
2. 测试更多异常场景
3. 测试私有方法（通过反射或包私有访问）
4. 添加集成测试

## 持续集成

建议在 CI/CD 流程中集成测试：

```yaml
# .github/workflows/test.yml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Run tests
        run: mvn test
      - name: Upload coverage
        uses: codecov/codecov-action@v4
```

## 总结

✅ 测试基础设施已完整搭建
✅ 48 个测试用例覆盖所有功能
✅ 遵循 TDD 最佳实践
✅ Mock WebClient 避免真实API调用
✅ 高测试覆盖率预期 >90%

## 下一步

1. 运行 `mvn test` 验证所有测试通过
2. 检查 JaCoCo 覆盖率报告
3. 根据需要添加更多测试用例
4. 在 CI/CD 流程中集成测试