# Non-Fillable Day Type – Multi-Project: Plan & Rationale

## 1. Current State (Brief)

- **Non-fillable day types:** e.g. Public Holiday (4), Client Holiday (6), Week Off (7), Leave (2), etc. – no work hours, no in/out time.
- **One location:** Single “NA” location (default work location type). User cannot add more locations.
- **Multiple projects:** Already supported. Under that one location, user can add multiple projects (“Add Project” enabled when `activeProjectList.length > 1`).
- **Per project we collect:** Project, Client, Client Location (all required), and **Description** (required for non-fillable). No activities, no hours, no documents.
- **Payload:** `workCheckIn` / `workCheckOut` and location in/out times are `null`; `project.activities = null`.

So multi-project for non-fillable is already in place; the open points are **what to collect**, **how to present it**, and **what to automate**.

---

## 2. How to Manage Multi-Project for Non-Fillable

### 2.1 Keep One Location, Multiple Projects (Recommended)

- **Structure:** 1 location (NA) → N projects → per project: selection (Project, Client, Client Location) + description.
- **Why:**
  - Matches current backend and validation (location sessions with projects; no activities for non-fillable).
  - No work location meaning on leave/holiday; “NA” is a single logical place; splitting by “location” adds no value.
  - Reusing the same location/project model keeps code and APIs simple.

### 2.2 Optional: “Single project per day” Policy

- **If business rule is:** “For leave/holiday/week-off, only one project per day.”
- **Then:** Hide “Add Project” for non-fillable (or show it disabled with tooltip: “Only one project allowed for this day type”). Keep one project row; still collect Project, Client, Client Location, Description.
- **Why:** Simplifies approval and reporting if the policy is “one project per non-working day.”

### 2.3 Optional: “One row per project” Without Location Wording

- **UI only:** For non-fillable, you can show “Project 1, Project 2, …” without emphasizing “Location: NA” (e.g. collapse or de-emphasize the single location card).
- **Data model:** Unchanged (still 1 location, N projects). This is a presentation improvement, not a structural change.

**Recommendation:** Keep current model (1 location, N projects). Only restrict to single project or tweak UI if product/business asks for it.

---

## 3. What Data to Collect from the User (Non-Fillable)

### 3.1 Must Collect (Keep as Today)

| Data | Why |
|------|-----|
| **Date / From Date (and To Date if night shift)** | Identifies the day(s); required for any timesheet. |
| **Day type** | Distinguishes leave vs holiday vs week-off; needed for reporting and rules. |
| **At least one project** | Backend and validation expect at least one project per location. |
| **Project** (projectId) | Which project the non-working day is attributed to (billing/cost allocation, visibility). |
| **Client** (clientId) | Required for project; keeps data consistent with fillable flow. |
| **Client Location** (clientLocationId) | Required for project; same reason. |
| **Description** (per project) | Explains reason/context for the day (e.g. “Annual leave”, “Company holiday”). Validation already requires it for non-fillable. |

So: **date, day type, and per project: Project + Client + Client Location + Description** is the minimal set and should stay.

### 3.2 Optional to Collect (Only If Product Needs It)

| Data | When to add | Why |
|------|-------------|-----|
| **Leave type / sub-type** | If day type is “Leave” and you need to distinguish (e.g. sick, casual, annual). | Leave balance, policy, approval rules. |
| **Approver / comments** | If non-fillable entries need approval or audit trail. | Workflow and compliance. |
| **Attachment** (e.g. medical certificate) | For specific leave types. | Policy and audit. |

Recommendation: **Don’t add these unless product explicitly requires them.** Keeps non-fillable flow simple.

### 3.3 What We Should Not Collect for Non-Fillable

- **Work check-in / check-out:** Not applicable; already null.
- **Location in/out time:** Not applicable; already null.
- **Activities / hours:** No work; already null.
- **Documents (Filled/Approved attendance proof):** Not applicable; already hidden for non-fillable.

No change needed here.

---

## 4. What Can Be Automated

### 4.1 High-Value, Low-Risk (Recommend Doing)

| Automation | What | Why |
|------------|------|-----|
| **Default single project when only one** | If `activeProjectList.length === 1`, auto-select that project (and cascade Client / Client Location if only one option). | Already partially there (`autoSelectProjectIfSingle`). Reduces clicks when user has only one project. |
| **Default Client / Client Location when only one** | When project is selected, if that project has only one client and one client location, auto-fill them. | Same as fillable; less repetitive input for single-client projects. |
| **Pre-fill description by day type** | When day type is e.g. “Public Holiday”, pre-fill description “Public Holiday” (editable). | User can keep or change; saves typing and keeps text consistent. |

### 4.2 Medium-Value (Consider If You Want Smoother UX)

| Automation | What | Why |
|------------|------|-----|
| **Single project when only one** | When only one project is available, don’t show “Add Project” and show a single project row (no “Project 2” possibility). | Clearer UX: user sees one project and one description. |
| **Remember last used project (or most recent)** | For non-fillable, pre-select the project the user last used (or most recently used for this emp). | Speeds up repeated entries (e.g. same leave project). Requires last-used or recent-project API or local state. |

### 4.3 Do Not Automate (Keep User in Control)

| Don’t automate | Why |
|----------------|-----|
| **Auto-add multiple projects** | Which projects to attribute to a non-working day is a user decision (e.g. split between two projects for reporting). |
| **Auto-description from leave type** | Can be a hint/pre-fill (see above), but final text should be editable for clarity and exceptions. |
| **Changing day type from backend** | Day type should remain an explicit user (or approval) choice. |

---

## 5. Summary Table

| Topic | Recommendation | Reason |
|-------|----------------|--------|
| **Multi-project structure** | Keep 1 location (NA) + N projects. | Aligns with backend and validation; no need for multiple “locations” on non-working days. |
| **Data to collect** | Date, day type; per project: Project, Client, Client Location, Description. | Minimal required set; optional extras only if product needs them. |
| **Automate** | (1) Auto-select single project when only one; (2) Auto Client/Client Location when only one option; (3) Pre-fill description from day type (editable). | Less effort, consistent data, no loss of control. |
| **Optional UX** | Single-project-only mode for non-fillable if business rule is “one project per day”; de-emphasize “Location: NA” in UI. | Simpler mental model if policy is one project per non-working day. |

---

## 6. Implementation Order (If You Implement the Plan)

1. **Keep as-is:** 1 location (NA), N projects, current validation and payload.
2. **Automate:** Ensure single-project auto-select and, where applicable, single Client/Client Location auto-fill for non-fillable (reuse fillable logic).
3. **Pre-fill description:** When day type changes to non-fillable, set a default description from day type name (e.g. “Public Holiday”), editable.
4. **Optional:** If product confirms “one project per non-fillable day”, disable “Add Project” for non-fillable and optionally simplify UI (e.g. single project card without “Location” emphasis).

This keeps the new timesheet flow consistent, minimizes required data, and improves UX through safe, predictable automation.
