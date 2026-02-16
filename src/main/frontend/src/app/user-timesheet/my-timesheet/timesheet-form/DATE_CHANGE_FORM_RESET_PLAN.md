# Date Change vs Form State – Analysis & Plan

## 1. Problem

- Projects are **date-aware**: they are fetched via `getProjectListForDateAndEmpId(fromDate, empId)`. So the list is valid **only for the selected date**.
- **Scenario:** User selects **date-1** → projects for date-1 load (e.g. A, B). User fills locations/projects/activities but **does not submit**. Then user changes to **date-2**.
- **Current behavior:** `onFromDateChange()` only:
  - Recalculates `totalWorkingHours`
  - Calls `getProjectListForDateAndEmpId()` so **activeProjectList** is replaced with projects for **date-2**.
- **What is not done:** Existing **locations, project selections, and activities** are **not** cleared. So the form can still contain:
  - Locations with **Project A** (valid for date-1)
  - While **activeProjectList** is now for date-2 (e.g. B, C only).
- **Result:** Mismatch and validation risk: project chosen for date-1 might not be valid for date-2 → backend can reject or data can be inconsistent.

---

## 2. Should We Reset Form When User Changes Date?

**Yes.** We should clear **date-dependent** form state when the user changes the date.

**Reasons:**

1. **Projects are date-scoped** – The project list is fetched for a specific date. Selections made for date-1 are not guaranteed (and often not) valid for date-2.
2. **Backend validates by date** – Submit uses the **current** `fromDate`. If we keep old project selections, backend may reject (e.g. “project not assigned on this date”) or create inconsistent data.
3. **Single source of truth** – After date change, the only valid project list is the one for the **new** date. Keeping old locations/projects would mix two different “dates” in one form.
4. **Predictable UX** – “Change date → form reflects the new date only” is clear and avoids silent validation errors on submit.

So: **on date change we must clear at least locations (and their projects/activities) and re-fetch projects for the new date.** Optionally we can also clear in/out times and presence so the form is a clean slate for the new date.

---

## 3. What to Reset on Date Change

We do **not** do a full `resetForm()` (that would clear dayType and fromDate too; we are *reacting* to fromDate change).

Clear only **date-dependent** state and keep **date + day type** (user already chose the new date and day type).

| State | Clear? | Reason |
|--------|--------|--------|
| `timesheetLocations` (and their projects/activities) | **Yes** | Projects are date-specific; old selections can be invalid for new date. |
| `documentData`, `selectedFile`, `uniqueProjectsList` | **Yes** | Tied to projects/locations. |
| `apmosysInTime`, `apmosysOutTime`, `totalPresence` | **Yes** | Avoid “presence with no locations”; clean slate for new date. |
| `empHasClientSideId` | **Yes** | Depends on project selection. |
| `dayType`, `fromDate`, `toDate` | **No** | User just set the new date (and toDate for night shift). |
| `activeProjectList` | **Re-fetch** | Replaced by `getProjectListForDateAndEmpId()` for new date. |
| Highlight/expand UI state | **Yes** | Related to locations/projects. |

---

## 4. Implementation Plan

### 4.1 Add `resetDateDependentFormState()`

- Clear:
  - `timesheetLocations` then call `addLocation(null)` (one empty location).
  - `documentData`, `selectedFile`, `uniqueProjectsList`.
  - `apmosysInTime`, `apmosysOutTime`, `totalPresence`.
  - `empHasClientSideId` (e.g. set to `false`).
  - `cleanupDocumentData()` and location-related UI state (`highlightLocationList`, `highlightLocationIdSet`, `expandedLocationIndex`, `expandedProjectIndexMap`).
- Do **not** clear: `dayType`, `fromDate`, `toDate`, `timesheetAppliedFor`, `timesheetFilledForUser`, `currentUser`, etc.

### 4.2 Use it in `onFromDateChange()`

1. If `!this.fromDate` → keep current behavior (totalPresence = 0, return).
2. Night shift: compute and set `toDate` as now.
3. **Call `resetDateDependentFormState()`** so all date-dependent entries are cleared.
4. `calculateTotalWorkingHours()` (will use new date; presence will be 0 until user sets in/out).
5. Call `getProjectListForDateAndEmpId().catch(...)` so dropdown and “add” state are correct for the new date.

### 4.3 Optional UX: Short message

- After reset, optionally show a brief message:  
  **“Date changed. Form entries have been cleared for the new date.”**  
  So the user understands why their previous locations/projects disappeared.

---

## 5. Summary

| Question | Answer |
|----------|--------|
| Should we reset form when user changes date? | **Yes** – at least date-dependent parts (locations, projects, activities, documents, in/out times, presence). |
| How to handle it? | Add `resetDateDependentFormState()`, call it from `onFromDateChange()`, then re-fetch projects for the new date; optionally show a one-line message that entries were cleared for the new date. |

This keeps validation correct and avoids submitting project/location data that is not valid for the selected date.
