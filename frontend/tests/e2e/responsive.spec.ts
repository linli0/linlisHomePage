/**
 * Responsive Design Tests
 * 
 * Multi-viewport testing across 320px - 1920px
 * - No horizontal overflow
 * - Elements visible and not overlapped
 * - Navigation works at all sizes
 * - Mobile menu functionality
 */

import { test, expect } from '@playwright/test';
import { VIEWPORTS, checkNoHorizontalOverflow, isElementInViewport, isElementNotOverlapped, testAcrossViewports } from '../helpers/visual';
import { createSemanticAssertions } from '../helpers/assertions';

// Define viewports for testing
const viewportSizes = [
  { name: 'mobile-320', width: 320, height: 568 },
  { name: 'mobile-375', width: 375, height: 667 },
  { name: 'tablet-768', width: 768, height: 1024 },
  { name: 'desktop-1024', width: 1024, height: 768 },
  { name: 'desktop-1280', width: 1280, height: 720 },
  { name: 'desktop-1920', width: 1920, height: 1080 },
];

// Test each viewport
for (const viewport of viewportSizes) {
  test.describe(`Responsive Layout - ${viewport.name} (${viewport.width}x${viewport.height})`, () => {
    
    test.use({ viewport: { width: viewport.width, height: viewport.height } });

    test.beforeEach(async ({ page }) => {
      // Login first
      await page.goto('/login');
      await page.locator('#password').fill('admin123');
      await page.locator('button[type="submit"]').click();
      await page.waitForURL('/');
    });

    test('No horizontal overflow', async ({ page }) => {
      await page.waitForLoadState('networkidle');
      
      // Check body doesn't overflow viewport
      const noOverflow = await checkNoHorizontalOverflow(page, viewport.width);
      expect(noOverflow, `Page should not have horizontal overflow at ${viewport.width}px`).toBe(true);
      
      // Also check document width
      const documentWidth = await page.evaluate(() => document.documentElement.scrollWidth);
      expect(documentWidth, `Document width should not exceed ${viewport.width}px`).toBeLessThanOrEqual(viewport.width + 10); // Small tolerance for rounding
    });

    test('Navigation is visible and functional', async ({ page }) => {
      await page.waitForLoadState('networkidle');
      
      const nav = page.locator('nav');
      await expect(nav, 'Navigation should be visible').toBeVisible();
      
      // Check nav position
      const navBox = await nav.boundingBox();
      expect(navBox, 'Navigation bounding box should exist').toBeTruthy();
      
      // Nav should be at top
      expect(navBox?.y, 'Navigation should be at top of page').toBeLessThan(50);
      
      // At mobile sizes, check mobile menu toggle
      if (viewport.width < 768) {
        // Mobile menu toggle should be visible
        const mobileToggle = page.locator('button.md:hidden');
        await expect(mobileToggle, 'Mobile menu toggle should be visible').toBeVisible();
        
        // Click to open mobile menu
        await mobileToggle.click();
        
        // Mobile menu should appear
        const mobileMenu = page.locator('.md:hidden').filter({ has: page.locator('router-link') });
        await expect(mobileMenu, 'Mobile menu should be visible after toggle').toBeVisible();
        
        // Close menu
        await mobileToggle.click();
      } else {
        // Desktop: nav links should be directly visible
        const navLinks = page.locator('nav').locator('router-link, a').filter({ hasNotText: '' });
        await expect(navLinks.first(), 'Desktop nav link should be visible').toBeVisible();
      }
    });

    test('Main content is visible and in viewport', async ({ page }) => {
      await page.waitForLoadState('networkidle');
      
      // Main content area
      const main = page.locator('main, .min-h-screen, .max-w-7xl').first();
      await expect(main, 'Main content should be visible').toBeVisible();
      
      // Check main content is in viewport
      const inViewport = await isElementInViewport(main);
      expect(inViewport, 'Main content should be in viewport').toBe(true);
    });

    test('Key buttons are visible and not overlapped', async ({ page }) => {
      await page.waitForLoadState('networkidle');
      
      // Find all buttons
      const buttons = page.locator('button:visible');
      const count = await buttons.count();
      
      // Check first few buttons
      for (let i = 0; i < Math.min(count, 5); i++) {
        const button = buttons.nth(i);
        
        // Button is visible
        await expect(button).toBeVisible();
        
        // Button is in viewport
        const inViewport = await isElementInViewport(button, 0.5);
        if (inViewport) {
          // Button is not overlapped
          const notOverlapped = await isElementNotOverlapped(button);
          expect(notOverlapped, `Button ${i} should not be overlapped`).toBe(true);
        }
      }
    });

    test('Images and media fit viewport', async ({ page }) => {
      await page.waitForLoadState('networkidle');
      
      // Find all images
      const images = page.locator('img');
      const count = await images.count();
      
      for (let i = 0; i < count; i++) {
        const img = images.nth(i);
        if (await img.isVisible()) {
          const imgBox = await img.boundingBox();
          if (imgBox) {
            // Image should not exceed viewport width
            expect(imgBox.width, `Image ${i} should not exceed viewport width`).toBeLessThanOrEqual(viewport.width + 10);
          }
        }
      }
    });

    test('Cards and grid layouts respond correctly', async ({ page }) => {
      await page.waitForLoadState('networkidle');
      
      // Find card grids
      const grids = page.locator('.grid');
      const count = await grids.count();
      
      for (let i = 0; i < count; i++) {
        const grid = grids.nth(i);
        if (await grid.isVisible()) {
          // Check grid columns at this viewport
          const gridBox = await grid.boundingBox();
          if (gridBox) {
            // Grid should fit viewport
            expect(gridBox.width, `Grid ${i} should fit viewport`).toBeLessThanOrEqual(viewport.width + 10);
            
            // Check cards in grid
            const cards = grid.locator('.card, .card-hover');
            const cardCount = await cards.count();
            
            for (let j = 0; j < cardCount; j++) {
              const card = cards.nth(j);
              if (await card.isVisible()) {
                const cardBox = await card.boundingBox();
                if (cardBox) {
                  // Card should not exceed viewport
                  expect(cardBox.width, `Card ${j} in grid ${i} should fit viewport`).toBeLessThanOrEqual(viewport.width);
                }
              }
            }
          }
        }
      }
    });
  });
}

// ========== Specific Page Responsive Tests ==========
test.describe('Home Page - Responsive', () => {
  
  for (const viewport of viewportSizes) {
    test(`Hero section at ${viewport.name}`, async ({ page }) => {
      test.use({ viewport: { width: viewport.width, height: viewport.height } });
      
      await page.goto('/login');
      await page.locator('#password').fill('admin123');
      await page.locator('button[type="submit"]').click();
      await page.waitForURL('/');
      
      // Hero section is visible
      const hero = page.locator('.hero-gradient-animated, .gradient-text').first();
      await expect(hero).toBeVisible();
      
      // Hero text fits viewport
      const heroTitle = page.locator('h1').first();
      const titleBox = await heroTitle.boundingBox();
      expect(titleBox?.width, 'Hero title should fit viewport').toBeLessThanOrEqual(viewport.width);
      
      // CTA buttons are visible
      const ctaButtons = page.locator('button, router-link').filter({ hasText: /金价|AI|Wiki/ });
      const count = await ctaButtons.count();
      expect(count, 'CTA buttons should be present').toBeGreaterThan(0);
    });
  }
});

test.describe('Gold Price Page - Responsive', () => {
  
  for (const viewport of viewportSizes) {
    test(`Currency and period selectors at ${viewport.name}`, async ({ page }) => {
      test.use({ viewport: { width: viewport.width, height: viewport.height } });
      
      await page.goto('/login');
      await page.locator('#password').fill('admin123');
      await page.locator('button[type="submit"]').click();
      await page.goto('/gold');
      
      // Currency selector buttons
      const currencyBtns = page.locator('button').filter({ hasText: /USD|CNY|EUR|GBP/ });
      const count = await currencyBtns.count();
      
      for (let i = 0; i < count; i++) {
        const btn = currencyBtns.nth(i);
        await expect(btn).toBeVisible();
        
        // Buttons should not be stacked weirdly at mobile
        if (viewport.width < 400) {
          const btnBox = await btn.boundingBox();
          expect(btnBox?.width, 'Currency button should not overflow').toBeLessThanOrEqual(viewport.width / 2);
        }
      }
      
      // Period selector buttons
      const periodBtns = page.locator('button').filter({ hasText: /天/ });
      await expect(periodBtns.first()).toBeVisible();
    });

    test(`Price chart at ${viewport.name}`, async ({ page }) => {
      test.use({ viewport: { width: viewport.width, height: viewport.height } });
      
      await page.goto('/login');
      await page.locator('#password').fill('admin123');
      await page.locator('button[type="submit"]').click();
      await page.goto('/gold');
      await page.waitForLoadState('networkidle');
      
      // Chart container
      const chart = page.locator('.chart-container, canvas, .card:has(canvas)').first();
      
      if (await chart.count() > 0) {
        const chartBox = await chart.boundingBox();
        expect(chartBox?.width, 'Chart should fit viewport').toBeLessThanOrEqual(viewport.width);
      }
    });
  }
});

test.describe('Tools Page - Responsive', () => {
  
  for (const viewport of viewportSizes) {
    test(`Tool cards at ${viewport.name}`, async ({ page }) => {
      test.use({ viewport: { width: viewport.width, height: viewport.height } });
      
      await page.goto('/login');
      await page.locator('#password').fill('admin123');
      await page.locator('button[type="submit"]').click();
      await page.goto('/tools');
      
      // Tool cards
      const toolCards = page.locator('.card-hover');
      const count = await toolCards.count();
      
      for (let i = 0; i < count; i++) {
        const card = toolCards.nth(i);
        const cardBox = await card.boundingBox();
        
        // Card should fit viewport
        expect(cardBox?.width, `Tool card ${i} should fit viewport`).toBeLessThanOrEqual(viewport.width);
        
        // Card inputs should be usable
        const inputs = card.locator('input, textarea');
        const inputCount = await inputs.count();
        
        for (let j = 0; j < inputCount; j++) {
          const input = inputs.nth(j);
          const inputBox = await input.boundingBox();
          expect(inputBox?.width, `Input ${j} in card ${i} should be usable`).toBeGreaterThan(50);
        }
      }
    });
  }
});

// ========== Mobile-Specific Tests ==========
test.describe('Mobile Specific (320px - 375px)', () => {
  
  test.use({ viewport: { width: 320, height: 568 } });

  test('Mobile menu opens and closes', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    
    // Mobile toggle exists
    const toggle = page.locator('button.md:hidden');
    await expect(toggle).toBeVisible();
    
    // Open menu
    await toggle.click();
    
    // Menu items visible
    const menuItems = page.locator('.md:hidden a, .md:hidden router-link');
    await expect(menuItems.first()).toBeVisible();
    
    // Close menu
    await toggle.click();
    
    // Menu hidden
    await expect(menuItems.first()).not.toBeVisible();
  });

  test('Touch-friendly button sizes', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    
    // Buttons should be at least 44x44 for touch (WCAG 2.1)
    const buttons = page.locator('button:visible');
    const count = await buttons.count();
    
    for (let i = 0; i < Math.min(count, 10); i++) {
      const btn = buttons.nth(i);
      const box = await btn.boundingBox();
      
      if (box) {
        // WCAG 2.1 target size recommendation: 44x44 CSS pixels
        expect(box.height, `Button ${i} height should be touch-friendly (≥32px)`).toBeGreaterThanOrEqual(32);
        expect(box.width, `Button ${i} width should be touch-friendly (≥32px)`).toBeGreaterThanOrEqual(32);
      }
    }
  });

  test('No text truncation issues', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    
    // Check text elements don't overflow
    const textElements = page.locator('h1, h2, p, span').filter({ hasNot: page.locator('.sr-only') });
    const count = await textElements.count();
    
    for (let i = 0; i < Math.min(count, 20); i++) {
      const el = textElements.nth(i);
      if (await el.isVisible()) {
        const elBox = await el.boundingBox();
        if (elBox) {
          expect(elBox.width, `Text element ${i} should fit viewport`).toBeLessThanOrEqual(320);
        }
      }
    }
  });
});