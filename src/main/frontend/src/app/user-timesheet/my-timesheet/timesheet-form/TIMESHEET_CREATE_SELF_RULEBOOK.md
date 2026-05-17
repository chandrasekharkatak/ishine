# Timesheet Create (Self) – Rule Book

Quick reference for **what**, **why**, **constraints**, and **behavior** of the self timesheet creation flow.

---

## 1. What

- **Scope:** User creates a **new** timesheet **for themselves** (or, when allowed, for another employee as “Applied for”).
- **Entry point:** Create mode of the timesheet form (e.g. “Timesheet Entry”).
- **Output:** One timesheet record (date range, day type, location(s), project(s), activities, optional document).

---

## 2. Why (Business Purpose)

- Record **work date**, **day type**, **location(s)**, **project(s)** and **activity hours** for payroll / billing / compliance.
- Support **shadow** or **team** timesheet: “Applied for” can be another employee; projects and team members are then driven by **that** employee and the **selected date** (date-aware APIs).

---

## 3. Constraints (Rules)

### 3.1 Date

- **Source of truth:** Server date (`getServerDate`). All “today” and allowed range are based on server date, not browser date.
- **Selectable range:**
  - **Max date** = server date (cannot create for future).
  - **Min date** = server date − (backdated days or lock days), depending on config:
    - If **timesheet lock check disabled:** `serverDate − (timesheetBackDatedDays + 1)` days (e.g. 30 backdated → 31 days back).
    - If **timesheet lock check enabled:** `serverDate − (timesheetLockDays + 1)` days.
  - Backdated days may be capped by **date of joining** (e.g. cannot go before DOJ).
- **Disabled dates:** Dates that already have a timesheet (from `availableTimesheets`) are disabled so user cannot create a duplicate for the same day; in update mode, current timesheet’s date is excluded from this list.

### 3.2 Day Type

- **Fillable:** User can enter in/out time, locations, projects, activities (e.g. Working Day, etc.).
- **Non-fillable:** Public Holiday (4), Client Holiday (6), Week Off (7).  
  - **Rule:** No in/out time, no location/project/activity breakdown; only **one default location (type 4 – NA)** and **description** at project level.  
  - **Behavior:** On switching to a non-fillable day type, form is cleared and re-initialized for that day type (single NA location, projects from active list, only description editable).

### 3.3 Location & Project

- **Max locations:** **5** per timesheet. Add button disabled when `timesheetLocations.length >= 5`.
- **Add location:** Allowed only when date and time (from/to) are set; otherwise “Complete date and time first.”
- **Add project:** Allowed per location; disabled when only one project is available and already added.
- **Projects per date:** Fetched via **date-aware** API so only projects **active on the selected date** are shown (e.g. `getProjectListForDateAndEmpId` with `fromDate` and correct `empId` from “Applied for”).

### 3.4 “Applied For” (Self vs Shadow / Team)

- **Self:** `timesheetAppliedFor` = current user → projects and team list use **current user’s** `empId` and **selected date**.
- **Shadow / Team:** `timesheetAppliedFor` = another employee → projects and team list use **that employee’s** `empId` and **selected date** (date-aware APIs).

### 3.5 Team Member List (Dropdown)

- Used when creating for someone else (e.g. team timesheet).
- **API:** `getAllTeamMemberViewForTimesheet(empId, date)` (POST with `empId` and `date`).
- **Date-aware:** Only employees who have an **active `employee_team_mapping`** on the selected date are included.

### 3.6 Employee List by Project

- When selecting a project, employee dropdown (if used) is filled with employees mapped to **that project**.
- **API:** `getEmployeeListByProjectId(projectId, currentUserEmpId, date)`.
- **Date-aware:** Optional `date` parameter; when sent, only employees who were mapped to the project on that date (e.g. onboarded on or before that date) are returned.

### 3.7 Validation (Submit)

- Date range and day type set.
- At least one location; per location, at least one project; per project, activity hours (and any mandatory fields) valid.
- Project activity hours vs total presence (e.g. sum of project hours ≤ total presence or as per product rule).
- Non-fillable: only description at project level; no in/out time.

---

## 4. Init Order (Create Mode)

1. **Server date first:** `loadServerDateThenInitCreate()` → `getServerDate()`.
2. **Then init create:** On server date response, call `onTimesheetAppliedForChange()`.
3. **Inside `onTimesheetAppliedForChange()`:**  
   - `resetForm()`.  
   - Then load metadata / team list / projects, etc., so that **date constraints** (min/max, disabled dates) and **Applied for**–dependent data use server date and selected user.

**Why:** Prevents race where date picker or project list were built before server date was available.

---

## 5. Lifecycle & Cleanup

- **Subscriptions:** All HTTP/subscriptions in the form use `takeUntil(this.destroy$)` and, where appropriate, `finalize()` so loading flags are cleared and no subscription is left after destroy.
- **Reset:** `resetForm()` is called from `onTimesheetAppliedForChange()` only; not called again from `getAllTeamMemberList()` to avoid double reset.
- **Document URLs:** Any `URL.createObjectURL` for preview is revoked on close/destroy to avoid memory leaks.

---

## 6. APIs (Backend) – Date-Aware Where Used

| API | Purpose | Date-aware |
|-----|---------|------------|
| `getServerDate` | Current date for range & validation | N/A |
| `getProjectListForDateAndEmpId` | Projects for selected date & emp (Applied for) | Yes (date + empId) |
| `getEmployeeListByProjectId` | Employees for a project (e.g. shadow) | Yes (optional `date`) |
| `getAllTeamMemberViewForTimesheet` | Team members for dropdown (team timesheet) | Yes (empId + date); filters by `employee_team_mapping` date window |

---

## 7. Constants (Frontend)

- **Non-fillable day types:** `[4, 6, 7]` (Public Holiday, Client Holiday, Week Off).
- **Max locations:** `5`.
- **Default location for non-fillable:** Location type `4` (NA), single location, projects from active list; only description editable.

---

## 8. Summary Table

| Item | Rule |
|------|------|
| Date range | Server date − (backdated/lock days) to server date; DOJ can cap backdated. |
| Duplicate date | Dates with existing timesheet disabled in picker. |
| Day type non-fillable | Clear form; single NA location; only description at project level. |
| Max locations | 5. |
| Projects | Date-aware; only active on selected date for Applied-for emp. |
| Team list | Date-aware; only emp with active team mapping on selected date. |
| Employee by project | Date-aware when `date` param sent. |
| Create init | Server date → then `onTimesheetAppliedForChange()` (reset + metadata/team/projects). |
| Subscriptions | `takeUntil(destroy$)`, `finalize()` where needed. |

---

## 9. Validation Matrix (Frontend & Backend)

### 9.1 Frontend – `TimesheetValidationService` (`timesheet-validation.service.ts`)

- **Basic timesheet fields**
  - **Employee selection**: `timesheetFilledForUser.empId` is required.
  - **Team timesheet self-selection**: when `timesheetAppliedFor === 'TEAM'`, `currentUser.empId` **must not** equal `timesheetFilledForUser.empId` (cannot select yourself for team timesheet).
  - **Day type**: `dayType` cannot be null.
  - **Date / From Date**:
    - Required.
    - Cannot be a **future date** (compared to current client date; backend also checks against server date/lock rules).
  - **Night shift** (only for fillable day types):
    - If `isNightShift === true` and day type is fillable, `toDate` is required.
    - For night shift, `toDate` must be **after** `fromDate`.
    - Night shift is **not allowed** for non-fillable day types.
- **Times & presence (ApMoSys in/out)**
  - For **fillable** day types:
    - `apmosysInTime` required.
    - `apmosysOutTime` required.
    - `totalPresence` must be **> 0**.
  - For **non-fillable** day types:
    - `apmosysInTime` **must be empty**.
    - `apmosysOutTime` **must be empty**.
    - `totalPresence` must be **0**.

- **Locations & projects**
  - At least **one location** in `timesheetLocations` is required.
  - For each location:
    - If day type is fillable:
      - **Location In time** (if provided) must be **≥ Work Check-In**:
        - `validateLocationInTime(locationInTime, fromDate, apmosysInTime)` must be true.
      - **Location Out time** (if provided) must be **≤ Work Check-Out** (handles night shift with `toDate`):
        - `validateLocationOutTime(locationOutTime, fromDate, apmosysOutTime, isNightShift, toDate)` must be true.
    - Must have at least **one project** for the location.
    - For each project:
      - **Project selection**: `project.projectId` required.
      - **Client**: `project.clientId` required.
      - **Client location**: `project.clientLocationId` required.
      - **Client DSR approval status**:
        - If `project.clientSideId` is present and day type is fillable, `project.clientApprovalStatus` is required.
      - **Activities (fillable day types)**:
        - At least **one activity** per project.
        - For each activity:
          - `teamId` required.
          - `activityId` required.
          - `durationMinutes` must be **> 0**.
      - **Description (non-fillable day types)**:
        - For non-fillable day types, `project.description` is required (per project).

- **Hours consistency**
  - For each location:
    - Sum of all project activity minutes for that location (`projectHoursForLocation`) must be **≤** `location.totalWorkingHours`.
  - Across all locations:
    - Sum of `location.totalWorkingHours` over all locations must be **≤** `totalPresence`.

- **Document uploads**
  - Validation applies only when:
    - `empHasClientSideId === true`,
    - day type is **fillable**, and
    - `documentData` and `uniqueProjectsList` are present.
  - For each project in `uniqueProjectsList`:
    - Only when `clientApprovalStatus` is **Pending (1)** or **Approved (2)**:
      - **Filled Attendance Proof** (`docType === 'Filled'`) must exist in `documentData` for that project, with no `fileError`.
    - Additionally, when `clientApprovalStatus === 2` (Approved):
      - **Approved Attendance Proof** (`docType === 'Approved'`) must exist for that project, with no `fileError`.

> Frontend validation is used to give **immediate feedback** and to avoid unnecessary backend calls, but must be considered **advisory** – backend remains the final gatekeeper.

### 9.2 Backend – `TimesheetValidatorService` (`TimesheetValidatorService.java`)

- **Authorization**
  - **validateEmployeeAuthorization**:
    - If `timesheetDTO.empId != timesheetDTO.createdBy`, creator must be authorized:
      - `createdBy` must have the target `empId` in their **team list** (`getAllTeamMemberView(createdBy)`).
    - Otherwise an `UnauthorizedAccessException` is thrown (“Employee not authorized to perform this action”).

- **Timesheet date**
  - **validateTimesheetDateNotInFuture(LocalDate timesheetDate)**:
    - Rejects dates **after today** (“Timesheet date cannot be in the future.”).
  - **validateTimesheetLockPeriod(empId, timesheetDate)**:
    - If `employeeRepository.getIsLockEnabled(empId)` is `"true"` and `timesheetDate < today − 1`, returns fail response:
      - `"Your timesheet is locked"`.

- **Client-side documents (project-level)**
  - **validateClientSideDocuments(TimesheetDTO, doc1, doc2)**:
    - **Skip** when day type is:
      - Public Holiday, Week Off, Leave, or Client Holiday.
    - **Skip** when `shadowFor == 'Self'` (pure self timesheet, not shadow).
    - Otherwise, if project’s client-side ID is mandatory (`projectRepository.getClientSideIdMandatory(projectId)`):
      - At least one of `doc1` or `doc2` must be present; if both missing, throw:
        - `"Client-side ID is mandatory, please upload required documents."`

- **Office time (ApMoSys in/out)**
  - **validateOfficeTime(officeInTime, officeOutTime, currentTime)**:
    - `officeInTime` cannot be **after** `currentTime` (“Office In Time cannot be greater than current time.”).
    - `officeOutTime` cannot be **after** `currentTime` (“Office Out Time cannot be greater than current time.”).
    - `officeInTime` cannot be **after** `officeOutTime` (“Office Out Time cannot be less than Office In Time.”).

- **Client time (client in/out)**
  - **validateClientTime(clientInTime, clientOutTime, currentTime)**:
    - If either time is `null` → fail response:
      - `"Client in time or out time is not provided"`.
    - If `clientInTime > clientOutTime` → `IllegalArgumentException`:
      - `"Client Out Time cannot be less than Client In Time."`
    - If either in/out is **after current time** → fail response:
      - `"Client in time or out time cannot be greater than current date/time"`.

- **Client approval + documents**
  - **validateClientApprovalStatus(clientApprovalStatus)**:
    - If `clientApprovalStatus == null` → fail:
      - `"Client approval status is null"`.
  - **validateDocumentsForApprovalStatus(TimesheetDTO, doc1, doc2)**:
    - If `clientApprovalStatus` is `null` → delegates to `validateClientApprovalStatus(null)`.
    - If status is `"pending"` or `"approved"`:
      - `doc1` (filled timesheet document) required; else fail:
        - `"In case of pending/approved the filled timesheet document is missing"`.
    - If status is `"approved"`:
      - `doc2` (approval proof) also required; else fail:
        - `"In case of approved the approval document proof is missing"`.

- **Activities list**
  - **isActivitiesListEmpty(List<ActivityDTO> activities)**:
    - Returns `true` when activities list is empty for working days; typically used to trigger **warning** or additional checks.

> Backend validation is the **source of truth** – any data that passes frontend checks but violates these rules will be rejected here.

---

This is the rule book for **timesheet create self** (what, why, constraints, init order, APIs, lifecycle, and full validation rules on frontend and backend). Use it for implementation, testing, and onboarding.
