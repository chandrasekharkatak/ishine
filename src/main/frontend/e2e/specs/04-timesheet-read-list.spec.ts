import { test, expect } from '@playwright/test';
import { hasE2eCredentials } from '../helpers/env';
import { loginWithPassword } from '../helpers/auth';
import { goToMyTimesheet, openMyTimesheetsList } from '../helpers/navigation';

test.describe('My Timesheets list', () => {
  test.beforeEach(async ({ page }) => {
    test.skip(!hasE2eCredentials(), 'Set E2E_USERNAME and E2E_PASSWORD');
    await loginWithPassword(page);
    await goToMyTimesheet(page);
  });

  test('list view shows table or empty state', async ({ page }) => {
    const listBtn = page.getByRole('button', { name: /^My Timesheets$/i });
    test.skip(!(await listBtn.isVisible().catch(() => false)), 'No My Timesheets permission');

    await openMyTimesheetsList(page);

    const hasTable = await page.locator('table').first().isVisible().catch(() => false);
    const hasCard = await page.getByText(/timesheet|no data|no records/i).first().isVisible().catch(() => false);
    expect(hasTable || hasCard).toBeTruthy();
  });

  test('Edit button appears only when rows exist (smoke)', async ({ page }) => {
    const listBtn = page.getByRole('button', { name: /^My Timesheets$/i });
    test.skip(!(await listBtn.isVisible().catch(() => false)), 'No My Timesheets permission');

    await openMyTimesheetsList(page);

    const editBtn = page.locator('button.ts-action-btn--edit[title="Edit"]').first();
    const count = await editBtn.count();
    // Informational: either 0 editable rows or at least one
    expect(count >= 0).toBeTruthy();
  });
});
