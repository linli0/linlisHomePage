# Domestic Gold Price (AU9999) Implementation Plan

> **For agentic workers:** Implement task-by-task. Steps use checkbox syntax.

**Goal:** Show 国内金价（黄金9999 · 元/克） from Sina, with daily auto-refresh and manual refresh.

**Architecture:** Backend fetches `hq.sinajs.cn/list=gds_AU9999`, stores CNY/g in `GoldPrice.price_usd`, exposes `POST /refresh` + lifespan daily job at 09:30 Asia/Shanghai. Frontend drops 60s polling and updates copy.

**Tech Stack:** FastAPI, httpx, SQLite/SQLAlchemy, Vue 3

## Global Constraints

- No API key; Sina public quote only
- Unit: 元/克; label: 国内金价 / 黄金9999
- No 60s frontend poll
- Reuse `price_usd` column for CNY/g base (no migration)

---

### Task 1: Backend fetch + refresh + current semantics

**Files:** `api/app/services/gold_price.py`, `api/app/routers/gold_price.py`, `api/tests/test_gold_price.py`, `api/app/main.py`

- [x] Parse/fetch/refresh + CNY-base current
- [x] POST `/api/gold-price/refresh`
- [x] Lifespan stale+daily scheduler
- [x] Tests with mocked Sina body

### Task 2: Frontend copy + manual refresh

**Files:** `frontend/src/api/goldPrice.ts`, `HomeView.vue`, `GoldPriceView.vue`

- [x] `goldPriceApi.refresh()`
- [x] Remove auto poll; refresh calls POST
- [x] Copy → 国内金价 / 元/克
