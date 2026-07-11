/**
 * Natural Language Assertion Helpers
 * 
 * User-focused assertions that describe what users can do,
 * not code details.
 * 
 * Examples:
 * - "user can click submit button"
 * - "form is visible and accessible"
 * - "navigation works correctly"
 */

import { Page, Locator, expect } from '@playwright/test';
import { isElementNotOverlapped, checkElementVisibility, isElementInViewport } from './visual';

/**
 * Semantic Assertions Class
 * Provides natural language assertion methods
 */
export class SemanticAssertions {
  constructor(private page: Page) {}

  /**
   * Assert user can click an element
   * Checks: visible, enabled, not overlapped, in viewport
   */
  async userCanClick(selector: string, description?: string): Promise<void> {
    const element = this.page.locator(selector);
    const desc = description || `user should be able to click ${selector}`;

    await test.step(desc, async () => {
      // Element is visible
      await expect(element, `${selector} should be visible`).toBeVisible();
      
      // Element is enabled
      await expect(element, `${selector} should be enabled`).toBeEnabled();
      
      // Element is in viewport
      const inViewport = await isElementInViewport(element);
      expect(inViewport, `${selector} should be in viewport`).toBe(true);
      
      // Element is not overlapped
      const notOverlapped = await isElementNotOverlapped(element);
      expect(notOverlapped, `${selector} should not be overlapped by other elements`).toBe(true);
      
      // Element has proper visibility styles
      const visibility = await checkElementVisibility(element);
      expect(visibility.isVisible, `${selector} should have proper visibility CSS`).toBe(true);
      expect(parseFloat(visibility.opacity), `${selector} opacity should be > 0`).toBeGreaterThan(0);
    });
  }

  /**
   * Assert form is visible and all inputs are accessible
   */
  async formIsVisible(formSelector: string): Promise<void> {
    const form = this.page.locator(formSelector);

    await test.step(`form ${formSelector} should be visible`, async () => {
      // Form is visible
      await expect(form, 'form should be visible').toBeVisible();
      
      // Form is in viewport
      const inViewport = await isElementInViewport(form);
      expect(inViewport, 'form should be in viewport').toBe(true);
      
      // All inputs are visible and enabled
      const inputs = form.locator('input:not([type="hidden"]), select, textarea');
      const count = await inputs.count();
      
      for (let i = 0; i < count; i++) {
        const input = inputs.nth(i);
        await expect(input, `form input ${i} should be visible`).toBeVisible();
        await expect(input, `form input ${i} should be enabled`).toBeEnabled();
        
        // Check input has accessible label
        const id = await input.getAttribute('id');
        if (id) {
          const label = this.page.locator(`label[for="${id}"]`);
          const hasLabel = await label.count() > 0;
          const ariaLabel = await input.getAttribute('aria-label');
          const ariaLabelledBy = await input.getAttribute('aria-labelledby');
          
          expect(
            hasLabel || ariaLabel || ariaLabelledBy,
            `form input ${i} should have an accessible label`
          ).toBeTruthy();
        }
      }
      
      // Submit button is accessible
      const submitButton = form.locator('button[type="submit"], input[type="submit"]');
      if (await submitButton.count() > 0) {
        await this.userCanClick(`${formSelector} button[type="submit"]`, 'user should be able to submit form');
      }
    });
  }

  /**
   * Assert element is accessible
   * Checks: visible, enabled, has accessible name, proper ARIA
   */
  async elementIsAccessible(selector: string): Promise<void> {
    const element = this.page.locator(selector);

    await test.step(`element ${selector} should be accessible`, async () => {
      // Element is visible
      await expect(element, `${selector} should be visible`).toBeVisible();
      
      // Element is enabled (if it's interactive)
      const role = await element.getAttribute('role');
      const tagName = await element.evaluate(el => el.tagName.toLowerCase());
      const isInteractive = ['button', 'a', 'input', 'select', 'textarea'].includes(tagName) 
        || role === 'button' 
        || role === 'link';
      
      if (isInteractive) {
        await expect(element, `${selector} should be enabled`).toBeEnabled();
      }
      
      // Element has accessible name
      const accessibleName = await this.getAccessibleName(element);
      expect(accessibleName, `${selector} should have an accessible name`).toBeTruthy();
    });
  }

  /**
   * Assert navigation is functional
   */
  async navigationWorks(linkText: string, expectedUrl?: string): Promise<void> {
    await test.step(`navigation to "${linkText}" works`, async () => {
      const link = this.page.getByRole('link', { name: linkText });
      
      // Link is visible and clickable
      await this.userCanClick(`a:has-text("${linkText}")`, `link "${linkText}" should be clickable`);
      
      // Click link
      await link.click();
      
      // Check URL if specified
      if (expectedUrl) {
        await expect(this.page, `should navigate to ${expectedUrl}`).toHaveURL(expectedUrl);
      }
      
      // Wait for page load
      await this.page.waitForLoadState('networkidle');
    });
  }

  /**
   * Assert modal/dialog is properly displayed
   */
  async modalIsVisible(modalSelector: string): Promise<void> {
    const modal = this.page.locator(modalSelector);

    await test.step(`modal ${modalSelector} should be visible`, async () => {
      // Modal is visible
      await expect(modal, 'modal should be visible').toBeVisible();
      
      // Modal has proper ARIA attributes
      await expect(modal, 'modal should have role="dialog"').toHaveAttribute('role', 'dialog');
      await expect(modal, 'modal should have aria-modal="true"').toHaveAttribute('aria-modal', 'true');
      
      // Modal has accessible name (aria-labelledby or aria-label)
      const labelledBy = await modal.getAttribute('aria-labelledby');
      const ariaLabel = await modal.getAttribute('aria-label');
      expect(labelledBy || ariaLabel, 'modal should have accessible name').toBeTruthy();
      
      // Close button is accessible
      const closeButton = modal.locator('button[aria-label*="close"], button[aria-label*="关闭"], .close-button');
      if (await closeButton.count() > 0) {
        await this.userCanClick(`${modalSelector} button[aria-label*="close"]`, 'user should be able to close modal');
      }
    });
  }

  /**
   * Assert error message is properly displayed and accessible
   */
  async errorMessageIsAccessible(errorSelector: string): Promise<void> {
    const error = this.page.locator(errorSelector);

    await test.step(`error message ${errorSelector} should be accessible`, async () => {
      // Error is visible
      await expect(error, 'error message should be visible').toBeVisible();
      
      // Error has appropriate role (alert or status)
      const role = await error.getAttribute('role');
      expect(['alert', 'status'].includes(role || ''), 'error should have role="alert" or role="status"').toBe(true);
      
      // Error has aria-live for screen readers
      const ariaLive = await error.getAttribute('aria-live');
      expect(ariaLive, 'error should have aria-live attribute').toBeTruthy();
    });
  }

  /**
   * Assert button is clickable and has proper accessibility
   */
  async buttonIsAccessible(buttonSelector: string, expectedName?: string): Promise<void> {
    const button = this.page.locator(buttonSelector);

    await test.step(`button ${buttonSelector} should be accessible`, async () => {
      // Button is visible and clickable
      await this.userCanClick(buttonSelector);
      
      // Button has accessible name
      const accessibleName = await this.getAccessibleName(button);
      expect(accessibleName, 'button should have accessible name').toBeTruthy();
      
      if (expectedName) {
        expect(accessibleName, `button accessible name should be "${expectedName}"`).toBe(expectedName);
      }
      
      // Button has type attribute (defaults to submit in forms)
      const type = await button.getAttribute('type');
      const isInForm = await button.evaluate(el => el.closest('form') !== null);
      
      if (isInForm && !type) {
        // Warn: button in form without type defaults to submit
        console.warn(`Button ${buttonSelector} in form should have type="button" or type="submit"`);
      }
    });
  }

  /**
   * Get accessible name of an element
   */
  private async getAccessibleName(element: Locator): Promise<string | null> {
    return element.evaluate((el) => {
      // aria-label
      const ariaLabel = el.getAttribute('aria-label');
      if (ariaLabel) return ariaLabel;
      
      // aria-labelledby
      const labelledBy = el.getAttribute('aria-labelledby');
      if (labelledBy) {
        const labelEl = document.getElementById(labelledBy);
        if (labelEl) return labelEl.textContent?.trim() || null;
      }
      
      // label[for] for input
      if (el.id) {
        const label = document.querySelector(`label[for="${el.id}"]`);
        if (label) return label.textContent?.trim() || null;
      }
      
      // label wrapping input
      const parentLabel = el.closest('label');
      if (parentLabel) {
        // Get text content excluding the input itself
        return parentLabel.textContent?.replace(el.textContent || '', '').trim() || null;
      }
      
      // Button text content
      if (el.tagName === 'BUTTON') {
        return el.textContent?.trim() || null;
      }
      
      // Link text content
      if (el.tagName === 'A') {
        return el.textContent?.trim() || el.getAttribute('title') || null;
      }
      
      // Alt text for images
      if (el.tagName === 'IMG') {
        return el.getAttribute('alt');
      }
      
      // Title attribute fallback
      return el.getAttribute('title');
    });
  }
}

// Helper for test.step (imported from playwright)
import { test } from '@playwright/test';

/**
 * Create semantic assertions instance
 */
export function createSemanticAssertions(page: Page): SemanticAssertions {
  return new SemanticAssertions(page);
}

/**
 * Quick helpers for common assertions
 */
export async function userCanClick(page: Page, selector: string): Promise<void> {
  const assertions = createSemanticAssertions(page);
  await assertions.userCanClick(selector);
}

export async function formIsVisible(page: Page, formSelector: string): Promise<void> {
  const assertions = createSemanticAssertions(page);
  await assertions.formIsVisible(formSelector);
}

export async function navigationWorks(page: Page, linkText: string, expectedUrl?: string): Promise<void> {
  const assertions = createSemanticAssertions(page);
  await assertions.navigationWorks(linkText, expectedUrl);
}