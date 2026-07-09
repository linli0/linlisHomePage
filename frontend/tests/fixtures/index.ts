/**
 * Test Fixtures
 * 
 * Custom Playwright fixtures for:
 * - Accessibility testing
 * - Visual testing
 * - Semantic assertions
 * - Authenticated page state
 */

import { test as base, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import { createSemanticAssertions } from './helpers/assertions';
import { scanForA11yViolations } from './helpers/accessibility';
import { VIEWPORTS, isElementNotOverlapped, checkNoHorizontalOverflow } from './helpers/visual';

// Extend base test with custom fixtures
type MyFixtures = {
  /** Pre-authenticated page (logged in as admin) */
  authenticatedPage: void;
  /** Semantic assertions helper */
  semanticAssertions: ReturnType<typeof createSemanticAssertions>;
  /** Accessibility scan helper */
  accessibilityScan: (options?: any) => Promise<any>;
  /** Visual regression helper */
  visualHelper: {
    checkNoOverflow: () => Promise<boolean>;
    checkElementVisible: (selector: string) => Promise<boolean>;
  };
};

export const test = base.extend<MyFixtures>({
  // Authenticated page fixture
  authenticatedPage: async ({ page }, use) => {
    // Login before test
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    await use();
  },

  // Semantic assertions fixture
  semanticAssertions: async ({ page }, use) => {
    const assertions = createSemanticAssertions(page);
    await use(assertions);
  },

  // Accessibility scan fixture
  accessibilityScan: async ({ page }, use) => {
    const scan = async (options = {}) => {
      const results = await scanForA11yViolations(page, options);
      return results;
    };
    await use(scan);
  },

  // Visual helper fixture
  visualHelper: async ({ page }, use) => {
    const helper = {
      checkNoOverflow: async () => {
        const viewportSize = page.viewportSize();
        if (viewportSize) {
          return await checkNoHorizontalOverflow(page, viewportSize.width);
        }
        return true;
      },
      checkElementVisible: async (selector: string) => {
        const element = page.locator(selector);
        const visible = await element.isVisible();
        if (visible) {
          const inViewport = await isElementNotOverlapped(element);
          return inViewport;
        }
        return false;
      },
    };
    await use(helper);
  },
});

// Export expect for convenience
export { expect };

// Re-export helpers
export { createSemanticAssertions, userCanClick, formIsVisible } from './helpers/assertions';
export { scanForA11yViolations, assertNoA11yViolations, checkColorContrast } from './helpers/accessibility';
export { VIEWPORTS, captureScreenshot, isElementNotOverlapped, checkNoHorizontalOverflow } from './helpers/visual';