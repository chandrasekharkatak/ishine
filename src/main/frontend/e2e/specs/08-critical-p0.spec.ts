import { expect, test } from '@playwright/test';
import { hasE2eCredentials } from '../helpers/env';
import { loginWithPassword } from '../helpers/auth';
import { goToMyTimesheet, openCreateTimesheetTab, openMyTimesheetsList } from '../helpers/navigation';
import { createTimesheetSubmitButton, firstEditTimesheetButton, updateTimesheetSubmitButton } from '../helpers/timesheet-form';

test.describe('Phase 2 - Critical P0 scenarios', () => {
  test('TS-AUTH-001: login page has required controls', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('#username')).toBeVisible();
    await expect(page.locator('#userPassword')).toBeVisible();
    await expect(page.getByRole('button', { name: /LOG\s*IN/i })).toBeVisible();
  });

  test('TS-AUTH-003: invalid credentials do not login', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#username').fill('__invalid_user_e2e__');
    await page.locator('#userPassword').fill('__invalid_pass_e2e__');
    await page.getByRole('button', { name: /LOG\s*IN/i }).click();
    await expect(page).toHaveURL(/\/login/);
  });

  test('TS-ROUTE-002: unauthenticated access to my-timesheet is blocked', async ({ page }) => {
    await page.goto('/user-timesheet/my-timesheet');
    await page.waitForTimeout(1200);
    const redirectedToLogin = page.url().includes('/login');
    const loginInputVisible = await page.locator('#username').isVisible().catch(() => false);
    expect(redirectedToLogin || loginInputVisible).toBeTruthy();
  });

  test.describe('Authenticated critical journeys', () => {
    test.beforeEach(async ({ page }) => {
      test.skip(!hasE2eCredentials(), 'Set E2E_USERNAME and E2E_PASSWORD (and E2E_OTP when needed).');
      await loginWithPassword(page);
      await goToMyTimesheet(page);
    });

    test('TS-AUTH-004 + TS-ROUTE-001: login lands outside /login and my-timesheet route opens', async ({ page }) => {
      await expect(page).toHaveURL(/user-timesheet\/my-timesheet/);
      await expect(page).not.toHaveURL(/\/login$/);
    });

    test('TS-UI-001 + TS-UI-002: create form opens and self mode is default', async ({ page }) => {
      const createBtn = page.getByRole('button', { name: /^Create Timesheet$/i });
      test.skip(!(await createBtn.isVisible().catch(() => false)), 'User lacks create permission.');

      await openCreateTimesheetTab(page);
      await expect(page.locator('.timesheet-form-wrapper')).toBeVisible();
      const selfRadio = page.locator('input[type="radio"][name="timesheetFor"][value="self"]');
      await expect(selfRadio).toBeChecked();
    });

    test('TS-VAL-001: empty create submit shows alert modal', async ({ page }) => {
      const createBtn = page.getByRole('button', { name: /^Create Timesheet$/i });
      test.skip(!(await createBtn.isVisible().catch(() => false)), 'User lacks create permission.');

      await openCreateTimesheetTab(page);
      await createTimesheetSubmitButton(page).click();
      await expect(page.locator('.modal.ts-alert-modal, .ts-alert-modal').first()).toBeVisible();
    });

    test('TS-LIST-001: my timesheets list renders table or no-data state', async ({ page }) => {
      const listBtn = page.getByRole('button', { name: /^My Timesheets$/i });
      test.skip(!(await listBtn.isVisible().catch(() => false)), 'User lacks list permission.');

      await openMyTimesheetsList(page);
      const tableVisible = await page.locator('table').first().isVisible().catch(() => false);
      const emptyStateVisible = await page.getByText(/no data|no records|timesheet/i).first().isVisible().catch(() => false);
      expect(tableVisible || emptyStateVisible).toBeTruthy();
    });

    test('TS-UPD-001 + TS-UPD-002: edit flow opens update mode with update action', async ({ page }) => {
      const listBtn = page.getByRole('button', { name: /^My Timesheets$/i });
      test.skip(!(await listBtn.isVisible().catch(() => false)), 'User lacks list permission.');

      await openMyTimesheetsList(page);
      const editBtn = firstEditTimesheetButton(page);
      test.skip((await editBtn.count()) === 0, 'No editable rows in current account.');

      await editBtn.click();
      await expect(page.getByText('Edit Timesheet', { exact: false }).first()).toBeVisible();
      await expect(updateTimesheetSubmitButton(page)).toBeVisible();
    });
  });
});
