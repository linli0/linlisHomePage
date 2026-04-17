# Frontend Tests

> Vue 3 test infrastructure: Vitest + Vue Test Utils + MSW + Playwright

---

## OVERVIEW

39 test files across unit, component, integration, and E2E layers. MSW mocks all 8 API modules. Playwright handles accessibility and visual regression.

---

## STRUCTURE

```
tests/
├── setup.ts              # Global MSW server, localStorage mock, Pinia setup
├── mocks/
│   ├── handlers.ts       # 8 API modules (527 lines)
│   ├── browser.ts        # MSW browser setup
│   └── node.ts           # MSW node setup
├── helpers/
│   ├── assertions.ts     # Natural language: userCanClick(), formIsVisible()
│   ├── accessibility.ts  # WCAG 2.1 AA with axe-core
│   └── visual.ts         # pixelmatch, viewport testing
├── fixtures/index.ts     # Custom Playwright fixtures
├── api/*.spec.ts         # 8 API module tests
├── components/*.spec.ts  # 4 component tests
├── views/*.spec.ts       # 9 view tests
├── stores/*.test.ts      # Pinia store tests
├── unit/*.test.ts        # Utility tests
└── e2e/*.spec.ts         # User flows, a11y, responsive
```

---

## UNIQUE CONVENTIONS

**Natural Language Assertions:**
```typescript
await semantic.userCanClick('button[type="submit"]')
await semantic.formIsVisible('#login-form')
await semantic.navigationWorks('Dashboard', '/dashboard')
```

**WCAG 2.1 AA Testing:**
```typescript
await assertNoA11yViolations(page, {
  tags: ['wcag2a', 'wcag2aa'],
  generateReport: true
})
```

**Visual Regression:**
```typescript
await testAcrossViewports(page, async (viewport) => {
  await expect(page).toHaveScreenshot(`${viewport.name}.png`)
})
```

**Coverage Thresholds:**
- lines: 80%, functions: 80%, statements: 80%, branches: 75%

---

## COMMANDS

```bash
npm test              # Vitest unit tests
npm run test:coverage # Coverage report
npm run test:e2e      # Playwright E2E
npm run test:a11y     # Accessibility tests
```

---

## NOTES

- MSW intercepts all API calls in `setup.ts`
- `happy-dom` environment for component tests
- Viewports: 320px, 375px, 768px, 1280px, 1920px
- Screenshots saved to `screenshots/`
- A11y reports saved to `reports/accessibility/`
