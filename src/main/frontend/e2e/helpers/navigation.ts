import type { Page } from '@playwright/test';
import { expect } from '@playwright/test';

const MY_TIMESHEET_PATH = '/user-timesheet/my-timesheet';

function waitForApi(
  page: Page,
  apiPath: string,
  timeout = 20_000,
): Promise<boolean> {
  return page
    .waitForResponse((response) => response.url().includes(apiPath), { timeout })
    .then(() => true)
    .catch(() => false);
}

export async function goToMyTimesheet(page: Page): Promise<void> {
  // Start watchers before navigation so fast API calls are not missed.
  const timesheetBootstrapApis = Promise.allSettled([
    waitForApi(page, '/api/getServerDate'),
    waitForApi(page, '/api/getAllHoliday'),
    waitForApi(page, '/api/getAllHolidays'),
    waitForApi(page, '/api/getAllMyLeaveApplicationsByEmpId'),
    waitForApi(page, '/api/v2/timesheet/getAllMyTimesheetsByEmpId'),
    waitForApi(page, '/api/getAllMyTeamTimesheets'),
    waitForApi(page, '/api/wasEmployeeInClientProjCurrAndPrevMon'),
    waitForApi(page, '/api/isInTNMProject'),
  ]);

  // Hash-route navigation can be SPA-only; avoid relying on navigation events.
  await page.goto(`/#${MY_TIMESHEET_PATH}`, { waitUntil: 'domcontentloaded' });
  let onTimesheetRoute = await page
    .waitForFunction(
      (targetPath) =>
        window.location.pathname.includes(targetPath) ||
        window.location.hash.includes(targetPath),
      MY_TIMESHEET_PATH,
      { timeout: 15_000 },
    )
    .then(() => true)
    .catch(() => false);

  if (!onTimesheetRoute) {
    // Fallback for cases where router ignores initial goto but accepts hash reassignment.
    await page.evaluate((targetPath) => {
      window.location.hash = `#${targetPath}`;
    }, MY_TIMESHEET_PATH);
    onTimesheetRoute = await page
      .waitForFunction(
        (targetPath) =>
          window.location.pathname.includes(targetPath) ||
          window.location.hash.includes(targetPath),
        MY_TIMESHEET_PATH,
        { timeout: 10_000 },
      )
      .then(() => true)
      .catch(() => false);
  }

  if (!onTimesheetRoute) {
    throw new Error(`Failed to navigate to My Timesheet route. Current URL: ${page.url()}`);
  }

  // Best-effort: do not fail test if a permission-gated call is skipped.
  await timesheetBootstrapApis;
}

/** Opens Create Timesheet tab if the action button exists (permission-gated). */
export async function openCreateTimesheetTab(page: Page): Promise<void> {
  const createBtn = page.getByRole('button', { name: /Create Timesheet/i });
  await expect(createBtn).toBeVisible({ timeout: 20_000 });
  await createBtn.click();
  await expect(page.locator('.timesheet-form-wrapper')).toBeVisible({ timeout: 20_000 });
  await expect(page.getByText('Timesheet Entry', { exact: false }).first()).toBeVisible();
}

/** Switches to list view */
export async function openMyTimesheetsList(page: Page): Promise<void> {
  const listBtn = page.getByRole('button', { name: /My Timesheets/i });
  await expect(listBtn).toBeVisible({ timeout: 20_000 });
  await listBtn.click();
  await expect(page.getByText('My Timesheets', { exact: false }).first()).toBeVisible();
}
