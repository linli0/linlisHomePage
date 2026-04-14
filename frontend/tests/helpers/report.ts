/**
 * Test Report Configuration
 * 
 * Generates:
 * - Accessibility HTML reports (axe-html-reporter)
 * - Playwright HTML reports
 * - Test summary reports
 */

import { createHtmlReport } from 'axe-html-reporter';
import * as fs from 'fs';
import * as path from 'path';

// Report configuration
export const reportConfig = {
  accessibility: {
    outputDir: 'reports/accessibility',
    reportFileName: 'accessibility-report.html',
  },
  playwright: {
    outputDir: 'playwright-report',
  },
  testResults: {
    outputDir: 'test-results',
  },
};

// Generate accessibility report from axe-core results
export function generateA11yReport(results: any, pageName: string): void {
  const outputDir = reportConfig.accessibility.outputDir;
  const reportFileName = `${pageName}-a11y-report.html`;
  
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }
  
  createHtmlReport({
    results,
    options: {
      outputDir,
      reportFileName,
      customSummary: `
        <h3>Accessibility Report: ${pageName}</h3>
        <p>Date: ${new Date().toISOString()}</p>
        <p>Violations: ${results.violations.length}</p>
        <p>Passes: ${results.passes.length}</p>
      `,
    },
  });
}

// Generate test summary report
export function generateTestSummary(testResults: any): void {
  const outputDir = reportConfig.testResults.outputDir;
  const summaryPath = path.join(outputDir, 'summary.json');
  
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }
  
  const summary = {
    timestamp: new Date().toISOString(),
    totalTests: testResults.totalTests || 0,
    passed: testResults.passed || 0,
    failed: testResults.failed || 0,
    skipped: testResults.skipped || 0,
    duration: testResults.duration || 0,
    coverage: testResults.coverage || null,
    accessibility: {
      violations: testResults.accessibilityViolations || 0,
      passes: testResults.accessibilityPasses || 0,
    },
  };
  
  fs.writeFileSync(summaryPath, JSON.stringify(summary, null, 2));
}

// Report types
export interface A11yReportOptions {
  outputDir?: string;
  reportFileName?: string;
  customSummary?: string;
}

export interface TestSummary {
  timestamp: string;
  totalTests: number;
  passed: number;
  failed: number;
  skipped: number;
  duration: number;
  coverage?: number;
  accessibility?: {
    violations: number;
    passes: number;
  };
}

// Export default configuration
export default reportConfig;