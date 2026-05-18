import { test, expect } from '@playwright/test';
import { hasE2eCredentials } from '../helpers/env';
import { loginWithPassword } from '../helpers/auth';
import { goToMyTimesheet, openCreateTimesheetTab } from '../helpers/navigation';
import { createTimesheetSubmitButton } from '../helpers/timesheet-form';

test.describe('Timesheet client-side validation', () => {
  test.beforeEach(async ({ page }) => {
    test.skip(!hasE2eCredentials(), 'Set E2E_USERNAME and E2E_PASSWORD');
    await loginWithPassword(page);
    await goToMyTimesheet(page);
  });

  test('submitting empty create form opens validation modal', async ({ page }) => {
    const createBtn = page.getByRole('button', { name: /^Create Timesheet$/i });
    test.skip(!(await createBtn.isVisible().catch(() => false)), 'No Create Timesheet permission');

    await openCreateTimesheetTab(page);
    await createTimesheetSubmitButton(page).click();

    // openAlertMod → NgbModal with modalDialogClass: ts-alert-modal
    const dialog = page.locator('.modal.ts-alert-modal, .ts-alert-modal').first();
    await expect(dialog).toBeVisible({ timeout: 10_000 });
  });
});
