# Document Cleanup on Timesheet Update – Implementation Checklist

This checklist covers the **four scenarios** where document data (DB + server files) must be cleaned during **update timesheet**. Use it as a step-by-step guide; no code is written here.

---

## 1. Scenario 1: Day type changed to non‑working

**Condition:** New day type is non‑working (e.g. Week Off, Holiday, Leave). Any previous working-day documents for this timesheet must be removed.

| Item | Detail |
|------|--------|
| **Class** | `TimesheetServiceNew` |
| **Method** | `updateTimesheet(Long timesheetId, EmployeeTimesheetDTO newEmpDTO, List<MultipartFile> documents)` |
| **Call site** | Immediately after `DayTypeTransition transition = timesheetValidationHelper.resolveDayTypeTransition(empTS, newEmpDTO);` (around line 941). Add a **new block**: if `transition == DayTypeTransition.WORKING_TO_NON_WORKING` then call document cleanup, then skip structure cleanup and document upload (and optionally skip working-day validations for the rest of this request). |deleteFile
| **Action** | Call **existing** `timesheetDocumentService.deleteByTimesheetId(timesheetId)`. |
| **Signature (existing)** | `TimesheetDocumentServiceNew.deleteByTimesheetId(Long timesheetId)` |
| **Notes** | After cleanup, do **not** run `cleanTimesheetStructure` for locations/projects if your product behaviour for non‑working is to clear them (or run it but then skip document upload). Do **not** call `handleDocumentUploadsFromNewContract` for this request. If `deleteByTimesheetId` currently deletes `FinalDocumentNew` and files, adjust it per the **Shared reference rule** below (only delete final docs/files when no other rows reference them, or only unlink by setting `bulkApprovedDocId` to null). |

---

## 2. Scenario 2: Location deleted during update

**Condition:** A location session is removed (present in DB but not in incoming `locationSessions`). All documents for projects under that location must be removed.

| Item | Detail |
|------|--------|
| **Class** | `TimesheetStructureCleanupService` |
| **Method** | `deleteLocationCascade(Long timesheetId, Long locationMappingId)` (private) |
| **Call site** | At the **start** of `deleteLocationCascade`, before the loop over projects. Get the list of projects for this location (you already have `projectTimesheetService.findByLocationMappingId(locationMappingId)` in the next line). For **each** such project, call document cleanup for that `(timesheetId, projectId)`. Then keep the existing loop that calls `deleteProjectCascade` and finally `locationRepo.deleteById`. |
| **Action** | For each `ProjectTimesheetDTO project` in `projects`, call **new** `timesheetDocumentService.deleteByTimesheetIdAndProjectId(timesheetId, project.getProjectId())`. |
| **Dependency** | `TimesheetStructureCleanupService` must **inject** `TimesheetDocumentServiceNew` and call the new method. Method to add: see Section “New methods to add” below. |

---

## 3. Scenario 3: Project removed during update

**Condition:** A project is removed from a location (present in DB for that location but not in incoming `projects`). All documents for that project on this timesheet must be removed.

| Item | Detail |
|------|--------|
| **Class** | `TimesheetStructureCleanupService` |
| **Method** | `deleteProjectCascade(Long timesheetId, Long locationMappingId, Integer projectId)` (private) |
| **Call site** | At the **start** of `deleteProjectCascade`, right after the approved check (or immediately after it). Call document cleanup for this `(timesheetId, projectId)`, then keep existing calls: `timesheetRejectionDetailsNewRepository.deleteRow`, `activityTimesheetService.deleteActivitiesForProject`, `projectTimesheetService.deleteByTimesheetIdAndLocationMappingIdAndProjectId`. |
| **Action** | Call **new** `timesheetDocumentService.deleteByTimesheetIdAndProjectId(timesheetId, projectId)`. |
| **Dependency** | Same new method and injection as Scenario 2. |

---

## 4. Scenario 4: Project status changed from Approved to Filled

**Condition:** For a **kept** project, client approval status changes from Approved (e.g. 2) to Pending/Filled (e.g. 1). The approved (final) document for that project must be removed.

| Item | Detail |
|------|--------|
| **Class** | `TimesheetServiceNew` |
| **Method** | `handleProjectsUnderLocationMapping(Long timesheetId, Long locationMappingId, List<ProjectTimesheetDTO> incomingProjects, Long createdBy)` |
| **Call site** | In the **UPDATE** branch (when `existingProject != null`), **before** `projectTimesheetService.update(projectDTO)`: capture `existingProject.getClientApprovalStatus()` (or equivalent) as `oldStatus`. **After** `projectTimesheetService.update(projectDTO)`: if `oldStatus` was Approved (e.g. `Integer.valueOf(2)`) and `projectDTO.getClientApprovalStatus()` is now Filled/Pending (e.g. 1), call **new** `timesheetDocumentService.deleteApprovedDocumentsByTimesheetIdAndProjectId(timesheetId, projectDTO.getProjectId())`. |
| **Action** | Call **new** `timesheetDocumentService.deleteApprovedDocumentsByTimesheetIdAndProjectId(timesheetId, projectId)`. |
| **Dependency** | New method in `TimesheetDocumentServiceNew`: see below. Use your app’s constants for Approved (2) vs Pending (1) if any. |

---

## Shared reference rule (important)

**Do not delete `FinalDocumentNew` rows or physical files** when cleaning documents for a single timesheet/project. The same final document (`bulkApprovedDocId`) can be referenced by other rows (e.g. other users/timesheets in bulk approval). Deleting it would break those references.

**Correct approach:** Only **set the corresponding reference to null** and **update the flag** on the rows we are cleaning. On `TimesheetDocumentDetailsNew`: set **`bulkApprovedDocId = null`**, **`finalFlag = false`**, and **`clientApprovalStatusId = 1`** (Pending). Do **not** delete any `FinalDocumentNew` row or the physical file.

- **Scenario 1 (full timesheet cleanup):** Existing `deleteByTimesheetId` may still delete FinalDocumentNew and files **only** when they are not referenced by any other timesheet (e.g. after nulling or deleting all TimesheetDocumentDetailsNew for this timesheet, delete final docs that have no remaining referrers). If your cudeleteFilerrent implementation already shares final docs across timesheets, adjust it so that you only unlink (set `bulkApprovedDocId` to null) and never delete shared `FinalDocumentNew` or files.
- **Scenarios 2, 3, 4 (project-scoped or approved-only cleanup):** Do **not** delete `FinalDocumentNew` or files; only set the reference (`bulkApprovedDocId` = null) and update flags (`finalFlag` = false, `clientApprovalStatusId` = 1) on the `TimesheetDocumentDetailsNew` rows being cleaned.

---

## New methods to add

### 5.1 `TimesheetDocumentServiceNew`

| Method | Signature | Responsibility |
|--------|-----------|----------------|
| Unlink/clean all docs for one project on one timesheet | `void deleteByTimesheetIdAndProjectId(Long timesheetId, Integer projectId)` | Find all `TimesheetDocumentDetailsNew` for this `(timesheetId, projectId)`. For each row: set **`bulkApprovedDocId = null`**, **`finalFlag = false`**, **`clientApprovalStatusId = 1`**. Do **not** delete `FinalDocumentNew` rows or physical files (same doc may be referenced by other users/rows). Save the updated rows. |
| Unlink only approved/final doc reference for one project | `void deleteApprovedDocumentsByTimesheetIdAndProjectId(Long timesheetId, Integer projectId)` | Find `TimesheetDocumentDetailsNew` for this `(timesheetId, projectId)` where `finalFlag == true`. For each such row: set **`bulkApprovedDocId = null`**, **`finalFlag = false`**, **`clientApprovalStatusId = 1`**. Do **not** delete the row, `FinalDocumentNew`, or physical files (they may be shared). Save the updated rows. |

### 5.2 Repository (if needed)

| Class | Method | Purpose |
|-------|--------|---------|
| `TimesheetDocumentDetailsNewRepository` | `List<TimesheetDocumentDetailsNew> findAllByTimesheetIdAndProjectId(@Param("timesheetId") Long timesheetId, @Param("projectId") Integer projectId)` | Used by `deleteByTimesheetIdAndProjectId` and by `deleteApprovedDocumentsByTimesheetIdAndProjectId` (then filter by `finalFlag` in Java). Alternatively, add `findByTimesheetIdAndProjectIdAndFinalFlagTrue` for Scenario 4. Existing `findByTimesheetIdAndProjectIdAndActive` returns only active; for delete you can use that if only active rows exist, or add an unrestricted `findAllByTimesheetIdAndProjectId`. |

---

## Execution order (reference)

1. **TimesheetServiceNew.updateTimesheet**
   - Resolve `DayTypeTransition` (existing).
   - **If WORKING_TO_NON_WORKING:** call `timesheetDocumentService.deleteByTimesheetId(timesheetId)`; skip structure cleanup and document upload; then update header and return (or follow your non‑working flow).
   - **Else:** run working-day validations (existing), then **cleanTimesheetStructure** (which now does document cleanup for removed locations and removed projects).
2. **TimesheetStructureCleanupService.cleanTimesheetStructure**
   - **cleanupRemovedLocations** → for each removed location, **deleteLocationCascade** (first delete docs per project under that location, then existing cascade).
   - **cleanupRemovedProjects** → for each removed project, **deleteProjectCascade** (first delete docs for that project, then existing cascade).
3. **TimesheetServiceNew** (after structure cleanup)
   - Update header (existing).
   - **handleUpdateTimesheet** → **handleProjectsUnderLocationMapping** (existing). In the UPDATE project branch: detect Approved → Filled and call **deleteApprovedDocumentsByTimesheetIdAndProjectId**.
   - **handleDocumentUploadsFromNewContract** (existing) only when day type is working and documentData is present.

---

## Files to touch (summary)

| File | Change |
|------|--------|
| `TimesheetServiceNew.java` | Scenario 1: after resolve transition, branch on WORKING_TO_NON_WORKING and call `deleteByTimesheetId`; skip structure cleanup and document upload for that branch. Scenario 4: in `handleProjectsUnderLocationMapping`, in UPDATE branch, before/after `projectTimesheetService.update`, call `deleteApprovedDocumentsByTimesheetIdAndProjectId` when status goes from Approved to Filled. |
| `TimesheetStructureCleanupService.java` | Inject `TimesheetDocumentServiceNew`. In `deleteLocationCascade`: for each project in location, call `deleteByTimesheetIdAndProjectId`. In `deleteProjectCascade`: call `deleteByTimesheetIdAndProjectId(timesheetId, projectId)`. |
| `TimesheetDocumentServiceNew.java` | Add `deleteByTimesheetIdAndProjectId(Long timesheetId, Integer projectId)`. Add `deleteApprovedDocumentsByTimesheetIdAndProjectId(Long timesheetId, Integer projectId)`. |
| `TimesheetDocumentDetailsNewRepository.java` (optional) | Add `findAllByTimesheetIdAndProjectId` (and optionally `findByTimesheetIdAndProjectIdAndFinalFlagTrue`) if not already covered by existing methods. |

---

## Constants / status values

- Use the same client approval status values as elsewhere (e.g. **1** = Pending/Filled, **2** = Approved). If you have a constant or enum (e.g. `ClientApprovalStatus` or in `TimesheetAggregationHelper`), use it in the Scenario 4 condition.

---

*Checklist end. Implement in the order above and run tests for all four scenarios.*
