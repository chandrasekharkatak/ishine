# Phase 1 - Scenario Traceability Matrix (Timesheet)

Source scenarios are from `src/app/user-timesheet/my-timesheet/timesheet-form/TIMESHEET_TEST_CASES_SCENARIOS.md`.

## Legend

- `Automated`: implemented and active in Playwright.
- `Automated (guarded)`: implemented, but needs specific data/permission/env and may `skip`.
- `Planned`: prioritized for next implementation.
- `Blocked`: needs backend seed/API hooks/special test user setup.

## P0 / Critical Scenarios (Phase 2 target)

| ID | Source | Scenario | Priority | Status | Spec |
|---|---|---|---|---|---|
| TS-AUTH-001 | Login | Login page renders fields and button | P0 | Automated | `01-login.spec.ts` |
| TS-AUTH-002 | Login | Empty login blocked | P0 | Automated | `01-login.spec.ts` |
| TS-AUTH-003 | Login | Invalid credentials stay on login | P0 | Automated | `01-login.spec.ts` |
| TS-AUTH-004 | Login | Valid login routes to home | P0 | Automated (guarded) | `01-login.spec.ts` |
| TS-ROUTE-001 | Route | Direct `/user-timesheet/my-timesheet` route loads | P0 | Automated (guarded) | `02-timesheet-navigation.spec.ts` |
| TS-ROUTE-002 | Route/AuthGuard | Unauthenticated timesheet route redirects/blocks | P0 | Automated | `07-timesheet-edge-cases.spec.ts` |
| TS-UI-001 | 3.1 | Create tab and form container visible | P0 | Automated (guarded) | `03-timesheet-create-ui.spec.ts` |
| TS-UI-002 | 1.x | Self is default timesheet application mode | P0 | Automated (guarded) | `03-timesheet-create-ui.spec.ts` |
| TS-VAL-001 | 3.1 | Empty create submit shows validation alert | P0 | Automated (guarded) | `05-timesheet-validation.spec.ts` |
| TS-LIST-001 | Read | My Timesheets list renders table/empty state | P0 | Automated (guarded) | `04-timesheet-read-list.spec.ts` |
| TS-UPD-001 | 3.9 | Edit action opens update form | P0 | Automated (guarded) | `06-timesheet-update-ui.spec.ts` |
| TS-UPD-002 | 3.15 | Update button visible in edit mode | P0 | Automated (guarded) | `06-timesheet-update-ui.spec.ts` |

## High-value scenarios now structured (next immediate coding pass)

| ID | Source | Scenario | Priority | Status | Notes |
|---|---|---|---|---|---|
| TS-DAY-001 | 7 | Day type control lists fillable + non-fillable types | P1 | Planned | Assert options exist in Material dropdown |
| TS-DAY-002 | 12.3 | Day-type transition resets/rehydrates form sections | P1 | Planned | Needs deterministic date selection |
| TS-TEAM-001 | 2.x | Switch self -> team shows Team Member dropdown | P1 | Planned | Requires user with team members |
| TS-TEAM-002 | 12.2 | Applied-for change resets dependent fields | P1 | Planned | Guard by permissions/data |
| TS-DOC-001 | 1.4 | Client-side project with missing docs blocks create | P1 | Planned | Needs seeded project requiring docs |
| TS-DOC-002 | 1.7/6.4 | Preview call path works for filled/approved docs | P1 | Planned | Needs seeded docs + network assertion |
| TS-HL-001 | 5.1 | Half-day leave date rejects full working day type | P1 | Blocked | Needs leave-seeded account/date |
| TS-HL-002 | 5.4 | Full-day leave blocks timesheet creation | P1 | Blocked | Needs leave-seeded account/date |
| TS-LOCK-001 | 8.2 | Locked date rejects create/update | P1 | Blocked | Needs lock-window controlled date |
| TS-AUTO-001 | 4.1 | Autofill populates from last working day | P1 | Blocked | Needs deterministic historical dataset |

## Cross-cutting combinations (phased)

| Combination Bucket | Scenarios | Status |
|---|---|---|
| Self + client-side + working | 1.1, 1.2, 1.3, 3.1 | Planned |
| Self + non-client-side + working | 1.11 | Planned |
| Team + client-side + working | 2.1, 2.2, 2.3 | Planned |
| Shadow permutations | 1.14, 1.16, 2.7, 2.8 | Planned |
| Update transitions | 3.9 to 3.19 | Planned |

## Data contracts needed for full automation

- User A: self create/update permissions + at least one editable row.
- User B: team timesheet permissions + team member list.
- User C: seeded half-day leave date + full-day leave date.
- Project fixtures: one client-side-ID project, one non-client-side-ID project.
- Optional API seed/cleanup hooks for deterministic create/update assertions.
