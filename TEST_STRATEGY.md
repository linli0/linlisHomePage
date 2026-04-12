# CoffeeCookie'sHomePage 测试策略文档

## 1. 后端测试策略 (Spring Boot 3.2)

### 1.1 单元测试策略 - 服务层 (Service Layer)

**方法**: 使用 Mockito 模拟依赖项，专注于业务逻辑验证

**推荐模式**:
```java
@SpringBootTest
class GoldPriceServiceTest {
    @MockBean
    private WebClient webClient;
    
    @MockBean  
    private GoldPriceRepository goldPriceRepository;
    
    @Autowired
    private GoldPriceService goldPriceService;
    
    @Test
    void shouldCalculateGoldPriceSuccessfully() {
        // Given - 设置模拟数据
        given(webClient.get().retrieve().bodyToMono(GoldPriceResponse.class))
            .willReturn(Mono.just(mockSuccessResponse()));
        
        given(goldPriceRepository.save(any(GoldPrice.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
            
        // When - 执行被测方法
        GoldPrice result = goldPriceService.getCurrentGoldPrice("USD");
        
        // Then - 验证结果和交互
        assertThat(result.getPrice()).isGreaterThan(0);
        then(goldPriceRepository).should().save(any(GoldPrice.class));
    }
}
```

**关键原则**:
- **隔离性**: 每个测试只关注单一服务方法
- **覆盖率**: 覆盖成功路径、失败路径、边界条件
- **模拟策略**: 
  - Repository: 使用 `@MockBean` 模拟 JPA 操作
  - 外部 API: 使用 WebClient mocking (参考现有 GoldPriceService 测试)
  - 安全上下文: 使用 `@WithMockUser` 提供认证上下文

**优先级服务测试顺序**:
1. AuthService - 认证核心逻辑
2. GoldPriceService - 已有基础，需扩展边界情况
3. ArticleService - 文章业务逻辑
4. AIService - AI 集成逻辑
5. ToolService - 工具函数逻辑

### 1.2 集成测试策略 - 控制器层 (Controller Layer)

**方法**: 使用 MockMvc 进行轻量级 HTTP 层测试 vs TestRestTemplate 进行完整 HTTP 栈测试

**推荐选择**: **MockMvc** (更快、更专注)

**MockMvc 测试示例**:
```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("admin", "admin123");
        AuthResponse response = new AuthResponse("token", mockUser());
        
        given(authService.login(any(LoginRequest.class)))
            .willReturn(response);
            
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token"))
            .andExpect(jsonPath("$.user.username").value("admin"));
    }
}
```

**TestRestTemplate 使用场景**:
- 需要测试完整的 HTTP 栈（包括过滤器链）
- 测试 JWT 认证流程
- 端到端 API 验证

**控制器测试覆盖**:
- **状态码验证**: 200, 400, 401, 403, 404, 500
- **响应结构**: JSON 结构、字段类型、嵌套对象
- **请求验证**: 参数绑定、验证注解、错误处理
- **安全测试**: 权限控制、角色验证

### 1.3 仓库层测试策略 (Repository Layer)

**方法**: **Testcontainers + PostgreSQL** (生产级测试) vs **@DataJpaTest + H2** (快速单元测试)

**推荐策略**: **混合使用**
- **开发阶段**: `@DataJpaTest` + H2 (快速反馈)
- **CI/CD**: Testcontainers + PostgreSQL (生产一致性)

**@DataJpaTest 示例**:
```java
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldFindByUsername() {
        User user = new User("test", "test@example.com", "password");
        entityManager.persistAndFlush(user);
        
        Optional<User> found = userRepository.findByUsername("test");
        
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}
```

**Testcontainers 配置**:
```java
@Testcontainers
@SpringBootTest
class IntegrationTestBase {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16-alpine");
}

class ArticleIntegrationTest extends IntegrationTestBase {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateArticleWithTags() {
        // 测试真实数据库行为，包括约束、索引、事务
    }
}
```

### 1.4 安全测试策略 (Security Testing)

**JWT 验证测试**:
```java
@WebMvcTest(ArticleController.class)
class ArticleControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ArticleService articleService;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateArticle() throws Exception {
        // 管理员可以创建文章
        mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "USER")  
    void userCannotCreateArticle() throws Exception {
        // 普通用户不能创建文章
        mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void unauthenticatedCannotAccessProtectedEndpoint() throws Exception {
        // 未认证用户访问受保护端点
        mockMvc.perform(get("/api/articles"))
            .andExpect(status().isUnauthorized());
    }
}
```

**自定义 JWT 测试工厂**:
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory.class)
public @interface WithMockJwt {
    String username() default "test";
    String[] roles() default {"USER"};
    String email() default "test@example.com";
}

// 在需要精确 JWT 令牌测试时使用
@Test
@WithMockJwt(username = "admin", roles = {"ADMIN"})
void testAdminEndpoint() {
    // 测试具体的 JWT 令牌内容
}
```

### 1.5 定时任务测试策略 (Scheduled Tasks)

**GoldPriceService 定时更新测试**:
```java
@SpringBootTest
@Import(TestConfiguration.class)
class GoldPriceServiceScheduledTaskTest {
    @Autowired
    private GoldPriceService goldPriceService;
    
    @Autowired
    private GoldPriceRepository goldPriceRepository;
    
    @Test
    void scheduledTaskShouldUpdateGoldPrices() throws InterruptedException {
        // 清理现有数据
        goldPriceRepository.deleteAll();
        
        // 触发定时任务
        goldPriceService.updateGoldPrices();
        
        // 等待异步操作完成
        Thread.sleep(100);
        
        // 验证结果
        List<GoldPrice> prices = goldPriceRepository.findAll();
        assertThat(prices).hasSize(4); // USD, CNY, EUR, GBP
    }
}
```

**关键考虑**:
- 使用 `@DirtiesContext` 避免定时任务状态污染
- 模拟外部 API 响应确保可重复性
- 验证数据库状态变化而非仅方法调用

### 1.6 外部 API 模拟策略 (External API Mocking)

**WebClient 模拟工具类** (基于现有实现):
```java
public class WebClientMockUtil {
    public static void mockWebClientSuccess(WebClient webClient, 
            String responseBody, Class<?> responseType) {
        BDDMockito.given(webClient.get()
                .uri(Mockito.anyString())
                .retrieve()
                .bodyToMono(responseType))
            .willReturn(Mono.just(JsonUtils.fromJson(responseBody, responseType)));
    }
    
    public static void mockWebClientFailure(WebClient webClient) {
        BDDMockito.given(webClient.get()
                .uri(Mockito.anyString())
                .retrieve()
                .bodyToMono(Mockito.any(Class.class)))
            .willThrow(new WebClientResponseException(
                500, "Internal Server Error", null, null, null, null));
    }
}
```

**外部服务模拟范围**:
- **MetalpriceAPI**: 金价数据获取
- **ExchangeRate-API**: 货币转换率
- **Ollama**: AI 模型列表和聊天接口

## 2. 前端测试策略 (Vue 3 + TypeScript)

### 2.1 单元测试策略 - 组合式函数和工具 (Composables & Utilities)

**方法**: 使用 Vitest 直接测试函数逻辑

**Vitest 配置** (`vitest.config.ts`):
```typescript
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'happy-dom',
    include: ['src/**/*.{test,spec}.{js,ts}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/main.ts',
        '**/*.d.ts',
        '**/*.config.*'
      ]
    },
    setupFiles: ['./src/test/setup.ts']
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
```

**工具函数测试示例** (`format.test.ts`):
```typescript
import { describe, it, expect } from 'vitest'
import { formatDate, formatPrice } from '@/utils/format'

describe('format utils', () => {
  it('formats date correctly', () => {
    const date = new Date('2026-04-12T10:30:00Z')
    expect(formatDate(date)).toBe('2026-04-12')
  })
  
  it('formats price with 2 decimals', () => {
    expect(formatPrice(1234.567)).toBe('1,234.57')
    expect(formatPrice(0)).toBe('0.00')
  })
})
```

### 2.2 组件测试策略 - 视图和组件 (Views & Components)

**方法**: 使用 Vue Test Utils + Vitest

**安装依赖**:
```bash
npm install -D @vue/test-utils happy-dom
```

**组件测试示例** (`PriceChart.test.ts`):
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PriceChart from '@/components/PriceChart.vue'

describe('PriceChart', () => {
  const mockPriceHistory = [
    { timestamp: '2026-04-12T00:00:00Z', price: 2000 },
    { timestamp: '2026-04-11T00:00:00Z', price: 1950 }
  ]

  it('renders chart with price data', () => {
    const wrapper = mount(PriceChart, {
      props: {
        priceHistory: mockPriceHistory,
        currency: 'USD'
      }
    })

    expect(wrapper.find('[data-testid="chart-container"]').exists()).toBe(true)
    expect(wrapper.html()).toContain('2000')
  })

  it('handles empty price history', () => {
    const wrapper = mount(PriceChart, {
      props: {
        priceHistory: [],
        currency: 'USD'
      }
    })

    expect(wrapper.find('[data-testid="no-data"]').exists()).toBe(true)
  })
})
```

**视图测试示例** (`LoginView.test.ts`):
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import LoginView from '@/views/LoginView.vue'
import { useAuthStore } from '@/stores/auth'

describe('LoginView', () => {
  it('submits login form', async () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    const store = useAuthStore()
    const passwordInput = wrapper.find('[data-testid="password-input"]')
    const submitButton = wrapper.find('[data-testid="submit-button"]')

    await passwordInput.setValue('admin123')
    await submitButton.trigger('click')

    expect(store.login).toHaveBeenCalledWith({ password: 'admin123' })
  })

  it('shows error message on failed login', async () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [
          createTestingPinia({
            initialState: {
              auth: { error: 'Invalid password' }
            }
          })
        ]
      }
    })

    expect(wrapper.find('[data-testid="error-message"]').text()).toBe('Invalid password')
  })
})
```

### 2.3 Store 测试策略 - Pinia 状态管理

**方法**: 使用 `@pinia/testing` 创建隔离的 store 实例

**Auth Store 测试示例** (`auth.test.ts`):
```typescript
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import * as authApi from '@/api/auth'

vi.mock('@/api/auth')

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('logs in successfully', async () => {
    const mockResponse = {
      token: 'mock-token',
      user: { id: 1, username: 'admin', role: 'ADMIN' }
    }
    vi.mocked(authApi.login).mockResolvedValue(mockResponse)

    const store = useAuthStore()
    await store.login({ password: 'admin123' })

    expect(store.isAuthenticated).toBe(true)
    expect(store.user?.username).toBe('admin')
    expect(localStorage.getItem('token')).toBe('mock-token')
  })

  it('handles login failure', async () => {
    vi.mocked(authApi.login).mockRejectedValue(new Error('Invalid credentials'))

    const store = useAuthStore()
    await store.login({ password: 'wrong' })

    expect(store.error).toBe('Invalid credentials')
    expect(store.isAuthenticated).toBe(false)
  })

  it('logs out and clears state', () => {
    const store = useAuthStore()
    store.$state.token = 'mock-token'
    store.$state.user = { id: 1, username: 'admin', role: 'ADMIN' }

    store.logout()

    expect(store.token).toBe(null)
    expect(store.user).toBe(null)
    expect(localStorage.getItem('token')).toBeNull()
  })
})
```

### 2.4 API 模块测试策略 (Axios Mocking)

**方法**: 使用 Vitest 的 `vi.mock` 模拟 Axios

**API 测试设置** (`src/test/setup.ts`):
```typescript
// 全局模拟 Axios
vi.mock('axios', () => {
  const axiosInstance = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    create: vi.fn(() => axiosInstance),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    }
  }
  return {
    default: axiosInstance,
    ...axiosInstance
  }
})
```

**Gold Price API 测试示例** (`goldPrice.test.ts`):
```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import { getCurrentPrice, getPriceHistory } from '@/api/goldPrice'

vi.mock('axios')

describe('Gold Price API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('fetches current gold price', async () => {
    const mockResponse = { data: { price: 2000, currency: 'USD' } }
    vi.mocked(axios.get).mockResolvedValue(mockResponse)

    const result = await getCurrentPrice('USD')

    expect(axios.get).toHaveBeenCalledWith('/api/gold-price/current', {
      params: { currency: 'USD' }
    })
    expect(result.price).toBe(2000)
  })

  it('fetches price history', async () => {
    const mockHistory = [
      { timestamp: '2026-04-12T00:00:00Z', price: 2000 },
      { timestamp: '2026-04-11T00:00:00Z', price: 1950 }
    ]
    vi.mocked(axios.get).mockResolvedValue({ data: mockHistory })

    const result = await getPriceHistory('USD', 2)

    expect(axios.get).toHaveBeenCalledWith('/api/gold-price/history', {
      params: { currency: 'USD', days: 2 }
    })
    expect(result).toHaveLength(2)
  })
})
```

### 2.5 路由测试策略 (Router Testing)

**方法**: 测试导航守卫和路由保护

**路由测试示例**:
```typescript
import { describe, it, expect, vi } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import { createTestingPinia } from '@pinia/testing'
import { routes } from '@/router'
import { useAuthStore } from '@/stores/auth'

describe('Router Guards', () => {
  it('redirects unauthenticated users from protected routes', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes
    })

    const pinia = createTestingPinia({
      initialState: {
        auth: { token: null, user: null }
      }
    })
    const store = useAuthStore()

    // 模拟导航到受保护路由
    const to = { path: '/profile', meta: { requiresAuth: true } }
    const from = { path: '/' }
    const next = vi.fn()

    // 执行导航守卫逻辑
    if (to.meta.requiresAuth && !store.isAuthenticated) {
      next({ path: '/login', query: { redirect: to.path } })
    } else {
      next()
    }

    expect(next).toHaveBeenCalledWith({
      path: '/login',
      query: { redirect: '/profile' }
    })
  })

  it('allows authenticated users to access protected routes', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes
    })

    const pinia = createTestingPinia({
      initialState: {
        auth: { token: 'mock-token', user: { id: 1, username: 'admin' } }
      }
    })
    const store = useAuthStore()

    const to = { path: '/profile', meta: { requiresAuth: true } }
    const from = { path: '/' }
    const next = vi.fn()

    if (to.meta.requiresAuth && !store.isAuthenticated) {
      next({ path: '/login', query: { redirect: to.path } })
    } else {
      next()
    }

    expect(next).toHaveBeenCalledWith()
  })
})
```

### 2.6 E2E 测试策略 (Playwright)

**方法**: 使用 Playwright 进行端到端测试

**Playwright 配置** (`playwright.config.ts`):
```typescript
import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure'
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI
  }
})
```

**E2E 测试示例** (`auth.e2e.spec.ts`):
```typescript
import { test, expect } from '@playwright/test'

test.describe('Authentication Flow', () => {
  test('user can login and access profile', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login')
    
    // 填写密码并提交
    await page.getByTestId('password-input').fill('admin123')
    await page.getByTestId('submit-button').click()
    
    // 验证重定向到首页
    await expect(page).toHaveURL('/')
    await expect(page.getByText('Welcome, admin')).toBeVisible()
    
    // 导航到个人资料页面
    await page.getByRole('link', { name: 'Profile' }).click()
    await expect(page).toHaveURL('/profile')
    
    // 验证个人资料信息显示
    await expect(page.getByText('admin')).toBeVisible()
    await expect(page.getByText('admin@example.com')).toBeVisible()
  })

  test('unauthenticated user redirected from profile', async ({ page }) => {
    // 直接访问受保护页面
    await page.goto('/profile')
    
    // 验证重定向到登录页面
    await expect(page).toHaveURL(/login/)
    await expect(page.getByText('Please log in')).toBeVisible()
  })
})
```

**金价追踪 E2E 测试** (`gold-price.e2e.spec.ts`):
```typescript
import { test, expect } from '@playwright/test'

test.describe('Gold Price Tracking', () => {
  test('displays current gold price and chart', async ({ page }) => {
    await page.goto('/gold-price')
    
    // 验证当前价格显示
    await expect(page.getByTestId('current-price')).toBeVisible()
    await expect(page.getByText('USD')).toBeVisible()
    
    // 验证图表容器存在
    await expect(page.getByTestId('chart-container')).toBeVisible()
    
    // 切换货币
    await page.getByRole('combobox').selectOption('CNY')
    await expect(page.getByText('CNY')).toBeVisible()
    
    // 切换时间范围
    await page.getByRole('button', { name: '30 Days' }).click()
    await expect(page.getByTestId('chart-container')).toBeVisible()
  })
})
```

## 3. 测试组织结构

### 3.1 后端目录结构

```
backend/
├── src/
│   ├── main/
│   │   └── java/com/coffeecookies/homepage/
│   └── test/
│       ├── java/com/coffeecookies/homepage/
│       │   ├── config/           # 测试配置
│       │   ├── service/          # 服务层单元测试
│       │   ├── controller/       # 控制器集成测试  
│       │   ├── repository/       # 仓库层测试
│       │   └── security/         # 安全测试
│       └── resources/
│           ├── application-test.yml  # 测试配置文件
│           └── sql/              # SQL 测试数据
└── pom.xml
```

### 3.2 前端目录结构

```
frontend/
├── src/
│   ├── components/
│   │   ├── PriceChart.vue
│   │   └── PriceChart.test.ts    # 组件测试 (同目录)
│   ├── views/
│   │   ├── LoginView.vue
│   │   └── LoginView.test.ts     # 视图测试 (同目录)
│   ├── stores/
│   │   ├── auth.ts
│   │   └── auth.test.ts          # Store 测试 (同目录)
│   ├── api/
│   │   ├── goldPrice.ts
│   │   └── goldPrice.test.ts     # API 测试 (同目录)
│   ├── utils/
│   │   ├── format.ts
│   │   └── format.test.ts        # 工具测试 (同目录)
│   └── test/
│       └── setup.ts              # 全局测试设置
├── e2e/                          # E2E 测试
│   ├── auth.e2e.spec.ts
│   ├── gold-price.e2e.spec.ts
│   └── articles.e2e.spec.ts
├── vitest.config.ts              # Vitest 配置
└── playwright.config.ts          # Playwright 配置
```

### 3.3 文件命名约定

**后端**:
- 单元测试: `{ClassName}Test.java`
- 集成测试: `{ClassName}IntegrationTest.java`
- 测试工具: `{ClassName}TestHelper.java`, `{ClassName}MockUtil.java`

**前端**:
- 单元/组件测试: `{FileName}.test.ts`
- E2E 测试: `{FeatureName}.e2e.spec.ts`
- 测试设置: `setup.ts`

### 3.4 测试数据管理策略

**后端测试数据**:
- **内存数据库**: H2 用于快速单元测试
- **Testcontainers**: PostgreSQL 用于集成测试
- **工厂模式**: 使用 Lombok Builder 创建测试实体
- **SQL 脚本**: 复杂数据场景使用 SQL 初始化脚本

**前端测试数据**:
- **Mock 数据**: 在测试文件中定义常量
- **工厂函数**: 创建可重用的测试数据生成器
- **TypeScript 接口**: 确保 mock 数据符合实际 API 响应结构

```typescript
// frontend/src/test/mockData.ts
export const mockGoldPrice = {
  price: 2000.50,
  currency: 'USD',
  timestamp: '2026-04-12T10:30:00Z'
}

export const mockUser = {
  id: 1,
  username: 'admin',
  email: 'admin@example.com',
  role: 'ADMIN',
  createdAt: '2026-04-08T00:00:00Z'
}

export const createMockArticle = (overrides = {}) => ({
  id: 1,
  title: 'Test Article',
  content: 'Test content',
  published: true,
  viewCount: 0,
  category: { id: 1, name: '技术' },
  tags: [{ id: 1, name: 'Java', color: '#3b82f6' }],
  author: mockUser,
  createdAt: '2026-04-08T00:00:00Z',
  updatedAt: '2026-04-08T00:00:00Z',
  ...overrides
})
```

## 4. 覆盖率目标和指标

### 4.1 行覆盖率目标

| 层级 | 目标覆盖率 | 关键区域 |
|------|------------|----------|
| **服务层** | 85%+ | AuthService, GoldPriceService, ArticleService |
| **控制器层** | 80%+ | 所有控制器，重点关注错误处理 |
| **仓库层** | 75%+ | 自定义查询方法 |
| **实体/DTO** | 50%+ | 构造函数、Builder 方法 |
| **前端组件** | 70%+ | 业务逻辑、事件处理 |
| **前端 Store** | 85%+ | Actions、Getters、状态变更 |
| **前端 API** | 80%+ | 请求/响应处理、错误处理 |

### 4.2 分支覆盖率目标

- **服务层**: 80%+ (覆盖所有 if/else、try/catch 路径)
- **控制器层**: 75%+ (覆盖所有 HTTP 状态码路径)
- **前端逻辑**: 70%+ (覆盖所有条件分支)

### 4.3 关键路径覆盖率要求

**必须 100% 覆盖的关键路径**:
- **认证流程**: 登录、登出、JWT 验证
- **金价获取**: MetalpriceAPI 集成、货币转换、错误处理
- **文章管理**: CRUD 操作、权限控制、视图计数
- **安全边界**: 权限检查、输入验证、XSS 防护
- **数据一致性**: 事务边界、并发处理

### 4.4 性能指标

**测试执行时间目标**:
- **单元测试**: < 5ms per test (95% percentile)
- **组件测试**: < 50ms per test (95% percentile)  
- **集成测试**: < 500ms per test (95% percentile)
- **E2E 测试**: < 10s per test (95% percentile)

**整体测试套件**:
- **本地开发**: < 30 秒 (单元 + 组件测试)
- **CI/CD**: < 5 分钟 (完整测试套件)

## 5. 工具配置建议

### 5.1 Maven Surefire/Failsafe 配置

**pom.xml 配置**:
```xml
<properties>
    <maven.surefire.plugin.version>3.2.5</maven.surefire.plugin.version>
    <maven.failsafe.plugin.version>3.2.5</maven.failsafe.plugin.version>
</properties>

<build>
    <plugins>
        <!-- 单元测试 (Surefire) -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>${maven.surefire.plugin.version}</version>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                </includes>
                <excludes>
                    <exclude>**/*IntegrationTest.java</exclude>
                </excludes>
                <systemPropertyVariables>
                    <spring.profiles.active>test</spring.profiles.active>
                </systemPropertyVariables>
            </configuration>
        </plugin>
        
        <!-- 集成测试 (Failsafe) -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>${maven.failsafe.plugin.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <includes>
                    <include>**/*IntegrationTest.java</include>
                </includes>
                <systemPropertyVariables>
                    <spring.profiles.active>test</spring.profiles.active>
                </systemPropertyVariables>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 5.2 Vitest 配置与覆盖率报告

**vitest.config.ts** (完整配置):
```typescript
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'happy-dom',
    include: ['src/**/*.{test,spec}.{js,ts}'],
    exclude: [
      'node_modules',
      'dist',
      '.idea',
      '.git',
      '.cache',
      '**/*.d.ts'
    ],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/main.ts',
        'src/App.vue',
        '**/*.d.ts',
        '**/*.config.*',
        'src/assets/**',
        'src/router/index.ts' // 路由配置通常不需要高覆盖率
      ],
      thresholds: {
        statements: 70,
        branches: 65,
        functions: 70,
        lines: 70
      }
    },
    setupFiles: ['./src/test/setup.ts'],
    reporters: ['verbose', 'html'],
    outputFile: {
      html: './coverage/vitest-report.html'
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
```

### 5.3 Playwright 多浏览器配置

**playwright.config.ts** (生产级配置):
```typescript
import { defineConfig, devices } from '@playwright/test'

const PORT = process.env.PORT || 3000
const BASE_URL = process.env.BASE_URL || `http://localhost:${PORT}`

export default defineConfig({
  testDir: './e2e',
  timeout: 30 * 1000,
  expect: {
    timeout: 5000
  },
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: process.env.CI ? 'github' : [['html', { open: 'never' }]],
  use: {
    baseURL: BASE_URL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure'
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] }
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] }
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] }
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] }
    }
  ],
  webServer: {
    command: process.env.CI 
      ? 'npm run preview' 
      : 'npm run dev',
    url: BASE_URL,
    timeout: 120 * 1000,
    reuseExistingServer: !process.env.CI
  }
})
```

### 5.4 CI/CD 集成点

**GitHub Actions 配置** (`.github/workflows/test.yml`):
```yaml
name: Test

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_PASSWORD: test
          POSTGRES_DB: homepage_test
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Cache Maven packages
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2
      - name: Run backend tests
        run: mvn test
      - name: Run backend integration tests
        run: mvn verify
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/homepage_test
          SPRING_DATASOURCE_USERNAME: postgres
          SPRING_DATASOURCE_PASSWORD: test

  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      - name: Install dependencies
        run: npm ci
        working-directory: frontend
      - name: Run unit and component tests
        run: npm test -- --coverage
        working-directory: frontend
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          files: ./frontend/coverage/lcov.info
          flags: frontend
      - name: Setup Playwright
        run: npx playwright install-deps
        working-directory: frontend
      - name: Run E2E tests
        run: npm run test:e2e
        working-directory: frontend

  quality-gate:
    needs: [backend-test, frontend-test]
    runs-on: ubuntu-latest
    steps:
      - name: Quality gate passed
        run: echo "All tests passed!"
```

## 6. 实施优先级顺序

### 6.1 高优先级测试区域

**安全与认证 (Week 1)**:
- ✅ AuthService 单元测试 (100% 覆盖)
- ✅ AuthController 集成测试 (所有端点 + 错误场景)
- ✅ JWT 安全测试 (权限控制、未授权访问)
- ✅ LoginView 和 ProfileView 组件测试

**金价追踪 (Week 2)**:
- ✅ 扩展 GoldPriceService 测试 (边界条件、错误恢复)
- ✅ GoldPriceController 集成测试
- ✅ PriceChart 组件测试
- ✅ GoldPriceView E2E 测试
- ✅ 外部 API 模拟策略完善

### 6.2 中优先级测试区域

**文章管理 (Week 3-4)**:
- ArticleService 单元测试
- ArticleController 集成测试 (公共读取 + 管理员写入)
- ArticlesView 和 ArticleDetailView 组件测试
- 文章管理 E2E 测试

**工具箱 (Week 4)**:
- ToolService 单元测试
- ToolController 集成测试
- ToolsView 组件测试
- 工具功能 E2E 测试

### 6.3 低优先级测试区域

**分类和标签 (Week 5)**:
- CategoryService/TagService 单元测试
- CategoryController/TagController 集成测试
- 相关组件测试

**AI 聊天 (Week 5-6)**:
- AIService 单元测试 (流式响应处理)
- AIController 集成测试
- AIChat 组件测试
- AI 功能 E2E 测试

### 6.4 测试实施依赖关系

**基础设施依赖**:
1. **测试框架设置** → 所有其他测试
   - 后端: Maven Surefire/Failsafe 配置
   - 前端: Vitest + Vue Test Utils 安装
   - E2E: Playwright 安装和配置

2. **共享工具类** → 服务层测试
   - WebClientMockUtil (后端)
   - Mock 数据工厂 (前端)

**业务逻辑依赖**:
1. **AuthService 测试** → 所有需要认证的测试
2. **GoldPriceService 测试** → 金价相关 E2E 测试
3. **Store 测试** → 组件测试 (需要模拟状态)

**实施顺序建议**:
```
Week 1: 测试基础设施 + AuthService + Auth UI
Week 2: GoldPriceService 扩展 + GoldPrice UI + E2E 基础
Week 3: ArticleService + Articles UI
Week 4: ToolService + Tools UI + Article E2E
Week 5: Category/Tag + AI Service
Week 6: AI UI + 完整 E2E 套件 + 覆盖率优化
```

**并行实施机会**:
- 后端服务测试和前端组件测试可以并行开发
- 不同模块的 E2E 测试可以独立开发
- 测试数据工厂可以提前准备供团队共享

---

*本文档基于 CoffeeCookie'sHomePage 项目当前架构制定，将随着项目演进持续更新。*