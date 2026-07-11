/**
 * Accessibility Testing Utilities
 * 
 * WCAG 2.1 AA Compliance Testing with axe-core
 * - Color contrast ≥4.5:1 for normal text, ≥3:1 for large text
 * - Keyboard navigation testing
 * - Screen reader compatibility
 * - Form validation and error messages
 */

import AxeBuilder from '@axe-core/playwright';
import { Page, Locator, expect } from '@playwright/test';
import { createHtmlReport } from 'axe-html-reporter';
import * as fs from 'fs';
import * as path from 'path';

// WCAG 2.1 AA rule tags
export const WCAG_AA_TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'];

// Impact levels
export type ImpactLevel = 'critical' | 'serious' | 'moderate' | 'minor';

// Accessibility scan options
export interface A11yScanOptions {
  /** Include only specific element(s) */
  include?: string | string[];
  /** Exclude specific element(s) */
  exclude?: string | string[];
  /** Filter by WCAG tags */
  tags?: string[];
  /** Disable specific rules */
  disabledRules?: string[];
  /** Filter by impact level */
  impact?: ImpactLevel[];
  /** Generate HTML report */
  generateReport?: boolean;
  /** Report output directory */
  reportDir?: string;
  /** Custom report name */
  reportName?: string;
}

/**
 * Scan page for accessibility violations
 */
export async function scanForA11yViolations(
  page: Page,
  options: A11yScanOptions = {}
): Promise<AxeResults> {
  const builder = new AxeBuilder({ page })
    .withTags(WCAG_AA_TAGS);

  // Include specific elements
  if (options.include) {
    const includes = Array.isArray(options.include) ? options.include : [options.include];
    includes.forEach(sel => builder.include(sel));
  }

  // Exclude specific elements
  if (options.exclude) {
    const excludes = Array.isArray(options.exclude) ? options.exclude : [options.exclude];
    excludes.forEach(sel => builder.exclude(sel));
  }

  // Custom tags
  if (options.tags) {
    builder.withTags(options.tags);
  }

  // Disable specific rules
  if (options.disabledRules) {
    builder.disableRules(options.disabledRules);
  }

  const results = await builder.analyze();

  // Generate HTML report
  if (options.generateReport) {
    const reportDir = options.reportDir || 'reports/accessibility';
    const reportName = options.reportName || `a11y-report-${Date.now()}.html`;
    
    if (!fs.existsSync(reportDir)) {
      fs.mkdirSync(reportDir, { recursive: true });
    }

    createHtmlReport({
      results,
      options: {
        outputDir: reportDir,
        reportFileName: reportName,
      },
    });
  }

  return results;
}

/**
 * Filter violations by impact level
 */
export function filterViolationsByImpact(
  results: AxeResults,
  impacts: ImpactLevel[]
): AxeViolation[] {
  return results.violations.filter(v => impacts.includes(v.impact as ImpactLevel));
}

/**
 * Log violations to console in readable format
 */
export function logViolations(results: AxeResults): void {
  if (results.violations.length === 0) {
    console.log('✅ No accessibility violations found');
    return;
  }

  console.log(`\n❌ ${results.violations.length} accessibility violation(s) detected:\n`);
  
  results.violations.forEach(violation => {
    console.log(`[${violation.impact}] ${violation.id}: ${violation.description}`);
    console.log(`  Help: ${violation.helpUrl}`);
    console.log(`  Nodes affected: ${violation.nodes.length}`);
    violation.nodes.forEach(node => {
      console.log(`    - ${node.target}`);
    });
    console.log('');
  });
}

/**
 * Assert no accessibility violations
 */
export async function assertNoA11yViolations(
  page: Page,
  options: A11yScanOptions = {}
): Promise<void> {
  const results = await scanForA11yViolations(page, options);
  
  // Filter by impact if specified
  const violations = options.impact 
    ? filterViolationsByImpact(results, options.impact)
    : results.violations;

  if (violations.length > 0) {
    logViolations(results);
  }

  expect(violations).toEqual([]);
}

/**
 * Check color contrast ratio
 */
export async function checkColorContrast(
  page: Page,
  selector: string
): Promise<{ ratio: number; passes: boolean }> {
  const element = page.locator(selector);
  
  const contrastInfo = await element.evaluate((el) => {
    const style = window.getComputedStyle(el);
    const color = style.color;
    const bgColor = style.backgroundColor;
    
    // Parse RGB values
    const parseRGB = (colorStr: string) => {
      const match = colorStr.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/);
      if (match) {
        return [parseInt(match[1]), parseInt(match[2]), parseInt(match[3])];
      }
      return [0, 0, 0];
    };
    
    const rgb = parseRGB(color);
    const bgRgb = parseRGB(bgColor);
    
    // Calculate relative luminance
    const luminance = (r: number, g: number, b: number) => {
      const rsrgb = r / 255;
      const gsrgb = g / 255;
      const bsrgb = b / 255;
      
      const rLin = rsrgb <= 0.03928 ? rsrgb / 12.92 : Math.pow((rsrgb + 0.055) / 1.055, 2.4);
      const gLin = gsrgb <= 0.03928 ? gsrgb / 12.92 : Math.pow((gsrgb + 0.055) / 1.055, 2.4);
      const bLin = bsrgb <= 0.03928 ? bsrgb / 12.92 : Math.pow((bsrgb + 0.055) / 1.055, 2.4);
      
      return 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin;
    };
    
    const L1 = luminance(rgb[0], rgb[1], rgb[2]);
    const L2 = luminance(bgRgb[0], bgRgb[1], bgRgb[2]);
    
    // Calculate contrast ratio
    const lighter = Math.max(L1, L2);
    const darker = Math.min(L1, L2);
    const ratio = (lighter + 0.05) / (darker + 0.05);
    
    return { ratio, passes: ratio >= 4.5 };
  });
  
  return contrastInfo;
}

/**
 * Test keyboard navigation order
 */
export async function testKeyboardNavigation(
  page: Page,
  expectedOrder: string[]
): Promise<void> {
  await page.keyboard.press('Tab');
  
  for (const selector of expectedOrder) {
    const element = page.locator(selector);
    await expect(element).toBeFocused();
    await page.keyboard.press('Tab');
  }
}

/**
 * Check if element has proper ARIA attributes
 */
export async function checkAriaAttributes(
  page: Page,
  selector: string,
  expectedAttributes: Record<string, string>
): Promise<void> {
  const element = page.locator(selector);
  
  for (const [attr, value] of Object.entries(expectedAttributes)) {
    await expect(element).toHaveAttribute(attr, value);
  }
}

/**
 * Check form field has accessible label
 */
export async function checkFormFieldAccessibility(
  page: Page,
  inputSelector: string
): Promise<{ hasLabel: boolean; labelId?: string }> {
  const input = page.locator(inputSelector);
  
  const info = await input.evaluate((el) => {
    const id = el.id;
    const ariaLabel = el.getAttribute('aria-label');
    const ariaLabelledBy = el.getAttribute('aria-labelledby');
    
    // Check if input has explicit label
    if (id) {
      const label = document.querySelector(`label[for="${id}"]`);
      if (label) {
        return { hasLabel: true, labelId: id };
      }
    }
    
    // Check aria-label
    if (ariaLabel) {
      return { hasLabel: true, labelId: ariaLabel };
    }
    
    // Check aria-labelledby
    if (ariaLabelledBy) {
      return { hasLabel: true, labelId: ariaLabelledBy };
    }
    
    return { hasLabel: false };
  });
  
  return info;
}

// Type definitions for axe-core results
interface AxeResults {
  violations: AxeViolation[];
  passes: AxeResultItem[];
  incomplete: AxeResultItem[];
  inapplicable: AxeResultItem[];
}

interface AxeViolation extends AxeResultItem {
  impact: string;
}

interface AxeResultItem {
  id: string;
  description: string;
  help: string;
  helpUrl: string;
  nodes: AxeNode[];
}

interface AxeNode {
  target: string[];
  html: string;
}