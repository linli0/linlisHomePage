/**
 * Visual Regression Testing Utilities
 * 
 * Features:
 * - Screenshot capture with options
 * - Pixel-by-pixel comparison using pixelmatch
 * - Element occlusion detection
 * - Multi-viewport layout validation
 */

import { Page, Locator, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';
import * as pixelmatch from 'pixelmatch';
import { PNG } from 'pngjs';

// Viewport definitions for responsive testing
export const VIEWPORTS = {
  mobile320: { name: 'mobile-320', width: 320, height: 568 },
  mobile375: { name: 'mobile-375', width: 375, height: 667 },
  tablet768: { name: 'tablet-768', width: 768, height: 1024 },
  desktop1280: { name: 'desktop-1280', width: 1280, height: 720 },
  desktop1920: { name: 'desktop-1920', width: 1920, height: 1080 },
};

// Screenshot options
export interface ScreenshotOptions {
  /** Full page screenshot */
  fullPage?: boolean;
  /** Disable animations */
  animations?: 'disabled' | 'allow';
  /** Mask dynamic elements */
  mask?: string[];
  /** Custom mask color */
  maskColor?: string;
  /** Hide caret */
  caret?: 'hide' | 'initial';
  /** Omit background */
  omitBackground?: boolean;
  /** Custom CSS styles */
  style?: string;
}

// Comparison options
export interface ComparisonOptions {
  /** Maximum allowed different pixels */
  maxDiffPixels?: number;
  /** Threshold for pixel comparison (0-1) */
  threshold?: number;
  /** Include AA (anti-aliasing) detection */
  includeAA?: boolean;
  /** Alpha blending threshold */
  alpha?: number;
}

/**
 * Capture page screenshot with standard options
 */
export async function captureScreenshot(
  page: Page,
  filename: string,
  options: ScreenshotOptions = {}
): Promise<Buffer> {
  const screenshotPath = path.join('screenshots', filename);
  
  // Ensure directory exists
  const dir = path.dirname(screenshotPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  // Apply mask if specified
  const maskLocators = options.mask?.map(sel => page.locator(sel)) || [];

  const screenshotOptions = {
    path: screenshotPath,
    fullPage: options.fullPage ?? false,
    animations: options.animations ?? 'disabled',
    mask: maskLocators,
    maskColor: options.maskColor ?? '#FF00FF50',
    caret: options.caret ?? 'hide',
    omitBackground: options.omitBackground ?? false,
    style: options.style,
  };

  return await page.screenshot(screenshotOptions);
}

/**
 * Capture element screenshot
 */
export async function captureElementScreenshot(
  locator: Locator,
  filename: string,
  options: ScreenshotOptions = {}
): Promise<Buffer> {
  const screenshotPath = path.join('screenshots', filename);
  
  const dir = path.dirname(screenshotPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  const screenshotOptions = {
    path: screenshotPath,
    animations: options.animations ?? 'disabled',
    caret: options.caret ?? 'hide',
    omitBackground: options.omitBackground ?? false,
    style: options.style,
  };

  return await locator.screenshot(screenshotOptions);
}

/**
 * Compare two screenshots pixel-by-pixel
 */
export async function compareScreenshots(
  screenshot1: Buffer,
  screenshot2: Buffer,
  options: ComparisonOptions = {}
): Promise<{ diffPixels: number; diffPercentage: number; passes: boolean }> {
  const img1 = PNG.sync.read(screenshot1);
  const img2 = PNG.sync.read(screenshot2);
  
  // Ensure same dimensions
  if (img1.width !== img2.width || img1.height !== img2.height) {
    throw new Error(`Screenshots have different dimensions: ${img1.width}x${img1.height} vs ${img2.width}x${img2.height}`);
  }

  const { width, height } = img1;
  const diff = new PNG({ width, height });
  
  const diffPixels = pixelmatch(
    img1.data,
    img2.data,
    diff.data,
    width,
    height,
    {
      threshold: options.threshold ?? 0.2,
      includeAA: options.includeAA ?? false,
      alpha: options.alpha ?? 0.5,
    }
  );

  const totalPixels = width * height;
  const diffPercentage = (diffPixels / totalPixels) * 100;
  const maxDiffPixels = options.maxDiffPixels ?? 100;
  const passes = diffPixels <= maxDiffPixels;

  // Save diff image if there are differences
  if (diffPixels > 0) {
    const diffPath = path.join('screenshots', 'diff.png');
    fs.writeFileSync(diffPath, PNG.sync.write(diff));
  }

  return { diffPixels, diffPercentage, passes };
}

/**
 * Check if element is not overlapped by other elements
 */
export async function isElementNotOverlapped(
  locator: Locator
): Promise<boolean> {
  return locator.evaluate((el) => {
    const rect = el.getBoundingClientRect();
    
    const isPointVisible = (x: number, y: number) => {
      const elementAtPoint = document.elementFromPoint(x, y);
      return el.contains(elementAtPoint) || elementAtPoint === el;
    };

    // Check multiple points on the element
    const pointsToCheck = [
      { x: rect.left + 2, y: rect.top + 2 },           // Top-left
      { x: rect.right - 2, y: rect.top + 2 },          // Top-right
      { x: rect.left + 2, y: rect.bottom - 2 },        // Bottom-left
      { x: rect.right - 2, y: rect.bottom - 2 },       // Bottom-right
      { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 },  // Center
    ];

    return pointsToCheck.every(point => isPointVisible(point.x, point.y));
  });
}

/**
 * Check element visibility (opacity, z-index, display, visibility)
 */
export async function checkElementVisibility(
  locator: Locator
): Promise<{
  isVisible: boolean;
  opacity: string;
  zIndex: string;
  display: string;
  visibility: string;
}> {
  return locator.evaluate((el) => {
    const style = window.getComputedStyle(el);
    
    return {
      isVisible: style.opacity !== '0' 
        && style.visibility !== 'hidden'
        && style.display !== 'none',
      opacity: style.opacity,
      zIndex: style.zIndex,
      display: style.display,
      visibility: style.visibility,
    };
  });
}

/**
 * Check element is within viewport bounds
 */
export async function isElementInViewport(
  locator: Locator,
  ratio: number = 1
): Promise<boolean> {
  return locator.evaluate((el, ratio) => {
    const rect = el.getBoundingClientRect();
    const viewportWidth = window.innerWidth || document.documentElement.clientWidth;
    const viewportHeight = window.innerHeight || document.documentElement.clientHeight;

    // Calculate visible area
    const visibleWidth = Math.min(rect.right, viewportWidth) - Math.max(rect.left, 0);
    const visibleHeight = Math.min(rect.bottom, viewportHeight) - Math.max(rect.top, 0);
    const visibleArea = visibleWidth * visibleHeight;
    const totalArea = rect.width * rect.height;
    
    return (visibleArea / totalArea) >= ratio;
  }, ratio);
}

/**
 * Check for horizontal overflow (responsive layout issue)
 */
export async function checkNoHorizontalOverflow(
  page: Page,
  viewportWidth: number
): Promise<boolean> {
  const bodyWidth = await page.evaluate(() => document.body.scrollWidth);
  return bodyWidth <= viewportWidth;
}

/**
 * Get element bounding box
 */
export async function getElementBoundingBox(
  locator: Locator
): Promise<{ x: number; y: number; width: number; height: number } | null> {
  return await locator.boundingBox();
}

/**
 * Check elements don't overlap
 */
export async function checkElementsDontOverlap(
  locator1: Locator,
  locator2: Locator
): Promise<boolean> {
  const box1 = await locator1.boundingBox();
  const box2 = await locator2.boundingBox();
  
  if (!box1 || !box2) return true;
  
  // Check if rectangles overlap
  const xOverlap = Math.max(0, Math.min(box1.x + box1.width, box2.x + box2.width) - Math.max(box1.x, box2.x));
  const yOverlap = Math.max(0, Math.min(box1.y + box1.height, box2.y + box2.height) - Math.max(box1.y, box2.y));
  
  return xOverlap === 0 || yOverlap === 0;
}

/**
 * Visual regression assertion with screenshot comparison
 */
export async function assertVisualMatch(
  page: Page,
  name: string,
  options: ComparisonOptions = {}
): Promise<void> {
  const screenshotOptions = {
    animations: 'disabled',
    caret: 'hide',
  };

  await expect(page).toHaveScreenshot(`${name}.png`, {
    maxDiffPixels: options.maxDiffPixels ?? 100,
    threshold: options.threshold ?? 0.2,
  });
}

/**
 * Run visual tests across all viewports
 */
export async function testAcrossViewports(
  page: Page,
  testFn: (viewport: typeof VIEWPORTS[keyof typeof VIEWPORTS]) => Promise<void>
): Promise<void> {
  for (const [key, viewport] of Object.entries(VIEWPORTS)) {
    await page.setViewportSize({ width: viewport.width, height: viewport.height });
    await testFn(viewport);
  }
}