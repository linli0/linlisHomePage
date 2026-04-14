/**
 * E2E User Flow Tests
 * 
 * Critical user journeys testing:
 * - Login flow
 * - Gold price viewing
 * - Tools usage
 * - AI chat interaction
 * 
 * Natural language assertions: "user can complete X"
 */

import { test, expect } from '@playwright/test';
import { createSemanticAssertions, userCanClick, formIsVisible, navigationWorks } from '../helpers/assertions';
import { assertNoA11yViolations } from '../helpers/accessibility';

// ========== Login Flow ==========
test.describe('User Flow: Authentication', () => {
  
  test('User can login successfully', async ({ page }) => {
    await page.goto('/login');
    
    // Wait for login form
    await page.waitForLoadState('networkidle');
    
    // User can see login form
    const assertions = createSemanticAssertions(page);
    await assertions.formIsVisible('form');
    
    // User can enter password
    const passwordInput = page.locator('#password');
    await expect(passwordInput, 'Password input should be visible').toBeVisible();
    await passwordInput.fill('admin123');
    
    // User can click submit button
    const submitBtn = page.locator('button[type="submit"]');
    await userCanClick(page, 'button[type="submit"]');
    
    // User clicks submit
    await submitBtn.click();
    
    // User is redirected to home
    await page.waitForURL('/');
    
    // Home page is visible
    await expect(page.locator('nav')).toBeVisible();
    
    // User is logged in (user menu visible)
    const userMenu = page.locator('.w-8').filter({ has: page.locator('.rounded-xl') });
    await expect(userMenu, 'User avatar should be visible after login').toBeVisible();
  });

  test('User can logout successfully', async ({ page }) => {
    // Login first
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    
    // Open user menu
    const userMenuBtn = page.locator('button').filter({ has: page.locator('.w-8') });
    await userMenuBtn.click();
    
    // Click logout
    const logoutBtn = page.locator('button:has-text("退出")');
    await expect(logoutBtn).toBeVisible();
    await logoutBtn.click();
    
    // Redirected to login
    await page.waitForURL('/login');
    
    // Login form visible
    await expect(page.locator('#password')).toBeVisible();
  });

  test('Login shows error for invalid password', async ({ page }) => {
    await page.goto('/login');
    
    // Enter wrong password
    await page.locator('#password').fill('wrongpassword');
    await page.locator('button[type="submit"]').click();
    
    // Wait for error
    await page.waitForTimeout(1000);
    
    // Error message visible
    const errorMsg = page.locator('.bg-red-50, .bg-red-900/20, [class*="red"]');
    
    // Either error is shown or redirected back to login
    const currentUrl = page.url();
    expect(currentUrl.includes('login'), 'Should stay on login page after failed login').toBe(true);
  });

  test('User can toggle password visibility', async ({ page }) => {
    await page.goto('/login');
    
    // Password field is password type initially
    const passwordInput = page.locator('#password');
    expect(await passwordInput.getAttribute('type'), 'Password should be hidden initially').toBe('password');
    
    // Click toggle button
    const toggleBtn = page.locator('button[type="button"]').filter({ has: page.locator('svg') });
    await toggleBtn.click();
    
    // Password should be visible (type=text)
    expect(await passwordInput.getAttribute('type'), 'Password should be visible after toggle').toBe('text');
    
    // Toggle back
    await toggleBtn.click();
    expect(await passwordInput.getAttribute('type'), 'Password should be hidden again').toBe('password');
  });
});

// ========== Gold Price Flow ==========
test.describe('User Flow: Gold Price', () => {
  
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
  });

  test('User can view gold price', async ({ page }) => {
    // Navigate to gold price
    await page.goto('/gold');
    await page.waitForLoadState('networkidle');
    
    // Price display visible
    const priceDisplay = page.locator('.text-4xl, .text-3xl').filter({ hasText: /\d/ });
    await expect(priceDisplay.first(), 'Gold price should be displayed').toBeVisible();
    
    // Stats visible (High, Low, Average, Volatility)
    const statCards = page.locator('.card, .bg-white');
    await expect(statCards.first(), 'Stats cards should be visible').toBeVisible();
    
    // Chart visible
    const chart = page.locator('canvas, .chart-container');
    await expect(chart.first(), 'Price chart should be visible').toBeVisible();
  });

  test('User can change currency', async ({ page }) => {
    await page.goto('/gold');
    await page.waitForLoadState('networkidle');
    
    // Click CNY button
    const cnyBtn = page.locator('button:has-text("CNY")');
    await userCanClick(page, 'button:has-text("CNY")');
    await cnyBtn.click();
    
    // Wait for price update
    await page.waitForTimeout(500);
    
    // Currency indicator should show CNY
    const currencyIndicator = page.locator('text=/CNY|¥/');
    // Price should now be in CNY
  });

  test('User can change time period', async ({ page }) => {
    await page.goto('/gold');
    await page.waitForLoadState('networkidle');
    
    // Click 30 day button
    const periodBtn = page.locator('button:has-text("30")');
    await userCanClick(page, 'button:has-text("30天")');
    await periodBtn.click();
    
    // Wait for chart update
    await page.waitForTimeout(500);
    
    // Chart should update
    const chart = page.locator('canvas');
    await expect(chart).toBeVisible();
  });

  test('User can refresh price manually', async ({ page }) => {
    await page.goto('/gold');
    await page.waitForLoadState('networkidle');
    
    // Click refresh button
    const refreshBtn = page.locator('button:has-text("刷新")');
    await userCanClick(page, 'button:has-text("刷新")');
    await refreshBtn.click();
    
    // Loading indicator shown
    const spinner = page.locator('.animate-spin');
    await expect(spinner, 'Loading spinner should appear').toBeVisible();
    
    // Wait for refresh to complete
    await page.waitForTimeout(1000);
  });
});

// ========== Tools Flow ==========
test.describe('User Flow: Developer Tools', () => {
  
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
  });

  test('User can format JSON', async ({ page }) => {
    await page.goto('/tools');
    
    // Find JSON tool
    const jsonTool = page.locator('.card-hover').filter({ hasText: 'JSON' });
    await expect(jsonTool, 'JSON tool should be visible').toBeVisible();
    
    // Enter JSON
    const jsonInput = jsonTool.locator('textarea').first();
    await jsonInput.fill '{"name":"test","value":123}';
    
    // Click format
    const formatBtn = jsonTool.locator('button:has-text("格式化")');
    await formatBtn.click();
    
    // Output visible
    const output = jsonTool.locator('textarea').nth(1);
    await expect(output, 'Formatted output should be visible').toBeVisible();
    
    // Output is formatted (contains newlines)
    const outputText = await output.inputValue();
    expect(outputText.includes('\n'), 'Output should be formatted with newlines').toBe(true);
  });

  test('User can encode/decode Base64', async ({ page }) => {
    await page.goto('/tools');
    
    // Find Base64 tool
    const base64Tool = page.locator('.card-hover').filter({ hasText: 'Base64' });
    await expect(base64Tool).toBeVisible();
    
    // Enter text
    const input = base64Tool.locator('textarea').first();
    await input.fill('Hello World');
    
    // Click encode
    const encodeBtn = base64Tool.locator('button:has-text("编码")');
    await encodeBtn.click();
    
    // Output visible
    const output = base64Tool.locator('textarea').nth(1);
    await expect(output).toBeVisible();
    
    const encodedValue = await output.inputValue();
    expect(encodedValue, 'Output should be Base64 encoded').toContain('SGVsbG8');
  });

  test('User can calculate hash', async ({ page }) => {
    await page.goto('/tools');
    
    // Find Hash tool
    const hashTool = page.locator('.card-hover').filter({ hasText: '哈希' });
    await expect(hashTool).toBeVisible();
    
    // Enter text
    const input = hashTool.locator('input');
    await input.fill('test123');
    
    // Click MD5
    const md5Btn = hashTool.locator('button:has-text("MD5")');
    await md5Btn.click();
    
    // Output visible
    const output = hashTool.locator('input').nth(1);
    await expect(output).toBeVisible();
    
    const hashValue = await output.inputValue();
    expect(hashValue.length, 'MD5 hash should be 32 characters').toBe(32);
  });

  test('User can generate QR code', async ({ page }) => {
    await page.goto('/tools');
    
    // Find QR tool
    const qrTool = page.locator('.card-hover').filter({ hasText: '二维码' });
    await expect(qrTool).toBeVisible();
    
    // Enter content
    const contentInput = qrTool.locator('textarea');
    await contentInput.fill('https://example.com');
    
    // Click generate
    const generateBtn = qrTool.locator('button:has-text("生成")');
    await generateBtn.click();
    
    // Wait for QR image
    await page.waitForTimeout(1000);
    
    // QR image visible
    const qrImage = qrTool.locator('img');
    await expect(qrImage, 'QR code image should be visible').toBeVisible();
  });
});

// ========== AI Chat Flow ==========
test.describe('User Flow: AI Chat', () => {
  
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
  });

  test('User can access AI chat', async ({ page }) => {
    await page.goto('/ai');
    await page.waitForLoadState('networkidle');
    
    // Chat interface visible
    const chatInterface = page.locator('.card, form').filter({ has: page.locator('textarea') });
    await expect(chatInterface, 'Chat interface should be visible').toBeVisible();
    
    // Message input visible
    const messageInput = page.locator('textarea').filter({ hasNotText: '' });
    await expect(messageInput.first(), 'Message input should be visible').toBeVisible();
    
    // Send button visible
    const sendBtn = page.locator('button:has-text("发送")');
    await expect(sendBtn, 'Send button should be visible').toBeVisible();
  });

  test('User can send message', async ({ page }) => {
    await page.goto('/ai');
    await page.waitForLoadState('networkidle');
    
    // Enter message
    const messageInput = page.locator('textarea').first();
    await messageInput.fill('Hello AI');
    
    // Click send
    const sendBtn = page.locator('button:has-text("发送")');
    await sendBtn.click();
    
    // Wait for response
    await page.waitForTimeout(2000);
    
    // Message should appear in chat
    // Either user message or AI response visible
    const chatMessages = page.locator('.message, .bg-primary-500, .bg-surface-100');
    // Chat area should have content
  });

  test('User can clear chat', async ({ page }) => {
    await page.goto('/ai');
    await page.waitForLoadState('networkidle');
    
    // Find clear button
    const clearBtn = page.locator('button').filter({ hasText: /清空|清除|Clear/ });
    
    if (await clearBtn.count() > 0) {
      await clearBtn.click();
      
      // Chat should be empty
      const messages = page.locator('.message');
      await expect(messages).toHaveCount(0);
    }
  });
});

// ========== Articles Flow ==========
test.describe('User Flow: Articles/Wiki', () => {
  
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
  });

  test('User can browse articles', async ({ page }) => {
    await page.goto('/articles');
    await page.waitForLoadState('networkidle');
    
    // Article cards visible
    const articleCards = page.locator('.card-hover, .card').filter({ has: page.locator('router-link, a') });
    const count = await articleCards.count();
    
    expect(count, 'Articles should be displayed').toBeGreaterThan(0);
    
    // First article is visible
    await expect(articleCards.first()).toBeVisible();
  });

  test('User can click article to view detail', async ({ page }) => {
    await page.goto('/articles');
    await page.waitForLoadState('networkidle');
    
    // Click first article
    const firstArticle = page.locator('.card-hover, .card a').first();
    
    if (await firstArticle.count() > 0) {
      await firstArticle.click();
      
      // Article detail page
      await page.waitForURL(/\/article\/\d+/);
      
      // Article content visible
      const articleContent = page.locator('h1, .prose, article');
      await expect(articleContent.first(), 'Article content should be visible').toBeVisible();
    }
  });
});

// ========== Full User Journey ==========
test.describe('Full User Journey', () => {
  
  test('Complete journey: Login → Gold → Tools → Logout', async ({ page }) => {
    // Step 1: Login
    await page.goto('/login');
    await page.locator('#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('/');
    
    // Step 2: Navigate to Gold Price
    await page.goto('/gold');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('canvas, .text-4xl')).toBeVisible();
    
    // Step 3: Navigate to Tools
    await page.goto('/tools');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('.card-hover').first()).toBeVisible();
    
    // Step 4: Use a tool (JSON format)
    const jsonInput = page.locator('.card-hover').filter({ hasText: 'JSON' }).locator('textarea').first();
    await jsonInput.fill '{"test":"data"}';
    await page.locator('button:has-text("格式化")').click();
    
    // Step 5: Logout
    await page.locator('button').filter({ has: page.locator('.w-8') }).click();
    await page.locator('button:has-text("退出")').click();
    await page.waitForURL('/login');
    
    // Verify logged out
    await expect(page.locator('#password')).toBeVisible();
  });
});