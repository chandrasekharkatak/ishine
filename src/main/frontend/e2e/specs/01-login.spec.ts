import { test, expect } from '@playwright/test';
import { missingE2eCredentialVars } from '../helpers/env';
import { loginWithPassword } from '../helpers/auth';

test.describe('Login page', () => {
  test('shows username and password fields', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('#username')).toBeVisible();
    await expect(page.locator('#userPassword')).toBeVisible();
    await expect(page.getByRole('button', { name: /LOG\s*IN/i })).toBeVisible();
  });

  test('rejects empty submit or shows validation', async ({ page }) => {
    await page.goto('/login');
    await page.getByRole('button', { name: /LOG\s*IN/i }).click();
    // HTML5 validation may block submit — username stays empty or browser tooltip
    const user = page.locator('#username');
    const v = await user.evaluate((el: HTMLInputElement) => el.validity.valueMissing);
    expect(v).toBeTruthy();
  });

  test('wrong password does not reach home', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#username').fill('__e2e_invalid_user__');
    await page.locator('#userPassword').fill('__e2e_invalid_pass__');
    await page.getByRole('button', { name: /LOG\s*IN/i }).click();
    await page.waitForTimeout(2000);
    await expect(page).toHaveURL(/\/login/);
  });

  test('successful login with env credentials (handles proceed modal)', async ({ page }) => {
    const missing = missingE2eCredentialVars();
    expect(
      missing,
      `Missing env vars: ${missing.join(', ')}. Set them in frontend/.env.e2e or export in shell before running Playwright.`,
    ).toEqual([]);
    await loginWithPassword(page);
    await expect(page).toHaveURL(/\/home(\/|$|\?)/);
  });
});
