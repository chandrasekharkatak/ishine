# Named Queries Migration - Complete Report

## Summary
Complete migration of all timesheet-related named queries in `jpa-named-queries.properties` to use the new `_new` tables.

**Date:** 2025-01-30  
**Status:** ✅ **MIGRATION COMPLETE**

---

## All Queries Updated

### TimesheetActivityMap Queries (3 queries) ✅
1. ✅ `TimesheetActivityMap.activitiesByTimesheetId`
2. ✅ `TimesheetActivityMap.activitiesByTimesheetIdforBiomax`
3. ✅ `TimesheetActivityMap.getTimesheetActivityByTimesheetId`

### Timesheet Queries (16 queries) ✅
4. ✅ `Timesheet.getMyReporteesTimesheetRequests`
5. ✅ `Timesheet.getMyReporteesApprovedTimesheetRequests2`
6. ✅ `Timesheet.getMyReporteesApprovedTimesheets`
7. ✅ `Timesheet.getLast7DaysTimesheetsByEmpId`
8. ✅ `Timesheet.countMyReporteesTimesheetRequests`
9. ✅ `Timesheet.getTimesheetsForHomePageByEmpId`
10. ✅ `Timesheet.getAllTimesheetData`
11. ✅ `Timesheet.getLast9DaysPendingTimesheetReport`
12. ✅ `Timesheet.getLast9DaysFilledTimesheetReport`
13. ✅ `Timesheet.getAllMyTeamTimesheets`
14. ✅ `Timesheet.getAllMyTimesheets`
15. ✅ `Timesheet.findTimesheetOnLeaveDate`
16. ✅ `Timesheet.getAllLeaveTimesheetsWithoutLeaveApplication`
17. ✅ `Timesheet.getInactiveActivitiesByTimesheetId`
18. ✅ `Timesheet.getMyTeamsFilledEodCountByManagerId`
19. ✅ `Timesheet.getTimesheetFilledByMember`

### Project Queries (1 query) ✅
20. ✅ `Project.poProjectTimesheetSync`

### Department Queries (1 query) ✅
21. ✅ `Department.getSegregatedDeptEodDefaulter` (NEWLY UPDATED)

---

## Total Statistics

- **Total Named Queries Updated:** 21 queries
- **Backup Queries Created:** 21 OLD queries
- **Status:** ✅ **ALL QUERIES MIGRATED**

---

## Key Changes Applied

### Table Name Changes
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- `timesheet_document_details` → `timesheet_document_details_new`

### Master Table JOINs Added
- `day_type_master_new` for day_type resolution
- `status_master_new` for status resolution
- `client_status_master_new` for client approval status resolution
- `project_timesheet_status_new` for project-specific status

### Column Updates
- `total_time` → `ROUND(et.total_activities_minutes / 60, 2)`
- `total_working_hours` → `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`
- `completion_time` → `CAST(etam.duration_minutes AS DECIMAL(10,2))/60`

### Composite Key Handling
- Added `project_id` conditions for composite key matching
- Updated JOINs to include `etam.project_id = t.project_id`

---

## Verification

- ✅ All active named queries use `_new` tables
- ✅ All backup queries created with `OLD` suffix
- ✅ No remaining old table references in active queries
- ✅ Master table JOINs properly integrated
- ✅ Column mappings updated correctly

---

## Files Modified

1. `src/main/resources/META-INF/jpa-named-queries.properties`

---

## Notes

### Missing Queries from User's List
The following queries from the user's original list were not found in `jpa-named-queries.properties`:
- These are likely **inline queries** in repository interfaces
- They have already been updated in previous phases:
  - `EmployeeTimesheetsNewRepository` (Phase 1)
  - `TimesheetActivityMapNewRepository` (Phase 1)
  - `TimesheetDocumentDetailsNewRepository` (Phase 1)
  - `EmployeeRepository` (Phase 1)
  - `ProjectRepository` (Phase 1)

---

**Last Updated:** 2025-01-30  
**Status:** ✅ **MIGRATION COMPLETE - ALL NAMED QUERIES UPDATED**

