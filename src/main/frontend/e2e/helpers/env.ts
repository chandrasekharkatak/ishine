/**
 * E2E environment helpers — credentials never hardcoded; use .env.e2e or shell exports.
 */

export function hasE2eCredentials(): boolean {
  return Boolean(process.env.E2E_USERNAME?.trim() && process.env.E2E_PASSWORD?.trim());
}

export function missingE2eCredentialVars(): string[] {
  const missing: string[] = [];
  if (!process.env.E2E_USERNAME?.trim()) missing.push('E2E_USERNAME');
  if (!process.env.E2E_PASSWORD?.trim()) missing.push('E2E_PASSWORD');
  return missing;
}

export function getE2eOtp(): string | undefined {
  const v = process.env.E2E_OTP?.trim();
  return v || undefined;
}

export function allowTimesheetSubmit(): boolean {
  return process.env.E2E_SUBMIT_TIMESHEET === '1' || process.env.E2E_SUBMIT_TIMESHEET === 'true';
}

export function projectSearchText(): string | undefined {
  const t = process.env.E2E_PROJECT_SEARCH_TEXT?.trim();
  return t || undefined;
}
