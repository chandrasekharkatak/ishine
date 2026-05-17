import type { Page } from '@playwright/test';
import { expect } from '@playwright/test';
import { getE2eOtp } from './env';

function waitForApi(
  page: Page,
  apiPath: string,
  timeout = 30_000,
): Promise<boolean> {
  return page
    .waitForResponse(
      (response) =>
        response.url().includes(apiPath) && response.request().resourceType() === 'xhr',
      { timeout },
    )
    .then(() => true)
    .catch(() => false);
}

/**
 * Fills login form and submits. Handles optional OTP step if #userOtp appears.
 * Caller must ensure E2E_USERNAME / E2E_PASSWORD are set.
 */
export async function loginWithPassword(page: Page): Promise<void> {
  const user = process.env.E2E_USERNAME!.trim();
  const pass = process.env.E2E_PASSWORD!.trim();

  const loginReady = async (timeoutMs: number): Promise<boolean> =>
    page
      .waitForFunction(() => {
        const hash = (window.location.hash || '').toLowerCase();
        const movedFromLoginHash = !!hash && !(hash === '#/login' || hash.endsWith('/login'));
        const hasSession = !!sessionStorage.getItem('token') || !!sessionStorage.getItem('currentUser');
        return movedFromLoginHash || hasSession;
      }, { timeout: timeoutMs })
      .then(() => true)
      .catch(() => false);

  const handleProceedAndOtpQuick = async (): Promise<void> => {
    const proceedBtn = page.getByRole('button', { name: /yes\s*,?\s*proceed/i }).first();
    if (await proceedBtn.isVisible().catch(() => false)) {
      await proceedBtn.click();
    }

    // There can be multiple #userOtp elements in DOM; target visible one only.
    const otpInput = page.locator('#userOtp:visible').first();
    const otpVisible = await otpInput
      .waitFor({ state: 'visible', timeout: 12_000 })
      .then(() => true)
      .catch(() => false);
    if (!otpVisible) return;

    const otp = getE2eOtp();
    if (!otp) {
      throw new Error(
        'OTP field visible but E2E_OTP is not set. Use a test user without OTP, or set E2E_OTP for this run.',
      );
    }
    const waitForSubFeatures = page
      .waitForResponse(
        (response) =>
          response.request().method() === 'GET' &&
          response.url().includes('/api/getAllSubFeatures'),
        { timeout: 20_000 },
      )
      .then(() => true)
      .catch(() => false);

    await otpInput.fill(otp);
    await page.getByRole('button', { name: /^Confirm$/i }).click();
    await waitForSubFeatures;
  };

  await page.goto('/login', { waitUntil: 'domcontentloaded' });
  await expect(page.locator('#username')).toBeVisible();

  let loginCompletedAfterRetry = false;
  for (let attempt = 1; attempt <= 2; attempt++) {
    await page.locator('#username').fill(user);
    await page.locator('#userPassword').fill(pass);
    const loginBtn = page.getByRole('button', { name: /LOG\s*IN/i }).first();
    if (await loginBtn.isVisible().catch(() => false)) {
      await loginBtn.click();
    }

    await handleProceedAndOtpQuick();
    loginCompletedAfterRetry = await loginReady(20_000);
    if (loginCompletedAfterRetry) break;
  }

  if (!loginCompletedAfterRetry) {
    const errorText = await page
      .locator('.auth__error__msg')
      .first()
      .textContent()
      .catch(() => null);
    throw new Error(
      `Login did not complete. Current URL: ${page.url()}${errorText ? ` | UI error: ${errorText.trim()}` : ''}`,
    );
  }

  // If login lands on Home, wait for common home bootstrap APIs (best-effort).
  const hash = await page.evaluate(() => (window.location.hash || '').toLowerCase());
  if (hash.includes('/home')) {
    await Promise.allSettled([
      waitForApi(page, '/api/getEmployeeProfileCompletion', 12_000),
      waitForApi(page, '/api/getAllNotifications', 12_000),
      waitForApi(page, '/api/getTimesheetsForHomePageByEmpId', 12_000),
    ]);
  }
}
