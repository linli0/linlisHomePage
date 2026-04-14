# Frontend Documentation

## 📋 Table of Contents

- [Component Reference](component-reference.md)
- [API Integration](api-integration.md)
- [State Management](state-management.md)
- [Routing](routing.md)
- [Styling](styling.md)
- [Build Configuration](build-configuration.md)
- [Development Workflow](development-workflow.md)

## 🏗️ Architecture Overview

The frontend is built with **Vue 3.4.15** using the Composition API and follows modern TypeScript best practices.

### Technology Stack
- **Framework**: Vue 3.4.15 + Composition API (`<script setup>`)
- **Language**: TypeScript 5.3.3 (strict mode enabled)
- **Build Tool**: Vite 5.0.11
- **State Management**: Pinia 2.1
- **Routing**: Vue Router 4.2
- **Styling**: Tailwind CSS 3.4 + custom design system
- **HTTP Client**: Axios 1.6 with JWT interceptor
- **Charts**: Chart.js 4.4 + vue-chartjs + lightweight-charts 5.1.0
- **Trading**: trading-signals 7.4.3
- **WebSocket**: STOMP over WebSocket
- **Markdown**: marked 18.0 + DOMPurify 3.3

### Component Structure
- **Views**: 11 page-level components in `src/views/`
  - AIView, ArticleDetailView, ArticlesView, GoldPriceView, HomeView, LoginView, ProfileView, RegisterView, ToolsView, TweetsView, XiaomiView
- **Components**: 8 reusable UI components in `src/components/`
  - AIChat, FooterBar, IndicatorOverlay, KLineChart, NavigationBar, PriceChart, TweetCard, TweetMetricsChart
- **Stores**: Pinia store for authentication state in `src/stores/`
  - auth.ts
- **Composables**: Custom Vue composables in `src/composables/`
  - useTweetWebSocket.ts
- **API Modules**: 8 feature-based API services in `src/api/`
  - ai.ts, article.ts, auth.ts, goldPrice.ts, tools.ts, tradingApi.ts, tweets.ts, xiaomi.ts
- **Utilities**: Shared functions and HTTP client in `src/utils/`
  - request.ts, format.ts
- **Types**: TypeScript type definitions in `src/types/`
  - tweet.ts

## 🔧 Build and Development

### Prerequisites
- Node.js 20+
- npm 8+

### Installation
```bash
cd frontend
npm install
```

### Development Server
```bash
npm run dev
```
- Serves on http://localhost:3000
- Proxy configured to backend on http://localhost:8080
- Hot module replacement (HMR) enabled

### Production Build
```bash
npm run build
```
- Outputs to `dist/` directory
- Minified and optimized for production
- Source maps included for debugging

### Preview Production Build
```bash
npm run preview
```

## 🎨 Design System

### Color Palette
- **Primary**: Sky blue (`#0ea5e9`)
- **Gold**: Amber gold (`#f59e0b`)  
- **Accent**: Purple (`#d946ef`)
- **Surface**: Neutral gray backgrounds

### Custom Components
- **Buttons**: `.btn`, `.btn-primary`, `.btn-gold`
- **Cards**: `.card`, `.card-hover`, `.card-glass`
- **Inputs**: `.input`, `.input-glass`
- **Badges**: `.badge`, `.badge-primary`

### Animations
- **Float**: Gentle floating animation
- **Gradient**: Animated gradient backgrounds
- **Shimmer**: Loading shimmer effect
- **Slide-up**: Smooth entrance animations
- **Fade-in**: Opacity transitions
- **Scale-in**: Size-based entrance
- **Bounce-subtle**: Subtle bounce effects
- **Wiggle**: Playful wiggle animations

## 🧪 Testing

The frontend implements a comprehensive testing strategy:

- **Unit Tests**: Utilities and composables with Vitest
- **Component Tests**: Views and components with Vue Test Utils  
- **Store Tests**: Pinia store actions and getters
- **E2E Tests**: Critical user flows with Playwright

Testing dependencies will be added as part of the test framework implementation.

## 🚀 Deployment

The frontend is designed to work seamlessly with the Spring Boot backend:

- Production build outputs to `frontend/dist/`
- Spring Boot serves static files from the `dist/` directory
- Single deployment artifact containing both frontend and backend
- No separate frontend server required in production

## 📱 Responsive Design

The application is fully responsive and supports:

- **Mobile**: 320px - 767px
- **Tablet**: 768px - 1023px  
- **Desktop**: 1024px+

All components adapt to different screen sizes using Tailwind's responsive utilities.