import { test, expect } from '@playwright/test';
import { hasE2eCredentials } from '../helpers/env';
import { loginWithPassword } from '../helpers/auth';
import { goToMyTimesheet, openCreateTimesheetTab, openMyTimesheetsList } from '../helpers/navigation';

test.describe('Timesheet navigation (authenticated)', () => {
  test.beforeEach(async ({ page }) => {
    test.skip(!hasE2eCredentials(), 'Set E2E_USERNAME and E2E_PASSWORD');
    await loginWithPassword(page);
  });

  test('direct URL loads My Timesheet route', async ({ page }) => {
    await goToMyTimesheet(page);
    await expect(page.getByRole('button', { name: /Create Timesheet|My Timesheets/i }).first()).toBeVisible({
      timeout: 25_000,
    });
  });

  test('can switch Create Timesheet ↔ My Timesheets when permitted', async ({ page }) => {
    await goToMyTimesheet(page);

    const createBtn = page.getByRole('button', { name: /^Create Timesheet$/i });
    const listBtn = page.getByRole('button', { name: /^My Timesheets$/i });

    if (await createBtn.isVisible().catch(() => false)) {
      await openCreateTimesheetTab(page);
      await openMyTimesheetsList(page);
      await expect(listBtn).toHaveClass(/active/);
    } else {
      test.skip(true, 'User lacks add_timesheet — only list actions available');
    }
  });
});
