# Timesheet E2E (Playwright)

End-to-end tests for **login** and **User → My Timesheet** (`/user-timesheet/my-timesheet`). Selectors use **existing** UI (IDs, roles, classes) — no `data-testid` in Angular templates.

## Prerequisites

1. **Angular**: `npm start` → `http://localhost:4200` (or set `E2E_BASE_URL`).
2. **Backend API** reachable from the browser (same as normal dev use).
3. **Credentials**: a non-production test user. Do **not** commit secrets.

## Setup

```bash
cd src/main/frontend
npm install
npm run e2e:install
```

Copy `e2e/.env.example` to **`frontend/.env.e2e`** (repo root of this Angular app, **gitignored**) and fill values:

| Variable | Purpose |
|----------|---------|
| `E2E_BASE_URL` | App origin (default `http://localhost:4200`) |
| `E2E_USERNAME` | Test login |
| `E2E_PASSWORD` | Test password |
| `E2E_OTP` | If OTP appears after password |
| `E2E_SUBMIT_TIMESHEET` | Set `1` to enable optional destructive “full submit” spec |
| `E2E_PROJECT_SEARCH_TEXT` | Project name substring for optional submit flow |

Alternatively, export variables in your shell before running tests.

## Run

```bash
npm run e2e              # headless
npm run e2e:headed       # see the browser
npm run e2e:ui           # Playwright UI mode
npm run e2e:debug        # step debugger
```

HTML report: `playwright-report/` after a run.

## Phased delivery

- Phase 1 traceability matrix: `e2e/PHASE1_SCENARIO_MATRIX.md`
- Phase 2 critical automation: `e2e/specs/08-critical-p0.spec.ts` (scenario IDs prefixed `TS-*`)

## CI

- Provide secrets via CI **masked variables**, not the repo.
- Install browsers: `npx playwright install --with-deps chromium`
- Example: `E2E_BASE_URL`, `E2E_USERNAME`, `E2E_PASSWORD`, `E2E_OTP` (if needed).

## Spec map

| File | Scope |
|------|--------|
| `01-login.spec.ts` | Login form, invalid login, optional success |
| `02-timesheet-navigation.spec.ts` | Route + tab switching |
| `03-timesheet-create-ui.spec.ts` | Create form visible, Self default |
| `04-timesheet-read-list.spec.ts` | My Timesheets list / empty |
| `05-timesheet-validation.spec.ts` | Empty submit → alert modal |
| `06-timesheet-update-ui.spec.ts` | Edit row → update button |
| `07-timesheet-edge-cases.spec.ts` | Unauthenticated route, reload, opt-in submit |
| `08-critical-p0.spec.ts` | Consolidated P0 critical scenarios with IDs |

Tests **skip** when permissions or data are missing (e.g. no “Create Timesheet”, no rows to edit).

## Extending

- Add flows under `e2e/specs/` using helpers in `e2e/helpers/`.
- For mat-select / date pickers, prefer `getByRole`, labels, or stable text; avoid flaky `nth()` unless necessary.
