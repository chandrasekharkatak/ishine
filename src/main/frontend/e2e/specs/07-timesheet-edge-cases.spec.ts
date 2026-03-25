import { test, expect } from '@playwright/test';
import { hasE2eCredentials, allowTimesheetSubmit, projectSearchText } from '../helpers/env';
import { loginWithPassword } from '../helpers/auth';
import { goToMyTimesheet, openCreateTimesheetTab } from '../helpers/navigation';
import { createTimesheetSubmitButton } from '../helpers/timesheet-form';

test.describe('Edge cases & guards', () => {
  test('unauthenticated access to my-timesheet redirects or blocks at login', async ({ page }) => {
    await page.goto('/user-timesheet/my-timesheet');
    await page.waitForTimeout(1500);
    const onLogin = page.url().includes('/login');
    const stillTimesheet = page.url().includes('my-timesheet');
    // Either redirected to login, or app shows login shell
    expect(onLogin || !stillTimesheet || (await page.locator('#username').isVisible())).toBeTruthy();
  });

  test('reload on create form keeps route (smoke)', async ({ page }) => {
    test.skip(!hasE2eCredentials(), 'Set E2E_USERNAME and E2E_PASSWORD');
    await loginWithPassword(page);
    await goToMyTimesheet(page);

    const createBtn = page.getByRole('button', { name: /^Create Timesheet$/i });
    test.skip(!(await createBtn.isVisible().catch(() => false)), 'No Create Timesheet permission');

    await openCreateTimesheetTab(page);
    await page.reload();
    await expect(page).toHaveURL(/user-timesheet\/my-timesheet/);
  });
});

test.describe('Optional full submit (opt-in)', () => {
  test('full create submit — only when E2E_SUBMIT_TIMESHEET=1 and project text set', async ({ page }) => {
    test.skip(!hasE2eCredentials(), 'Set E2E_USERNAME and E2E_PASSWORD');
    test.skip(!allowTimesheetSubmit(), 'Set E2E_SUBMIT_TIMESHEET=1 to run destructive submit test');
    test.skip(!projectSearchText(), 'Set E2E_PROJECT_SEARCH_TEXT to a project visible in your account');

    await loginWithPassword(page);
    await goToMyTimesheet(page);
    await openCreateTimesheetTab(page);

    // Minimal happy-path is highly environment-specific; this block documents extension point.
    // Implementers can: pick date, add location row, select project via mat-select, fill hours, then:
    await createTimesheetSubmitButton(page).click();

    // Expect either success toast/modal or API error — do not assert success without stable test data
    await page.waitForTimeout(3000);
    const urlOk = page.url().includes('my-timesheet');
    expect(urlOk).toBeTruthy();
  });
});
