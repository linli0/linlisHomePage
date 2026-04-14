import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright E2E Testing Configuration
 * 
 * Features:
 * - Multi-viewport testing (320px - 1920px)
 * - Accessibility testing with axe-core
 * - Visual regression testing
 * - Natural language assertions
 * - HTML reports
 */

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list'],
    ['json', { outputFile: 'test-results/results.json' }]
  ],
  
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    // Desktop browsers (default)
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        viewport: { width: 1920, height: 1080 },
      }
    },
    {
      name: 'firefox',
      use: { 
        ...devices['Desktop Firefox'],
        viewport: { width: 1920, height: 1080 },
      }
    },
    {
      name: 'webkit',
      use: { 
        ...devices['Desktop Safari'],
        viewport: { width: 1920, height: 1080 },
      }
    },
    
    // Responsive viewports (for responsive tests)
    {
      name: 'viewport-1920',
      use: { viewport: { width: 1920, height: 1080 } },
      testMatch: /.*responsive.*\.spec\.ts/,
    },
    {
      name: 'viewport-1280',
      use: { viewport: { width: 1280, height: 720 } },
      testMatch: /.*responsive.*\.spec\.ts/,
    },
    {
      name: 'viewport-768',
      use: { viewport: { width: 768, height: 1024 } },
      testMatch: /.*responsive.*\.spec\.ts/,
    },
    {
      name: 'viewport-375',
      use: { viewport: { width: 375, height: 667 } },
      testMatch: /.*responsive.*\.spec\.ts/,
    },
    {
      name: 'viewport-320',
      use: { viewport: { width: 320, height: 568 } },
      testMatch: /.*responsive.*\.spec\.ts/,
    },
    
    // Mobile devices
    {
      name: 'iphone-13',
      use: { ...devices['iPhone 13'] },
      testMatch: /.*mobile.*\.spec\.ts/,
    },
    {
      name: 'ipad-pro',
      use: { ...devices['iPad Pro'] },
      testMatch: /.*mobile.*\.spec\.ts/,
    },
    
    // Accessibility-focused project
    {
      name: 'accessibility',
      use: { 
        ...devices['Desktop Chrome'],
        viewport: { width: 1280, height: 720 },
      },
      testMatch: /.*accessibility.*\.spec\.ts/,
    },
  ],

  // Run local dev server before tests
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  }
})