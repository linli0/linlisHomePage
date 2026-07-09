# Service Layer

> 15 service classes. Business logic, external API integration, transaction management.

**Generated:** 2026-04-17

---

## OVERVIEW

Service layer implements business logic between controllers and repositories. Key responsibilities: external API calls, scheduled tasks, financial calculations, AI integration, WebSocket streaming.

**Core Services:**
- **GoldPriceService** - MetalpriceAPI integration, currency conversion, price statistics
- **AIService** - Ollama LLM integration with auto-switching local/remote URLs
- **AuthService** - JWT authentication, password-only login support
- **XiaomiSpeakerService** - Cloud + local protocol control for Xiaomi speakers
- **TwitterStreamService** - Twitter API streaming with WebSocket broadcast

**Trading Services:**
- **TechnicalIndicatorService** - SMA, EMA, MACD, RSI, Bollinger Bands, ATR
- **SignalGenerationService** - Trading signal generation with scheduled tasks
- **BacktestingService** - Strategy backtesting with performance metrics

---

## WHERE TO LOOK

| Task | File | Notes |
|------|------|-------|
| Modify gold price logic | `GoldPriceService.java` | MetalpriceAPI, exchange rates, statistics |
| AI chat/streaming | `AIService.java` | Ollama integration, auto-switch URLs |
| Authentication | `AuthService.java` | JWT, password-only login |
| Xiaomi speaker | `XiaomiSpeakerService.java` | Cloud API + local LAN control |
| Twitter streaming | `TwitterStreamService.java` | WebSocket, exponential backoff |
| Technical indicators | `TechnicalIndicatorService.java` | All indicator calculations |
| Trading signals | `SignalGenerationService.java` | Signal generation, scheduled tasks |
| Backtesting | `BacktestingService.java` | Performance metrics, trade simulation |
| Utility tools | `ToolService.java` | JSON, Base64, URL, hash, QR code |
| Article CRUD | `ArticleService.java` | Article management |
| Category/Tag | `CategoryService.java`, `TagService.java` | Classification |
| Tweet polling | `TweetPollingService.java` | Scheduled tweet fetching |
| Truth Social | `TruthSocialService.java` | Truth Social integration |
| WebSocket tweets | `TweetWebSocketService.java` | Real-time tweet broadcast |

---

## COMPLEXITY HOTSPOTS

### XiaomiSpeakerService (449 lines)
**Issue:** Dual protocol handling (cloud + local) mixed in one class.
**Refactor:** Extract `XiaomiCloudClient` and `XiaomiLocalClient`.

### TwitterStreamService (401 lines)
**Issue:** WebSocket streaming + tweet parsing + backoff logic intertwined.
**Refactor:** Extract `TweetParser`, `StreamConnectionManager`.

### GoldPriceService (351 lines)
**Issue:** API fetching, currency conversion, statistics calculation mixed.
**Refactor:** Extract `ExchangeRateService`, `PriceStatisticsCalculator`.

### BacktestingService
**Inner classes:** `PerformanceMetrics`, `TradeResult` - consider extracting to DTOs.

---

## CONVENTIONS

### Service Pattern
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class SomeService {
    private final SomeRepository repository;
    private final WebClient.Builder webClientBuilder;
    
    @Transactional
    public Result doSomething(Request req) { ... }
    
    @Transactional(readOnly = true)
    public List<Data> getData() { ... }
}
```

### Scheduled Tasks
```java
@Scheduled(fixedRate = 60000)    // Every minute
@Scheduled(cron = "0 0 * * * *") // Every hour
```

### External API Calls
```java
// WebClient for reactive HTTP
webClient.get()
    .uri("/api/endpoint")
    .retrieve()
    .bodyToMono(Response.class)
    .block(Duration.ofSeconds(10));
```

### Financial Calculations
```java
// Always use BigDecimal with explicit scale
BigDecimal result = value.multiply(rate)
    .setScale(2, RoundingMode.HALF_UP);
```

---

## NOTES

- **Transaction boundaries:** Use `@Transactional` on service methods, not controllers
- **WebClient:** Pre-configured builder injected, set baseUrl per request
- **Scheduled tasks:** All use `@Scheduled`, check `enabled` flags before execution
- **Error handling:** Log errors with `log.error()`, return meaningful messages
- **Reactive:** `AIService` uses `StreamingResponseBody` for SSE streaming
- **Inner classes:** `BacktestingService` contains `PerformanceMetrics`, `TradeResult`
- **Auto-switching:** `AIService` automatically switches between local/remote Ollama
- **Dual protocols:** `XiaomiSpeakerService` supports both cloud API and local LAN
