# Update Timesheet Flow – Frontend Checklist

Comparison of **Create** vs **Update** in the timesheet form to list items that could be missing or different in the update case.

---

## 1. Payload (createOrUpdateObj)

| Item | Create | Update | Notes |
|------|--------|--------|--------|
| `timesheetId` | `null` | `this.timesheetId` | ✅ Present in update |
| `updatedBy` | Not sent | `this.currentUser.empId` | ✅ Present in update |
| `createdBy` | Set | Set | Same |
| `empId` | Set | Set | Same (self or team member) |
| `date`, `workCheckIn`, `workCheckOut`, `dayTypeId`, `totalWorkingMinutes`, `locationSessions`, `documentData` | Set | Set | Same structure |
| `locationSessions[].locationMappingId` | From clone (may be null for new) | From clone (preserved from load) | ✅ Preserved when loading for edit |
| `locationSessions[].projects[].locationMappingId` | From clone | From clone | ✅ Set in populateProjects from server |
| `locationSessions[].projects[].clientLocationId` | From form | From form | ✅ Populated from timesheet data on load |
| Activities `id` (existing) | N/A | In clone from form | Backend may use for update/delete detection |
| Hours → minutes conversion | Done before send | Done before send | Same logic |

**Conclusion:** Update payload includes `timesheetId`, `updatedBy`, and reuses the same shape as create. No obvious payload fields missing for update.

---

## 2. Edit Load Flow (where “No valid client details found” was fixed)

| Step | Done in update? | Notes |
|------|------------------|--------|
| `loadTimesheetForUpdate(timesheetId)` | ✅ | Calls `getTimesheetById` |
| Set `timesheetId`, `fromDate`, `dayType`, work times, `totalPresence` | ✅ | From `populateFormFromTimesheetData` |
| Set **`timesheetAppliedFor`** and **`timesheetFilledForUser.empId`** | ✅ Fixed | **Bug fix:** For team, `timesheetFilledForUser.empId` is set to `timesheetData.empId` immediately so `getClientDetailsByProjectIdAndEmpId` uses the correct empId when populating dropdowns. |
| `getProjectListForDateAndEmpId(timesheetData.empId)` | ✅ | Uses timesheet’s empId |
| `populateLocations` → `populateProjects` → `populateProjectDropdowns` | ✅ | Each project triggers `getClientDetailsByProjectIdAndEmpId(project)` |
| `populateDocuments` | ✅ | From `timesheetData.documentData` |
| Expand first location / project | ✅ | UX |

**Conclusion:** With the fix, client details are requested with the correct empId (timesheet owner), so “No valid client details found” on edit should be resolved.

---

## 3. Possible Gaps / Things to Verify in Update

1. **`locationMappingId` in payload**  
   Update sends `locationSessions` from `structuredClone(this.timesheetLocations)`, which were populated from server (including `locationMappingId`). Confirm backend update logic uses these IDs to update existing location rows rather than always creating new ones.

2. **Activity `id` (primary key)**  
   Existing activities loaded for edit have `id` from server. Confirm update API uses these IDs to update/delete existing activity rows and only creates new rows for activities without `id`.

3. **Document handling**  
   - Existing documents: `documentData` may contain `docId` and other server fields.  
   - New files: `selectedFile` / new uploads.  
   Confirm backend distinguishes “keep existing”, “replace”, “add new” and that frontend sends the same `documentData` structure as create where applicable.

4. **Validation**  
   Update uses the same `validateCreate(validationContext)` as create. If there are update-only rules (e.g. cannot change date, or different rules for locked timesheets), they may need to be added.

5. **Loading state**  
   Create sets `this.isLoadingTimesheet = true` before API and `false` in next/error. Update does not set a loading flag; consider adding for consistency.

6. **Success callback**  
   Create: alert, `resetForm()`. Update: alert, `timesheetUpdated.emit(this.timesheetId)`. Confirm parent refreshes list or reloads data so the table reflects the update.

7. **Employee name on edit (team case)**  
   `timesheetFilledForUser.name` is set from `timesheetData.employeeName` if present; otherwise it is filled when `loadTeamMemberForUpdate` completes. If `getTimesheetById` does not return `employeeName`, the dropdown may show name only after team member load finishes.

---

## 4. Summary

- **“No valid client details found” on edit:** Fixed by setting `timesheetFilledForUser.empId = timesheetData.empId` (and name when available) as soon as we detect team mode, before any async project/populate logic runs, so `getClientDetailsByProjectIdAndEmpId` always uses the timesheet owner’s empId.
- **Update payload:** Includes `timesheetId` and `updatedBy`; otherwise aligned with create. No clear missing top-level or nested fields.
- **Remaining checks:** Backend use of `locationMappingId` and activity `id` on update, document replace/add logic, optional loading state and validation refinements for update-only rules.
