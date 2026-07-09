# FastAPI Architecture Overview

## Stack

| Layer | Technology |
|-------|------------|
| API | Python 3.12 + FastAPI + Uvicorn |
| ORM | SQLAlchemy 2.0 |
| Database | SQLite (default), optional PostgreSQL/MySQL |
| Auth | JWT (HS256) + bcrypt |
| Frontend | Vue 3 + TypeScript + Vite (unchanged) |

## Directory Layout

```
api/
├── app/
│   ├── main.py           # Application entry
│   ├── core/             # Config, DB, security, deps
│   ├── models/           # SQLAlchemy models
│   ├── schemas/          # Pydantic DTOs + Result envelope
│   ├── routers/          # REST endpoints
│   ├── services/         # Business logic
│   └── seed.py           # Default users & sample data
├── tests/                # pytest TDD suite (27 tests)
├── Dockerfile
└── requirements.txt
```

## API Contract

All JSON endpoints return Spring Boot-compatible envelope:

```json
{ "code": 200, "message": "Success", "data": {}, "timestamp": 1710000000000 }
```

Frontend `request.ts` unwraps `data` automatically — no view changes required.

## Core Modules (MVP)

- `/api/auth` — password-only & username/password login, JWT
- `/api/articles` — public list/detail + admin CRUD
- `/api/categories`, `/api/tags` — taxonomy
- `/api/gold-price` — current/history/currencies (mock + cache)
- `/api/tools` — JSON, Base64, URL, hash, timestamp, QR PNG

## Local Development

```bash
./start.sh dev          # FastAPI :8000 + Vite :3000
cd api && PYTHONPATH=. pytest   # Run TDD suite
```

## Legacy

Spring Boot backend archived at `legacy/spring-boot/` (Java, no longer maintained).
