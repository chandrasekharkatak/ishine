# Named Queries Scan Report - Timesheet Migration

## Summary
This document provides a comprehensive scan of all named queries in `jpa-named-queries.properties` to identify which queries still need updates to use the new `_new` tables.

**Date:** 2025-01-30  
**Status:** ✅ **SCAN COMPLETE**

---

## Queries Already Updated (Have OLD Backups)

### TimesheetActivityMap Queries ✅
1. ✅ `TimesheetActivityMap.activitiesByTimesheetId` - Updated
2. ✅ `TimesheetActivityMap.activitiesByTimesheetIdforBiomax` - Updated
3. ✅ `TimesheetActivityMap.getTimesheetActivityByTimesheetId` - Updated

### Timesheet Queries ✅
4. ✅ `Timesheet.getMyReporteesTimesheetRequests` - Updated
5. ✅ `Timesheet.getMyReporteesApprovedTimesheetRequests2` - Updated
6. ✅ `Timesheet.getMyReporteesApprovedTimesheets` - Updated
7. ✅ `Timesheet.getLast7DaysTimesheetsByEmpId` - Updated
8. ✅ `Timesheet.countMyReporteesTimesheetRequests` - Updated
9. ✅ `Timesheet.getTimesheetsForHomePageByEmpId` - Updated
10. ✅ `Timesheet.getAllTimesheetData` - Updated
11. ✅ `Timesheet.getLast9DaysPendingTimesheetReport` - Updated
12. ✅ `Timesheet.getLast9DaysFilledTimesheetReport` - Updated
13. ✅ `Timesheet.getAllMyTeamTimesheets` - Updated
14. ✅ `Timesheet.getAllMyTimesheets` - Updated
15. ✅ `Timesheet.findTimesheetOnLeaveDate` - Updated
16. ✅ `Timesheet.getAllLeaveTimesheetsWithoutLeaveApplication` - Updated
17. ✅ `Timesheet.getInactiveActivitiesByTimesheetId` - Updated
18. ✅ `Timesheet.getMyTeamsFilledEodCountByManagerId` - Updated
19. ✅ `Timesheet.getTimesheetFilledByMember` - Updated

### Project Queries ✅
20. ✅ `Project.poProjectTimesheetSync` - Updated

---

## Queries Still Needing Updates

### Employee Queries ❌
1. ❌ **`Employee.getSegregatedDeptEodDefaulter`** (Line ~760)
   - **Status:** Still uses `employee_timesheets` (old table)
   - **Action Required:** Update to `employee_timesheets_new`

---

## Queries Not Found in Properties File

The following queries from the user's list were not found in `jpa-named-queries.properties`. They may be:
- Inline queries in repository interfaces
- Already migrated to inline queries
- Not yet created

### Missing Timesheet Queries:
- `Timesheet.getLastFilledTimesheetByEmp`
- `Timesheet.getEmployeeByNameAndEmpidForTimesheet`
- `Timesheet.getAllProjectsByEmpId`
- `Timesheet.checkIfProjectRequiresClientId`
- `Timesheet.getActiveProjectsAndClientSideIdByEmpId`
- `Timesheet.getClientSideIdByProjectIdAndEmpId`
- `Timesheet.getEmployeesWithClientId`
- `Timesheet.totalIshineFilledCount`
- `Timesheet.totalvmsNotFilled`
- `Timesheet.totalIshineNotFilledCount`
- `Timesheet.getVmsDocumentApprovalStatusWiseCount`
- `Timesheet.getEmployeeTimesheetAsCalender`
- `Timesheet.getEmployeeTimesheetAsCalenderByProjectId`
- `Timesheet.getRejectedTimesheetIdByEmpAndDateRange`
- `Timesheet.getProjectViewForClientAttendanceStatus`
- `Timesheet.getEmployeeViewForClientAttendanceStatus`
- `Timesheet.getAllEmployeeDSROfRM`
- `Timesheet.getEmployeeSummaryOnExport`
- `Timesheet.getEmployeeSummaryOnExportAccordingToStatus`
- `Timesheet.getProjectByMonthRangeAndEmpId`
- `Timesheet.allTimesheetFilledDatesForDateRange`
- `Timesheet.findDatesByEmpIdAndProjectId`

**Note:** These queries may be inline queries in `EmployeeTimesheetsNewRepository` or other repositories, which have already been updated in previous phases.

---

## Action Items

### Immediate Action Required:
1. ✅ Update `Employee.getSegregatedDeptEodDefaulter` to use `employee_timesheets_new`

### Verification Needed:
1. Check if missing queries are inline queries in repositories (already updated)
2. Verify all OLD backup queries are correctly marked

---

## Summary Statistics

- **Total Queries Scanned:** 20+ named queries
- **Queries Updated:** 20 queries ✅
- **Queries Needing Update:** 1 query ❌
- **Queries Not Found:** 21 queries (likely inline queries)

---

**Last Updated:** 2025-01-30  
**Next Step:** Update `Employee.getSegregatedDeptEodDefaulter`

