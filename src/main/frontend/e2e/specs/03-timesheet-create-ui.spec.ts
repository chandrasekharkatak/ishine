import { test, expect } from '@playwright/test';
import { hasE2eCredentials } from '../helpers/env';
import { loginWithPassword } from '../helpers/auth';
import { goToMyTimesheet, openCreateTimesheetTab } from '../helpers/navigation';
import { createTimesheetSubmitButton } from '../helpers/timesheet-form';

test.describe('Create timesheet UI', () => {
  test.beforeEach(async ({ page }) => {
    test.skip(!hasE2eCredentials(), 'Set E2E_USERNAME and E2E_PASSWORD');
    await loginWithPassword(page);
    await goToMyTimesheet(page);
  });

  test('create form shows basic sections and Self default', async ({ page }) => {
    const createBtn = page.getByRole('button', { name: /^Create Timesheet$/i });
    test.skip(!(await createBtn.isVisible().catch(() => false)), 'No Create Timesheet permission');

    await openCreateTimesheetTab(page);

    await expect(page.locator('.timesheet-form-wrapper')).toBeVisible();
    await expect(page.getByText('Basic Information', { exact: false })).toBeVisible();
    await expect(page.getByText('Timesheet Application For', { exact: false })).toBeVisible();

    const selfRadio = page.locator('input[type="radio"][name="timesheetFor"][value="self"]');
    await expect(selfRadio).toBeChecked();
  });

  test('Create Timesheet action button is present', async ({ page }) => {
    const createBtn = page.getByRole('button', { name: /^Create Timesheet$/i });
    test.skip(!(await createBtn.isVisible().catch(() => false)), 'No Create Timesheet permission');

    await openCreateTimesheetTab(page);
    const submit = createTimesheetSubmitButton(page);
    await expect(submit).toBeVisible();
    await expect(submit).toBeEnabled();
  });
});
