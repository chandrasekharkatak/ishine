import type { Page } from '@playwright/test';

/** Primary action on create flow */
export function createTimesheetSubmitButton(page: Page) {
  return page.locator('.btn-create-timesheet');
}

/** Primary action on edit flow */
export function updateTimesheetSubmitButton(page: Page) {
  return page.locator('.btn-update-timesheet');
}

/** First visible Edit control in timesheet table (title="Edit") */
export function firstEditTimesheetButton(page: Page) {
  return page.locator('button.ts-action-btn--edit[title="Edit"]').first();
}
