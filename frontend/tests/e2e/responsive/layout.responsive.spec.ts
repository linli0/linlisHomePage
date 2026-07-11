/**
 * Responsive Layout Tests - Multi-Viewport Validation
 * 
 * Tests layout across 320px, 375px, 768px, 1280px, 1920px
 * Checks:
 * - No horizontal overflow
 * - Elements in viewport
 * - Proper element stacking
 * - Mobile navigation
 */

import { test, expect } from '@playwright/test';
import { 
  VIEWPORTS, 
  checkNoHorizontalOverflow, 
  isElementInViewport, 
  isElementNotOverlapped,
  testAcrossViewports 
} from '../helpers/visual';
import { createSemanticAssertions } from '../helpers/assertions';

// Viewport definitions
const VIEWPORT_MATRIX = [
  { name: '320px (Mobile Small)', width: 320, height: 568 },
  { name: '375px (Mobile)', width: 375, height: 667 },
  { name: '768px (Tablet)', width: 768, height: 1024 },
  { name: '1024px (Tablet Landscape)', width: 1024, height: 768 },
  { name: '1280px (Desktop)', width: 1280, height: 720 },
  { name: '1920px (Large Desktop)', width: 1920, height: 1080 },
];

test.describe('Responsive Layout Tests', () => {
  // ============================================
  // Navigation Responsive Tests
  // ============================================
  for (const viewport of VIEWPORT_MATRIX) {
    test.describe(`${viewport.name}`, () => {
      test.use({ viewport: { width: viewport.width, height: viewport.height } });

      test('Navigation is visible and functional', async ({ page }) => {
        await page.goto('/');
        await page.waitForLoadState('networkidle');
        
        const nav = page.locator('nav');
        await expect(nav).toBeVisible();
        
        // Check nav position
        const navBox = await nav.boundingBox();
        expect(navBox, 'Nav should have bounding box').toBeTruthy();
        
        // Nav should be at top
        expect(navBox?.y, 'Nav should be at top of page').toBeLessThan(100);
        
        // Check nav width doesn't overflow
        expect(navBox?.width, 'Nav width should not exceed viewport').toBeLessThanOrEqual(viewport.width);
        
        // Desktop: all nav links visible
        if (viewport.width >= 768) {
          const navLinks = nav.locator('a').filter({ hasNot: page.locator('.hidden') });
          const count = await navLinks.count();
          expect(count, 'Desktop nav should have visible links').toBeGreaterThan(0);
        }
        
        // Mobile: hamburger menu visible
        if (viewport.width < 768) {
          const mobileMenuButton = nav.locator('button').filter({ has: page.locator('svg') });
          if (await mobileMenuButton.count() > 0) {
            await expect(mobileMenuButton.first()).toBeVisible();
          }
        }
      });

      test('No horizontal overflow', async ({ page }) => {
        await page.goto('/');
        await page.waitForLoadState('networkidle');
        
        const noOverflow = await checkNoHorizontalOverflow(page, viewport.width);
        expect(noOverflow, `Page should not have horizontal overflow at ${viewport.width}px`).toBe(true);
        
        // Check body scroll width
        const bodyScrollWidth = await page.evaluate(() => document.body.scrollWidth);
        expect(bodyScrollWidth, 'Body scroll width should not exceed viewport').toBeLessThanOrEqual(viewport.width + 10); // Small tolerance
      });

      test('Main content is visible in viewport', async ({ page }) => {
        await page.goto('/');
        await page.waitForLoadState('networkidle');
        
        // Hero section should be visible
        const hero = page.locator('.hero-gradient-animated, section:first-of-type');
        if (await hero.count() > 0) {
          await expect(hero.first()).toBeVisible();
        }
        
        // Main content should be in viewport
        const mainContent = page.locator('main, .max-w-7xl');
        if (await mainContent.count() > 0) {
          const inViewport = await isElementInViewport(mainContent.first());
          expect(inViewport, 'Main content should be in viewport').toBe(true);
        }
      });

      test('Buttons are not overlapped', async ({ page }) => {
        await page.goto('/');
        await page.waitForLoadState('networkidle');
        
        // Check CTA buttons
        const buttons = page.locator('button, .btn');
        const count = await buttons.count();
        
        for (let i = 0; i < Math.min(count, 5); i++) {
          const button = buttons.nth(i);
          if (await button.isVisible()) {
            const notOverlapped = await isElementNotOverlapped(button);
            expect(notOverlapped, `Button ${i} should not be overlapped`).toBe(true);
          }
        }
      });
    });
  }

  // ============================================
  // View-Specific Responsive Tests
  // ============================================
  test.describe('Gold Price View Responsive', () => {
    for (const viewport of VIEWPORT_MATRIX) {
      test(`${viewport.name} - layout correct`, async ({ page }) => {
        await page.setViewportSize({ width: viewport.width, height: viewport.height });
        await page.goto('/gold');
        await page.waitForLoadState('networkidle');
        
        // Price display visible
        const priceDisplay = page.locator('.text-4xl, .text-5xl');
        if (await priceDisplay.count() > 0) {
          await expect(priceDisplay.first()).toBeVisible();
        }
        
        // Chart visible
        const chart = page.locator('canvas, [data-cy="chart"]');
        if (await chart.count() > 0) {
          await expect(chart).toBeVisible();
          
          // Chart width should fit viewport
          const chartBox = await chart.boundingBox();
          expect(chartBox?.width, 'Chart width should fit viewport').toBeLessThanOrEqual(viewport.width);
        }
        
        // Currency buttons
        const currencyButtons = page.locator('button').filter({ hasText: /USD|CNY|EUR|GBP/ });
        const buttonCount = await currencyButtons.count();
        
        if (viewport.width < 768) {
          // Mobile: buttons should be compact or stacked
          for (let i = 0; i < buttonCount; i++) {
            await expect(currencyButtons.nth(i)).toBeVisible();
          }
        }
      });
    }
  });

  test.describe('Articles View Responsive', () => {
    for (const viewport of VIEWPORT_MATRIX) {
      test(`${viewport.name} - article cards layout`, async ({ page }) => {
        await page.setViewportSize({ width: viewport.width, height: viewport.height });
        await page.goto('/articles');
        await page.waitForLoadState('networkidle');
        
        // Article cards visible
        const articleCards = page.locator('.card-hover');
        const count = await articleCards.count();
        
        if (count > 0) {
          // Check card grid layout
          const firstCard = articleCards.first();
          await expect(firstCard).toBeVisible();
          
          // Card width should be reasonable
          const cardBox = await firstCard.boundingBox();
          expect(cardBox?.width, 'Card width should be reasonable').toBeGreaterThan(200);
          expect(cardBox?.width, 'Card should not overflow').toBeLessThanOrEqual(viewport.width);
          
          // Check card content not truncated
          const cardContent = firstCard.locator('h2, h3');
          if (await cardContent.count() > 0) {
            const contentVisible = await isElementInViewport(cardContent.first());
            expect(contentVisible, 'Card content should be visible').toBe(true);
          }
        }
        
        // No overflow
        const noOverflow = await checkNoHorizontalOverflow(page, viewport.width);
        expect(noOverflow, 'Articles page should not overflow').toBe(true);
      });
    }
  });

  test.describe('Tools View Responsive', () => {
    for (const viewport of VIEWPORT_MATRIX) {
      test(`${viewport.name} - tool cards layout`, async ({ page }) => {
        await page.setViewportSize({ width: viewport.width, height: viewport.height });
        await page.goto('/tools');
        await page.waitForLoadState('networkidle');
        
        // Tool cards
        const toolCards = page.locator('.card-hover');
        const count = await toolCards.count();
        
        // Desktop: 2-column grid
        if (viewport.width >= 768) {
          expect(count, 'Desktop should show multiple tool cards').toBeGreaterThanOrEqual(2);
        }
        
        // Mobile: single column
        if (viewport.width < 768) {
          // Cards should stack vertically
          if (count >= 2) {
            const card1Box = await toolCards.nth(0).boundingBox();
            const card2Box = await toolCards.nth(1).boundingBox();
            
            // Vertical stacking: second card below first
            expect(card2Box?.y, 'Cards should stack vertically on mobile').toBeGreaterThan(card1Box?.y || 0);
          }
        }
        
        // All cards visible and accessible
        for (let i = 0; i < Math.min(count, 3); i++) {
          const card = toolCards.nth(i);
          await expect(card).toBeVisible();
          
          // Card inputs should be visible
          const inputs = card.locator('input, textarea');
          const inputCount = await inputs.count();
          for (let j = 0; j < inputCount; j++) {
            await expect(inputs.nth(j)).toBeVisible();
          }
        }
      });
    }
  });

  // ============================================
  // Mobile Menu Tests
  // ============================================
  test.describe('Mobile Menu', () => {
    test.use({ viewport: { width: 375, height: 667 } });

    test('Mobile menu opens and closes', async ({ page }) => {
      await page.goto('/');
      await page.waitForLoadState('networkidle');
      
      // Find hamburger menu button
      const menuButton = page.locator('nav button').filter({ has: page.locator('svg') });
      
      if (await menuButton.count() > 0) {
        // Click to open
        await menuButton.first().click();
        
        // Menu should be visible
        const mobileMenu = page.locator('.md:hidden').filter({ has: page.locator('a') });
        await expect(mobileMenu).toBeVisible();
        
        // Nav links in mobile menu
        const menuLinks = mobileMenu.locator('a');
        const count = await menuLinks.count();
        expect(count, 'Mobile menu should have nav links').toBeGreaterThan(0);
        
        // Click to close
        await menuButton.first().click();
        
        // Menu should close
        await expect(mobileMenu).toBeHidden();
      }
    });

    test('Mobile menu links are functional', async ({ page }) => {
      await page.goto('/');
      await page.waitForLoadState('networkidle');
      
      const menuButton = page.locator('nav button').filter({ has: page.locator('svg') });
      
      if (await menuButton.count() > 0) {
        await menuButton.first().click();
        
        // Click a nav link
        const goldLink = page.locator('a[href="/gold"]');
        await goldLink.click();
        
        // Should navigate to gold page
        await expect(page).toHaveURL('/gold');
      }
    });
  });

  // ============================================
  // Touch Target Tests
  // ============================================
  test.describe('Touch Targets (Mobile)', () => {
    test.use({ viewport: { width: 375, height: 667 } });

    test('Touch targets are ≥44x44px', async ({ page }) => {
      await page.goto('/');
      await page.waitForLoadState('networkidle');
      
      const buttons = page.locator('button, a.btn, .btn');
      const count = await buttons.count();
      
      for (let i = 0; i < Math.min(count, 10); i++) {
        const button = buttons.nth(i);
        if (await button.isVisible()) {
          const box = await button.boundingBox();
          
          // WCAG 2.5.5: Touch targets should be at least 44x44px
          expect(box?.width, `Button ${i} width should be ≥44px`).toBeGreaterThanOrEqual(44);
          expect(box?.height, `Button ${i} height should be ≥44px`).toBeGreaterThanOrEqual(44);
        }
      }
    });
  });

  // ============================================
  // Text Size Tests
  // ============================================
  test.describe('Text Size Responsive', () => {
    test('Text is readable at 320px', async ({ page }) => {
      await page.setViewportSize({ width: 320, height: 568 });
      await page.goto('/');
      await page.waitForLoadState('networkidle');
      
      // Check text doesn't overflow
      const headings = page.locator('h1, h2, h3');
      const count = await headings.count();
      
      for (let i = 0; i < count; i++) {
        const heading = headings.nth(i);
        const box = await heading.boundingBox();
        
        // Text width should fit viewport
        expect(box?.width, `Heading ${i} should not overflow`).toBeLessThanOrEqual(320);
      }
      
      // Check no text truncation issues
      const truncatedText = page.locator('.line-clamp-2, .line-clamp-3');
      const truncatedCount = await truncatedText.count();
      
      // Truncated text should still show meaningful content
      for (let i = 0; i < truncatedCount; i++) {
        const element = truncatedText.nth(i);
        const text = await element.textContent();
        expect(text?.length, `Truncated text ${i} should have content`).toBeGreaterThan(10);
      }
    });
  });
});