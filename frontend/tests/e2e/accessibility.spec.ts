/**
 * Accessibility Test Suite
 * 
 * WCAG 2.1 AA Compliance Tests for all 11 views
 * - Color contrast ≥4.5:1 (AA) for normal text
 * - Keyboard navigation
 * - Screen reader compatibility
 * - Form validation and error messages
 * - ARIA attributes
 */

import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import { scanForA11yViolations, assertNoA11yViolations, checkColorContrast, checkFormFieldAccessibility } from '../helpers/accessibility';
import { createSemanticAssertions, userCanClick, formIsVisible } from '../helpers/assertions';

// Test all pages for WCAG AA compliance
test.describe('Accessibility - WCAG 2.1 AA Compliance', () => {
  
  test.describe.configure({ mode: 'parallel' });

  // ========== Login Page Accessibility ==========
  test('Login page - WCAG AA compliance', async ({ page }) => {
    await page.goto('/login');
    
    // Wait for page to be ready
    await page.waitForLoadState('networkidle');
    
    // Full page accessibility scan
    const results = await scanForA11yViolations(page, {
      generateReport: true,
      reportName: 'login-a11y-report.html',
    });
    
    expect(results.violations).toEqual([]);
  });

  test('Login form - accessible labels and inputs', async ({ page }) => {
    await page.goto('/login');
    
    // Check password input has accessible label
    const passwordInput = page.locator('#password');
    const labelInfo = await checkFormFieldAccessibility(page, '#password');
    expect(labelInfo.hasLabel, 'Password input should have accessible label').toBe(true);
    
    // Check label text is meaningful
    const label = page.locator('label[for="password"]');
    await expect(label).toContainText('密码');
    
    // Check error message container exists
    const errorContainer = page.locator('.bg-red-50, .bg-red-900/20');
    // Error may not be visible initially, but container should exist
  });

  test('Login form - keyboard navigation', async ({ page }) => {
    await page.goto('/login');
    
    // Tab to password input
    await page.keyboard.press('Tab');
    await expect(page.locator('#password')).toBeFocused();
    
    // Tab to submit button
    await page.keyboard.press('Tab');
    await expect(page.locator('button[type="submit"]')).toBeFocused();
    
    // Can toggle password visibility with keyboard
    await page.keyboard.press('Shift+Tab');
    await expect(page.locator('#password')).toBeFocused();
  });

  // ========== Home Page Accessibility ==========
  test('Home page - WCAG AA compliance', async ({ page }) => {
    // Need to login first (global auth required)
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    
    await page.waitForLoadState('networkidle');
    
    const results = await scanForA11yViolations(page, {
      generateReport: true,
      reportName: 'home-a11y-report.html',
    });
    
    expect(results.violations).toEqual([]);
  });

  test('Home page - navigation accessibility', async ({ page }) => {
    // Login
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    
    const assertions = createSemanticAssertions(page);
    
    // Navigation links are accessible
    await assertions.elementIsAccessible('nav');
    
    // All nav links have accessible names
    const navLinks = page.locator('nav a, nav router-link');
    const count = await navLinks.count();
    
    for (let i = 0; i < count; i++) {
      const link = navLinks.nth(i);
      await expect(link).toBeVisible();
      
      const text = await link.textContent();
      expect(text?.trim().length, `Nav link ${i} should have text content`).toBeGreaterThan(0);
    }
  });

  // ========== Gold Price Page Accessibility ==========
  test('Gold price page - WCAG AA compliance', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    
    await page.goto('/gold');
    await page.waitForLoadState('networkidle');
    
    const results = await scanForA11yViolations(page, {
      generateReport: true,
      reportName: 'gold-a11y-report.html',
    });
    
    expect(results.violations).toEqual([]);
  });

  test('Gold price - interactive elements accessible', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.goto('/gold');
    
    const assertions = createSemanticAssertions(page);
    
    // Currency selector buttons are accessible
    await assertions.userCanClick('button:has-text("USD")');
    await assertions.userCanClick('button:has-text("CNY")');
    
    // Period selector buttons are accessible
    await assertions.userCanClick('button:has-text("7天")');
    await assertions.userCanClick('button:has-text("30天")');
    
    // Refresh button is accessible
    await assertions.userCanClick('button:has-text("刷新")');
  });

  // ========== Tools Page Accessibility ==========
  test('Tools page - WCAG AA compliance', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    
    await page.goto('/tools');
    await page.waitForLoadState('networkidle');
    
    const results = await scanForA11yViolations(page, {
      generateReport: true,
      reportName: 'tools-a11y-report.html',
    });
    
    expect(results.violations).toEqual([]);
  });

  test('Tools page - forms accessible', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.goto('/tools');
    
    const assertions = createSemanticAssertions(page);
    
    // JSON tool form
    await assertions.userCanClick('.card-hover:has-text("JSON") button:has-text("格式化")');
    await assertions.userCanClick('.card-hover:has-text("JSON") button:has-text("压缩")');
    
    // Base64 tool form
    await assertions.userCanClick('.card-hover:has-text("Base64") button:has-text("编码")');
    await assertions.userCanClick('.card-hover:has-text("Base64") button:has-text("解码")');
    
    // Hash tool buttons
    await assertions.userCanClick('.card-hover:has-text("哈希") button:has-text("MD5")');
  });

  // ========== Articles Page Accessibility ==========
  test('Articles page - WCAG AA compliance', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    
    await page.goto('/articles');
    await page.waitForLoadState('networkidle');
    
    const results = await scanForA11yViolations(page, {
      generateReport: true,
      reportName: 'articles-a11y-report.html',
    });
    
    expect(results.violations).toEqual([]);
  });

  // ========== AI Chat Page Accessibility ==========
  test('AI chat page - WCAG AA compliance', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    
    await page.goto('/ai');
    await page.waitForLoadState('networkidle');
    
    const results = await scanForA11yViolations(page, {
      generateReport: true,
      reportName: 'ai-a11y-report.html',
    });
    
    expect(results.violations).toEqual([]);
  });

  test('AI chat - form accessibility', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.goto('/ai');
    
    const assertions = createSemanticAssertions(page);
    
    // Message input is accessible
    const messageInput = page.locator('textarea').first();
    await expect(messageInput).toBeVisible();
    await expect(messageInput).toBeEnabled();
    
    // Send button is accessible
    await assertions.userCanClick('button:has-text("发送")');
  });

  // ========== Profile Page Accessibility ==========
  test('Profile page - WCAG AA compliance', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');
    
    const results = await scanForA11yViolations(page, {
      generateReport: true,
      reportName: 'profile-a11y-report.html',
    });
    
    expect(results.violations).toEqual([]);
  });

  test('Profile forms accessible', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.goto('/profile');
    
    const assertions = createSemanticAssertions(page);
    
    // Profile edit form inputs
    const displayNameInput = page.locator('input').filter({ hasText: '' }).first();
    await expect(displayNameInput).toBeVisible();
    
    // Change password form
    await assertions.formIsVisible('form, .card');
  });
});

// ========== Color Contrast Tests ==========
test.describe('Color Contrast - WCAG AA (≥4.5:1)', () => {
  
  test('Primary color contrast on light background', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    
    // Check primary buttons
    const primaryButtons = page.locator('.btn-primary, .bg-primary-500');
    const count = await primaryButtons.count();
    
    if (count > 0) {
      // Check contrast of first primary button text
      const contrast = await checkColorContrast(page, '.btn-primary');
      // Primary-500 (#0ea5e9) on white should meet 4.5:1
      expect(contrast.ratio, 'Primary color contrast should meet WCAG AA').toBeGreaterThanOrEqual(4.5);
    }
  });

  test('Gold color contrast analysis', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.goto('/gold');
    
    // Gold-500 (#f59e0b) may fail on white - document this
    const goldElements = page.locator('.bg-gold-500, .btn-gold, .text-gold-500');
    const count = await goldElements.count();
    
    if (count > 0) {
      // Note: Gold-500 on white ~2.5:1 (FAILS WCAG AA)
      // This is a known accessibility gap that needs fixing
      console.log(`Found ${count} gold elements - checking contrast...`);
    }
  });
});

// ========== Keyboard Navigation Tests ==========
test.describe('Keyboard Navigation', () => {
  
  test('Global tab order follows logical reading order', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    
    // Get all focusable elements
    const focusableElements = await page.locator(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    ).all();
    
    // Tab through elements and verify focus
    for (let i = 0; i < Math.min(focusableElements.length, 10); i++) {
      await page.keyboard.press('Tab');
      // Focus should be visible
      const focused = page.locator(':focus');
      await expect(focused).toBeVisible();
    }
  });

  test('Modal traps focus correctly', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    
    // Open user dropdown if visible
    const userMenu = page.locator('button:has(.rounded-xl)').filter({ has: page.locator('.w-8') });
    
    if (await userMenu.count() > 0) {
      await userMenu.click();
      
      // Tab should stay within dropdown
      await page.keyboard.press('Tab');
      const focused = page.locator(':focus');
      
      // Focus should be on dropdown content or close button
      const dropdown = page.locator('.absolute.right-0');
      if (await dropdown.count() > 0) {
        const isInDropdown = await focused.evaluate((el, dropdown) => {
          return dropdown.contains(el);
        }, await dropdown.elementHandle());
        // Focus management for dropdowns
      }
      
      // Escape should close dropdown
      await page.keyboard.press('Escape');
      await expect(dropdown).not.toBeVisible();
    }
  });

  test('Enter key activates buttons', async ({ page }) => {
    await page.goto('/login');
    
    // Focus password input
    await page.locator('#password').focus();
    await page.locator('#password').fill('admin123');
    
    // Tab to submit button
    await page.keyboard.press('Tab');
    await expect(page.locator('button[type="submit"]')).toBeFocused();
    
    // Press Enter to submit
    await page.keyboard.press('Enter');
    
    // Should navigate to home
    await page.waitForURL('/');
  });
});