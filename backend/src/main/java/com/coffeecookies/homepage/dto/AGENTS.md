# DTO Layer

> 29 data transfer objects. Largest module in backend.

**Generated:** 2026-04-17

---

## OVERVIEW

Pure data containers. No business logic. Request/response contracts between layers.

---

## WHERE TO LOOK

| Task | File | Notes |
|------|------|-------|
| Unified response | `Result.java` | `Result.success(data)`, `Result.error(msg)` |
| Pagination | `PageResult.java` | Wraps Spring Data Page |
| Auth flows | `LoginRequest.java`, `AuthResponse.java`, `RegisterRequest.java` | JWT token exchange |
| Gold prices | `GoldPriceDTO.java` | Price + stats + history points |
| Articles | `ArticleDTO.java`, `ArticleCreateRequest.java` | Content management |
| Trading | `TradingSignalDTO.java`, `TradingStrategyDTO.java`, `BacktestResultDTO.java` | Signal generation |
| Social | `TweetDTO.java`, `TweetStatsDTO.java` | Twitter integration |
| AI chat | `AIChatRequest.java`, `AIModelsResponse.java` | Ollama integration |
| Tools | `*Request.java`, `*Response.java` pairs | Utility endpoints |

---

## CONVENTIONS

**Lombok annotations:**
```java
@Data                    // Getters, setters, toString, equals, hashCode
@Builder                 // Builder pattern for construction
@NoArgsConstructor       // Required for Jackson deserialization
@AllArgsConstructor      // Required for @Builder
```

**Request DTOs:**
```java
@NotBlank(message = "Required")
@Size(min = 3, max = 50)
@Pattern(regexp = "^[a-zA-Z0-9]+$")
```

**Response DTOs:**
- Immutable with `@Builder`
- No validation annotations
- Include nested DTOs for relationships

**Naming:** `*Request` (input), `*Response` (output), `*DTO` (shared)

---

## RESPONSE FORMAT

```json
{
  "code": 200,
  "message": "Success",
  "data": { ... },
  "timestamp": 1713206400000
}
```

**Usage:**
```java
return ResponseEntity.ok(Result.success(dto));
return ResponseEntity.ok(Result.error("Invalid input"));
```

---

## NOTES

- All DTOs are serializable (Jackson)
- Request DTOs use Jakarta Validation (`@Valid` in controllers)
- Nested static classes for related structures (e.g., `GoldPriceDTO.PricePoint`)
- No Entity references, only primitive types and other DTOs
