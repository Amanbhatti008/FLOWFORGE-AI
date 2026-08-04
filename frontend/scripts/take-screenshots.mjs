import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch({ headless: false });
  const page = await browser.newPage();
  
  // Set viewport to a typical desktop size
  await page.setViewportSize({ width: 1440, height: 900 });

  // 1. Login Page
  await page.goto('http://localhost:5173/login');
  await page.waitForTimeout(2000); // let animations settle
  await page.screenshot({ path: '../docs/assets/login.png' });
  console.log('Took login.png');

  // Login
  await page.fill('input[type="email"]', 'admin@flowforge.com');
  await page.fill('input[type="password"]', 'password123');
  await page.click('button[type="submit"]');

  // 2. Dashboard
  await page.waitForURL('http://localhost:5173/dashboard');
  await page.waitForTimeout(2000); // let data load
  await page.screenshot({ path: '../docs/assets/dashboard.png' });
  console.log('Took dashboard.png');

  // 3. Builder
  await page.click('a:has-text("Automation Builder")');
  await page.waitForURL('http://localhost:5173/builder');
  // Wait for canvas to render completely
  await page.waitForTimeout(2000);
  
  await page.screenshot({ path: '../docs/assets/builder.png' });
  console.log('Took builder.png');

  // 4. Execution View
  await page.click('button:has-text("Run Pipeline")');
  await page.waitForTimeout(2000); // wait for execution sidebar to slide in and run
  await page.screenshot({ path: '../docs/assets/execution.png' });
  console.log('Took execution.png');

  await browser.close();
})();
