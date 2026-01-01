# Phase 1, Task 2: Query Update Summary

## Overview
This document provides a comprehensive summary of all query updates completed in Phase 1, Task 2.

**Date:** 2025-01-30  
**Status:** Most simple queries completed, complex queries tracked separately

---

## ✅ Completed Queries Summary

### Category B: MODIFIED Queries (Simple Updates)

**Total Completed:** ~28 queries

#### Set 1-12: Already Updated (Previous Sets)
1. ✅ `getTimesheetDataByEmpIdAndDate`
2. ✅ `getAllTimesheetData`
3. ✅ `getLast9DaysPendingTimesheetReport`
4. ✅ `getLast9DaysFilledTimesheetReport`
5. ✅ `getMyReporteesTimesheetRequests`
6. ✅ `getMyReporteesApprovedTimesheetRequests2`
7. ✅ `countMyReporteesTimesheetRequests`
8. ✅ `getMyReporteesApprovedTimesheets`
9. ✅ `getLast7DaysTimesheetsByEmpId`
10. ✅ `getTimesheetsForHomePageByEmpId`
11. ✅ `getAllMyTeamTimesheets`
12. ✅ `getAllMyTimesheets`
13. ✅ `findTimesheetOnLeaveDate`
14. ✅ `getInactiveActivitiesByTimesheetId`
15. ✅ `getMyTeamsFilledEodCountByManagerId`
16. ✅ `getTimesheetFilledByMember`
17. ✅ `getAllEmployeeTimesheetsBetweenDates`
18. ✅ `getTimesheetsByDepartmentAndDateRange`
19. ✅ `findTimesheetsForRejection` (JPQL)
20. ✅ `findAllLeaveTimesheetsWithoutLeaveApplication` (JPQL)
21. ✅ `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise` (JPQL)
22. ✅ `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise` (JPQL)
23. ✅ `findDatesByEmpIdAndProjectId` (JPQL)
24. ✅ `getRejectedTimesheetIdByEmpAndDateRange` (JPQL)
25. ✅ `allTimesheetFilledDatesForDateRange` (JPQL)

#### Set 13: Verified as Already Updated (This Set)
26. ✅ `getLastFilledTimesheet`
27. ✅ `checkEmployeeActiveOrNot`
28. ✅ `findByEmpIdAndDateBetween`
29. ✅ `getTotalVmsFilledCount`
30. ✅ `totalIshineFilledCount`
31. ✅ `totalvmsNotFilled`
32. ✅ `totalIshineNotFilledCount`
33. ✅ `getPendingTimesheetsByEmpAndTeam`
34. ✅ `getMyTimesheetRequests`
35. ✅ `getMyReportees` (JPQL - No update needed)

#### Complex Queries (Partially Updated)
36. ✅ `getLastTimesheetFiledByEmpId`
37. ✅ `totalIshineNotFilledCountForAllEmpDash`
38. ✅ `getEmployeeSummaryReportClientSideApplicable`
39. ✅ `getEmployeeSummaryReportAll`
40. ✅ `getProjectByMonthRangeAndEmpId`
41. ✅ `getTimesheetDashboardCountForProject`
42. ✅ `getTimesheetDashboardCountForEmployee`
43. ✅ `getAllEmpTimesheetDashboardCountForProject`
44. ✅ `getTimesheetDashboardCountForAllEmployee`
45. ✅ `getEmployeeViewForClientAttendanceStatus`

---

## ⚠️ Partially Complete Queries

### 1. `getEmployeeSummaryReportAllEMP`
- **Status:** ⚠️ **PARTIALLY COMPLETE**
- **Issue:** `Employee_Timesheets_With_Activities` CTE (line ~6742) still uses old tables
- **Required Fix:**
  - Change `employee_timesheets` → `employee_timesheets_new`
  - Change `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Add JOINs to `day_type_master_new` and `status_master_new`
  - Update column references

---

## 🔴 Complex Queries (Category C - Tracked Separately)

**Total:** 37 queries  
**Status:** All tracked in `COMPLEX_QUERIES_TRACKER.md` for batch processing

### Critical Complex Queries (Highest Priority)
1. 🔴 `getProjectViewForClientAttendanceStatus` - 700+ lines
2. 🔴 `getEmployeeTimesheetAsCalender` - 300+ lines
3. 🔴 `getEmployeeTimesheetAsCalenderByProjectId` - 300+ lines
4. 🔴 `getEmployeeTimesheetAsCalenderForAllEmp` - 300+ lines
5. 🔴 `getEmployeeTimesheetAsCalenderByProjectIdForAllEmp` - 300+ lines

### High-Risk Aggregation Queries
6. 🔴 `getAllEmployeeDSROfRM`
7. 🔴 `getEmployeeSummaryOnExport`
8. 🔴 `getEmployeeSummaryOnExportAccordingToStatus`
9. 🔴 `getVmsDocumentApprovalStatusWiseCount`

### Medium-Risk Queries
10-37. 🔴 (See `COMPLEX_QUERIES_TRACKER.md` for full list)

---

## Common Update Patterns Applied

### 1. Table Name Changes
- ✅ `employee_timesheets` → `employee_timesheets_new`
- ✅ `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- ✅ `timesheet_document_details` → `timesheet_document_details_new`

### 2. Master Table JOINs Added
- ✅ `day_type_master_new` for `day_type_id` → `day_type`
- ✅ `status_master_new` for `status` (status_id) → `status`
- ✅ `client_status_master_new` for client approval status

### 3. Column Reference Updates
- ✅ `total_time` → `ROUND(et.total_activities_minutes / 60, 2)`
- ✅ `total_working_hours` → `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`
- ✅ `description` → Now from `etam.description` (EmployeeTimesheetActivitiesMappingNew)
- ✅ `completion_time` → `CAST(etam.duration_minutes AS DECIMAL(10,2))/60`

### 4. Client Side ID Updates
- ✅ `employee_client_side_id_mapping_new` with `active = 1` check
- ✅ Updated client flag logic to use `ecsm.client_side_id IS NOT NULL`

### 5. Project Timesheet Status
- ✅ `project_timesheet_status_new` with composite key: `pts.timesheet_id` AND `pts.project_id = etam.project_id`

---

## Verification Checklist

For each updated query, the following was verified:

- [x] `_old` backup created
- [x] Table names updated to `_new` versions
- [x] Master table JOINs added where needed
- [x] Column references updated (total_time, total_working_hours, etc.)
- [x] `description` field now comes from `EmployeeTimesheetActivitiesMappingNew`
- [x] Status/status_id mapping correct
- [x] Day type/day_type_id mapping correct
- [x] Client side ID references use `employee_client_side_id_mapping_new` with `active = 1` check
- [x] Project timesheet status uses composite key correctly

---

## Next Steps

### Immediate Actions:
1. ⚠️ **Fix `getEmployeeSummaryReportAllEMP`** - Complete the CTE update
2. 📋 **Verify remaining simple queries** - Check if any Category B queries were missed
3. 🔴 **Plan complex queries batch** - Begin planning for Category C query updates

### Future Actions:
4. 🔴 **Batch process complex queries** - After all simple queries are complete
5. ✅ **Testing** - Comprehensive testing of all updated queries
6. ✅ **Documentation** - Final documentation of all changes

---

## Files Modified

### Repository Files:
- `src/main/java/com/apmosys/employeeportal/repository/EmployeeTimesheetsNewRepository.java`
  - ~45 queries updated
  - All updates include `_old` backups

### Named Query Files:
- `src/main/resources/META-INF/jpa-named-queries.properties`
  - Multiple named queries updated with `OLD` backups

---

## Progress Metrics

| Category | Total | Completed | In Progress | Pending |
|----------|-------|-----------|-------------|---------|
| **A) SAME** | 8 | 8 | 0 | 0 |
| **B) MODIFIED** | 35 | ~33 | 1 | ~1 |
| **C) REWRITE** | 37 | 0 | 0 | 37 |
| **TOTAL** | **80** | **~41** | **1** | **~38** |

**Completion Rate:** ~51% of all queries  
**Simple Queries Completion:** ~94% of Category B queries

---

## Notes

1. **Backup Strategy:** All updated queries have `_old` backup versions for rollback if needed.

2. **JPQL Queries:** JPQL queries using entity mappings were updated to use new entities (`EmployeeTimesheetsNew`, `DayTypeMasterNew`, `StatusMasterNew`, etc.).

3. **Complex Queries:** All complex queries (Category C) are tracked separately and will be processed in batches after simple queries are complete.

4. **Testing Priority:** Focus testing on:
   - Approval workflow queries
   - Dashboard queries
   - Client attendance queries (affects billing)

---

**Last Updated:** 2025-01-30  
**Status:** Most simple queries complete, ready for complex query planning

