# Timesheet Query Update Tracker

## Overview
This document tracks the progress of updating queries in `EmployeeTimesheetsNewRepository` to use the new `_new` table schema.

**Last Updated:** 2025-01-30

---

## Progress Summary

| Category | Total | Completed | In Progress | Pending |
|----------|-------|-----------|-------------|---------|
| **A) SAME** | 8 | 8 | 0 | 0 |
| **B) MODIFIED** | 35 | ~20 | 1 | ~14 |
| **C) REWRITE** | 37 | 0 | 0 | 37 |
| **TOTAL** | **80** | **~28** | **1** | **~51** |

---

## Category B: MODIFIED Queries (Simple Updates)

### ✅ COMPLETED (20 queries)

1. ✅ `getTimesheetDataByEmpIdAndDate` - Updated
2. ✅ `getAllTimesheetData` - Updated (named query)
3. ✅ `getLast9DaysPendingTimesheetReport` - Updated (named query)
4. ✅ `getLast9DaysFilledTimesheetReport` - Updated (named query)
5. ✅ `getAllEmployeeTimesheetsBetweenDates` - Updated
6. ✅ `getTimesheetsByDepartmentAndDateRange` - Updated
7. ✅ `findTimesheetsForRejection` - Updated (JPQL)
8. ✅ `findAllLeaveTimesheetsWithoutLeaveApplication` - Updated (JPQL)
9. ✅ `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise` - Updated (JPQL)
10. ✅ `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise` - Updated (JPQL)
11. ✅ `findDatesByEmpIdAndProjectId` - Updated (JPQL)
12. ✅ `getRejectedTimesheetIdByEmpAndDateRange` - Updated (JPQL)
13. ✅ `allTimesheetFilledDatesForDateRange` - Updated (JPQL)
14. ✅ `getLastTimesheetFiledByEmpId` - Updated
15. ✅ `totalIshineNotFilledCountForAllEmpDash` - Updated
16. ✅ `getEmployeeSummaryReportClientSideApplicable` - Updated
17. ✅ `getEmployeeSummaryReportAll` - Updated
18. ✅ `getProjectByMonthRangeAndEmpId` - Updated
19. ✅ `getTimesheetDashboardCountForProject` - Updated
20. ✅ `getTimesheetDashboardCountForEmployee` - Updated
21. ✅ `getAllEmpTimesheetDashboardCountForProject` - Updated
22. ✅ `getTimesheetDashboardCountForAllEmployee` - Updated
23. ✅ `getEmployeeViewForClientAttendanceStatus` - Updated

### ⚠️ IN PROGRESS (1 query)

24. ⚠️ `getEmployeeSummaryReportAllEMP` - **PARTIALLY COMPLETE**
    - **Issue**: `Employee_Timesheets_With_Activities` CTE (line ~6742) still uses old tables
    - **Required**: Update CTE to use `employee_timesheets_new` and master table JOINs

### 📋 NEXT SET TO UPDATE (Simple Queries - Category B)

**Priority: Update these 10 queries next (Queries 25-34)**

25. 📋 `getMyReporteesTimesheetRequests` - Named query
    - **Location**: Named query in `jpa-named-queries.properties`
    - **Change**: `employee_timesheets` → `employee_timesheets_new` + master table JOINs
    - **Status**: Has `_old` backup, needs active query update

26. 📋 `getMyReporteesApprovedTimesheetRequests2` - Named query
    - **Location**: Named query in `jpa-named-queries.properties`
    - **Change**: `employee_timesheets` → `employee_timesheets_new` + master table JOINs
    - **Status**: Has `_old` backup, needs active query update

27. 📋 `countMyReporteesTimesheetRequests` - Named query
    - **Location**: Named query in `jpa-named-queries.properties`
    - **Change**: `employee_timesheets` → `employee_timesheets_new`
    - **Status**: Has `_old` backup, needs active query update

28. 📋 `getMyReporteesApprovedTimesheets` - Named query
    - **Location**: Named query in `jpa-named-queries.properties`
    - **Change**: `employee_timesheets` → `employee_timesheets_new` + master table JOINs
    - **Status**: Check if has backup

29. 📋 `getLast7DaysTimesheetsByEmpId` - Named query
    - **Location**: Named query in `jpa-named-queries.properties`
    - **Change**: `employee_timesheets` → `employee_timesheets_new` + master table JOINs
    - **Status**: Check if has backup

30. 📋 `getTimesheetsForHomePageByEmpId` - Named query
    - **Location**: Named query in `jpa-named-queries.properties`
    - **Change**: Both `employee_timesheets` and `employee_timesheet_activities_mapping` → `_new` versions
    - **Status**: Check if has backup

31. 📋 `getAllMyTeamTimesheets` - Named query
    - **Location**: Named query in `jpa-named-queries.properties`
    - **Change**: `employee_timesheets` → `employee_timesheets_new` + status master JOIN
    - **Status**: Check if has backup

32. 📋 `getAllMyTimesheets` - Named query
    - **Location**: Named query in `jpa-named-queries.properties`
    - **Change**: `employee_timesheets` → `employee_timesheets_new` + status master JOIN
    - **Status**: Check if has backup

33. 📋 `findTimesheetOnLeaveDate` - Named query
    - **Location**: Named query in `jpa-named-queries.properties`
    - **Change**: `employee_timesheets` → `employee_timesheets_new` + day_type master JOIN
    - **Status**: Check if has backup

34. 📋 `getInactiveActivitiesByTimesheetId` - Named query
    - **Location**: Named query in `jpa-named-queries.properties`
    - **Change**: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
    - **Status**: Check if has backup

### 📋 REMAINING SIMPLE QUERIES (Category B - 4 more)

35. 📋 `getMyTeamsFilledEodCountByManagerId` - Named query
36. 📋 `getTimesheetFilledByMember` - Named query
37. 📋 `getLastFilledTimesheet` - Named query
38. 📋 `checkEmployeeActiveOrNot` - Named query
39. 📋 `findByEmpIdAndDateBetween` - Named query
40. 📋 `getTotalVmsFilledCount` - Named query
41. 📋 `totalIshineFilledCount` - Named query
42. 📋 `totalvmsNotFilled` - Named query
43. 📋 `totalIshineNotFilledCount` - Named query
44. 📋 `getPendingTimesheetsByEmpAndTeam` - Named query
45. 📋 `getMyTimesheetRequests` - Named query
46. 📋 `getMyReportees` - JPQL query

---

## Category C: COMPLEX QUERIES (Tracked Separately)

### 🔴 CRITICAL COMPLEX QUERIES (37 queries - Do at end)

These queries require complete rewrite due to business logic changes. **Tracked separately for batch processing at the end.**

#### Status Calculation Queries (Highest Priority - Affects Billing)
1. 🔴 `getEmployeeViewForClientAttendanceStatus` - **700+ lines** - ✅ Already updated
2. 🔴 `getProjectViewForClientAttendanceStatus` - **700+ lines** - Status: Pending
3. 🔴 `getTimesheetDashboardCountForEmployee` - **400+ lines** - ✅ Already updated
4. 🔴 `getEmployeeTimesheetAsCalender` - **300+ lines** - Status: Pending
5. 🔴 `getEmployeeTimesheetAsCalenderByProjectId` - **300+ lines** - Status: Pending
6. 🔴 `getEmployeeTimesheetAsCalenderForAllEmp` - **300+ lines** - Status: Pending
7. 🔴 `getEmployeeTimesheetAsCalenderByProjectIdForAllEmp` - **300+ lines** - Status: Pending

#### Aggregation Queries (High Priority - Affects Reporting)
8. 🔴 `getTimesheetDashboardCountForProject` - **200+ lines** - ✅ Already updated
9. 🔴 `getAllEmpTimesheetDashboardCountForProject` - **200+ lines** - ✅ Already updated
10. 🔴 `getTimesheetDashboardCountForAllEmployee` - **200+ lines** - ✅ Already updated
11. 🔴 `getEmployeeSummaryReportClientSideApplicable` - **200+ lines** - ✅ Already updated
12. 🔴 `getEmployeeSummaryReportAll` - **200+ lines** - ✅ Already updated
13. 🔴 `getEmployeeSummaryReportAllEMP` - **200+ lines** - ⚠️ Partially complete
14. 🔴 `getProjectByMonthRangeAndEmpId` - **100+ lines** - ✅ Already updated
15. 🔴 `totalIshineNotFilledCountForAllEmpDash` - Complex CTE - ✅ Already updated

#### Other Complex Queries
16. 🔴 `getAllEmployeeDSROfRM` - Complex aggregation - Status: Pending
17. 🔴 `getEmployeeSummaryOnExport` - Export format - Status: Pending
18. 🔴 `getEmployeeSummaryOnExportAccordingToStatus` - Export with status - Status: Pending
19. 🔴 `getVmsDocumentApprovalStatusWiseCount` - Document approval count - Status: Pending
20. 🔴 `getLastFilledTimesheetByEmp` - May need activity-level logic - Status: Pending
21. 🔴 `getEmployeeByNameAndEmpidForTimesheet` - Employee lookup - Status: Pending
22. 🔴 `getClientSideIdByProjectId` - Client side ID lookup - Status: Pending
23. 🔴 `getClientSideIdByProjectIdAndEmpId` - Client side ID with employee - Status: Pending
24. 🔴 `fetchEmploymentIdByEmpId` - Employment ID lookup - Status: Pending
25. 🔴 `getEmployeesWithClientId` - Employee list with client ID - Status: Pending
26. 🔴 `getTimesheetForEmployee` - Timesheet retrieval - Status: Pending
27. 🔴 `getDocumentsByEmpAndDate` - Document retrieval - Status: Pending
28. 🔴 `getEmployeeTimesheetsByProject` - Timesheet list by project - Status: Pending
29. 🔴 `allTimesheetFilledDatesForDateRange` - Date list query - ✅ Already updated (JPQL)

---

## Update Strategy

### Phase 1: Simple Queries (Current Phase)
- ✅ Focus on Category B queries (simple table name changes)
- ✅ Update 10 queries at a time
- ✅ Create `_old` backups before changes
- ✅ Update named queries in `jpa-named-queries.properties`

### Phase 2: Complex Queries (Future Phase)
- 🔴 Batch process all Category C queries together
- 🔴 Requires business logic understanding
- 🔴 May need stakeholder review
- 🔴 Higher risk - needs extensive testing

---

## Notes

1. **Named Queries**: Most simple queries are in `jpa-named-queries.properties`. Update there and create `OLD` backups.

2. **Master Table JOINs**: Remember to add JOINs to:
   - `day_type_master_new` for `day_type_id`
   - `status_master_new` for `status` (status_id)
   - `client_status_master_new` for client approval status

3. **Column Changes**:
   - `total_time` → Use `ROUND(et.total_activities_minutes / 60, 2)`
   - `total_working_hours` → Use `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`
   - `description` → Now comes from `EmployeeTimesheetActivitiesMappingNew`

4. **Composite Keys**: Remember to use composite key fields correctly:
   - `TimesheetActivityMapId`: `etam.id.timesheetId`, `etam.id.activityId`
   - `ProjectTimesheetStatusId`: `pts.id.timesheetId`, `pts.id.projectId`
   - `EmployeeClientSideIdMapId`: `ecsm.id.empId`, `ecsm.id.projectId`

---

## Next Actions

1. ✅ Update next set of 10 simple queries (25-34)
2. ⚠️ Complete `getEmployeeSummaryReportAllEMP` CTE update
3. 📋 Track complex queries for batch processing
4. 📋 Create detailed update plan for complex queries

