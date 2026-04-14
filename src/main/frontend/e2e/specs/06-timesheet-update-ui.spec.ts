import { test, expect } from '@playwright/test';
import { hasE2eCredentials } from '../helpers/env';
import { loginWithPassword } from '../helpers/auth';
import { goToMyTimesheet, openMyTimesheetsList } from '../helpers/navigation';
import { updateTimesheetSubmitButton, firstEditTimesheetButton } from '../helpers/timesheet-form';

test.describe('Edit / update timesheet UI', () => {
  test.beforeEach(async ({ page }) => {
    test.skip(!hasE2eCredentials(), 'Set E2E_USERNAME and E2E_PASSWORD');
    await loginWithPassword(page);
    await goToMyTimesheet(page);
  });

  test('opening edit shows Edit Timesheet header and update button', async ({ page }) => {
    const listBtn = page.getByRole('button', { name: /^My Timesheets$/i });
    test.skip(!(await listBtn.isVisible().catch(() => false)), 'No My Timesheets permission');

    await openMyTimesheetsList(page);

    const edit = firstEditTimesheetButton(page);
    test.skip((await edit.count()) === 0, 'No timesheet rows to edit in this account');

    await edit.click();

    await expect(page.getByText('Edit Timesheet', { exact: false }).first()).toBeVisible({ timeout: 20_000 });
    await expect(page.locator('.timesheet-form-wrapper')).toBeVisible();
    await expect(updateTimesheetSubmitButton(page)).toBeVisible();
  });
});
