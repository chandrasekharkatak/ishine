# EmployeeTimesheetsNewRepository - Query Categorization Analysis

## Overview
This document categorizes ALL queries in `EmployeeTimesheetsNewRepository` into three buckets:
- **A) SAME** - Can be reused without change
- **B) MODIFIED** - Needs minor change for `_new` table
- **C) REWRITE** - Needs full rewrite due to logic or aggregation change

---

## CATEGORIZATION SUMMARY

| Category | Count | Percentage |
|----------|-------|------------|
| **A) SAME** | 8 | ~10% |
| **B) MODIFIED** | 35 | ~45% |
| **C) REWRITE** | 37 | ~45% |
| **TOTAL** | **80** | **100%** |

---

## DETAILED CATEGORIZATION

### A) SAME - Can be reused without change (8 queries)

These queries don't reference timesheet tables or use JPQL that maps correctly.

| # | Query Name | Current Query Summary | Reason for Classification |
|---|------------|----------------------|---------------------------|
| 1 | `findAllByEmpIdAndDateBetweenOrderByDateDesc` | Spring Data JPA method - finds by empId and date range | Uses JPA method naming - entity mapping handles table change |
| 2 | `findByEmpIdAndDate` | Spring Data JPA method - finds by empId and date | Uses JPA method naming - entity mapping handles table change |
| 3 | `findByEmpIdAndTimesheetIdIn` | Spring Data JPA method - finds by empId and timesheet IDs | Uses JPA method naming - entity mapping handles table change |
| 4 | `findByTimesheetIdIn` | Spring Data JPA method - finds by timesheet IDs | Uses JPA method naming - entity mapping handles table change |
| 5 | `getActiveProjectsByEmpId` | JPQL - Gets active projects for employee | No timesheet table reference - only Project/Team/EmployeeTeamMap |
| 6 | `isInTNMProject` | JPQL - Checks if employee in TNM project | No timesheet table reference - only Project/Team/EmployeeTeamMap |
| 7 | `getActiveProjectsAndClientSideIdByEmpId` | JPQL - Gets projects with client side ID | No timesheet table reference - only Project/Team/EmployeeTeamMap/EmployeeClientSideIdMapping |
| 8 | `checkProjectIsClientApplicable` | JPQL - Checks if project has client side ID | No timesheet table reference - only Project entity |

---

### B) MODIFIED - Needs minor change for `_new` table (35 queries)

These queries need table name changes and minor column adjustments.

| # | Query Name | Current Query Summary | Bucket | Reason for Classification |
|---|------------|----------------------|--------|---------------------------|
| 9 | `getTimesheetDataByEmpIdAndDate` | Native: `SELECT emp_Id,date,status FROM employee_timesheets WHERE...` | **B** | Simple SELECT - change `employee_timesheets` → `employee_timesheets_new` |
| 10 | `getMyReporteesTimesheetRequests` | Native: Named query - references `employee_timesheets` | **B** | Change table name + check column mappings |
| 11 | `getMyReporteesApprovedTimesheetRequests2` | Native: Named query - references `employee_timesheets` | **B** | Change table name + check column mappings |
| 12 | `countMyReporteesTimesheetRequests` | Native: Named query - COUNT from `employee_timesheets` | **B** | Change table name |
| 13 | `getMyReporteesApprovedTimesheets` | Native: Named query - references `employee_timesheets` | **B** | Change table name + check column mappings |
| 14 | `getLast7DaysTimesheetsByEmpId` | Native: Named query - references `employee_timesheets` | **B** | Change table name |
| 15 | `getTimesheetsForHomePageByEmpId` | Native: Named query - JOINs `employee_timesheets` + `employee_timesheet_activities_mapping` | **B** | Change both table names |
| 16 | `getAllTimesheetData` | Native: Named query - SELECT from `employee_timesheets` | **B** | Change table name |
| 17 | `getLast9DaysPendingTimesheetReport` | Native: Named query - references `employee_timesheets` | **B** | Change table name |
| 18 | `getLast9DaysFilledTimesheetReport` | Native: Named query - references `employee_timesheets` | **B** | Change table name |
| 19 | `getAllMyTeamTimesheets` | Native: Named query - references `employee_timesheets` | **B** | Change table name + check status column |
| 20 | `getAllMyTimesheets` | Native: Named query - references `employee_timesheets` | **B** | Change table name + check status column |
| 21 | `findTimesheetOnLeaveDate` | Native: Named query - references `employee_timesheets` | **B** | Change table name |
| 22 | `getInactiveActivitiesByTimesheetId` | Native: Named query - references `employee_timesheet_activities_mapping` | **B** | Change `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new` |
| 23 | `getMyTeamsFilledEodCountByManagerId` | Native: Named query - references `employee_timesheets` | **B** | Change table name + aggregation logic may need adjustment |
| 24 | `getTimesheetFilledByMember` | Native: Named query - references `employee_timesheets` | **B** | Change table name |
| 25 | `getAllEmployeeTimesheetsBetweenDates` | Native: Complex JOIN - `employee_timesheets` + `employee_timesheet_activities_mapping` | **B** | Change both table names + verify all column mappings |
| 26 | `getTimesheetsByDepartmentAndDateRange` | Native: Complex JOIN - `employee_timesheets` + `employee_timesheet_activities_mapping` | **B** | Change both table names + verify all column mappings |
| 27 | `getLastFilledTimesheet` | Native: Subquery + JOIN - `employee_timesheets` + `employee_timesheet_activities_mapping` | **B** | Change both table names |
| 28 | `checkEmployeeActiveOrNot` | Native: Subquery + JOIN - `employee_timesheets` + `employee_timesheet_activities_mapping` | **B** | Change both table names |
| 29 | `findByEmpIdAndDateBetween` | Native: Complex JOIN with GROUP BY - `employee_timesheets` + `employee_timesheet_activities_mapping` + `timesheet_document_details` | **B** | Change all 3 table names + verify aggregation |
| 30 | `getTotalVmsFilledCount` | Native: CTE with `employee_timesheets` | **B** | Change table name in CTE |
| 31 | `totalIshineFilledCount` | Native: COUNT from `employee_timesheets` | **B** | Change table name |
| 32 | `totalvmsNotFilled` | Native: CTE with `employee_timesheets` | **B** | Change table name in CTE |
| 33 | `totalIshineNotFilledCount` | Native: Complex CTE with `employee_timesheets` + `timesheet_document_details` | **B** | Change both table names + verify aggregation logic |
| 34 | `findDatesByEmpIdAndProjectId` | JPQL: JOIN `Timesheet` + `TimesheetDocumentDetails` | **B** | Entity mapping should handle, but verify `projectId` field exists in new structure |
| 35 | `getPendingTimesheetsByEmpAndTeam` | Native: JOIN `employee_timesheets` + `employee_timesheet_activities_mapping` | **B** | Change both table names + verify status/team filtering |
| 36 | `getMyTimesheetRequests` | Native: JOIN `employee_timesheets` + `employee_timesheet_activities_mapping` | **B** | Change both table names + verify date range filtering |
| 37 | `getLastTimesheetFiledByEmpId` | Native: CTE with ROW_NUMBER - references `employee_timesheets` | **B** | Change table name + verify ranking logic |
| 38 | `getRejectedTimesheetIdByEmpAndDateRange` | JPQL: SELECT from `Timesheet` entity | **B** | Entity mapping should handle, verify status field |
| 39 | `getMyReportees` | JPQL: SELECT from `Employee` only | **B** | No timesheet table - should work as-is, but verify entity relationships |
| 40 | `findTimesheetsForRejection` | JPQL: SELECT from `Timesheet` entity | **B** | Entity mapping should handle |
| 41 | `findAllLeaveTimesheetsWithoutLeaveApplication` | JPQL: Complex SELECT with JOINs - uses `Timesheet` entity | **B** | Entity mapping should handle, but verify all field mappings |
| 42 | `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise` | JPQL: Complex SELECT with JOINs - uses `Timesheet` entity | **B** | Entity mapping should handle, but verify all field mappings |
| 43 | `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise` | JPQL: Complex SELECT with JOINs - uses `Timesheet` entity | **B** | Entity mapping should handle, but verify all field mappings |

---

### C) REWRITE - Needs full rewrite due to logic or aggregation change (37 queries)

These queries need complete rewrite due to:
- Business logic changes (day-level vs project-level)
- Status calculation changes
- Aggregation logic changes
- Complex CTE queries with activity-level granularity

| # | Query Name | Current Query Summary | Bucket | Reason for Classification |
|---|------------|----------------------|--------|---------------------------|
| 44 | `getTimesheetDashboardCountForEmployee` | Native: 400+ line CTE - complex status calculation | **C** | **CRITICAL**: Status calculation logic completely different - day-level vs project-level aggregation |
| 45 | `getTimesheetDashboardCountForProject` | Native: 200+ line CTE - project-level aggregation | **C** | **CRITICAL**: Project-level status derivation needs rewrite - uses old status model |
| 46 | `getEmployeeTimesheetAsCalender` | Native: 300+ line CTE - calendar view with daily status | **C** | **CRITICAL**: Daily status calculation uses old model - needs activity-level aggregation |
| 47 | `getEmployeeTimesheetAsCalenderByProjectId` | Native: 300+ line CTE - calendar view filtered by project | **C** | **CRITICAL**: Same as above + project filtering logic |
| 48 | `getEmployeeTimesheetAsCalenderForAllEmp` | Native: 300+ line CTE - calendar view for all employees | **C** | **CRITICAL**: Same as above - status calculation rewrite needed |
| 49 | `getEmployeeTimesheetAsCalenderByProjectIdForAllEmp` | Native: 300+ line CTE - calendar view for all employees by project | **C** | **CRITICAL**: Same as above - status calculation rewrite needed |
| 50 | `getAllEmpTimesheetDashboardCountForProject` | Native: 200+ line CTE - project dashboard for all employees | **C** | **CRITICAL**: Project-level aggregation needs rewrite |
| 51 | `getTimesheetDashboardCountForAllEmployee` | Native: 200+ line CTE - employee dashboard for all | **C** | **CRITICAL**: Employee-level aggregation needs rewrite |
| 52 | `getEmployeeSummaryReportClientSideApplicable` | Native: 200+ line CTE - client-side employee summary | **C** | **CRITICAL**: Status aggregation logic needs rewrite |
| 53 | `getEmployeeSummaryReportAll` | Native: 200+ line CTE - all employee summary | **C** | **CRITICAL**: Status aggregation logic needs rewrite |
| 54 | `getEmployeeSummaryReportAllEMP` | Native: 200+ line CTE - all employee summary (variant) | **C** | **CRITICAL**: Status aggregation logic needs rewrite |
| 55 | `getEmployeeViewForClientAttendanceStatus` | Native: 700+ line CTE - complex client attendance view | **C** | **CRITICAL**: Most complex query - complete status derivation rewrite needed |
| 56 | `getProjectViewForClientAttendanceStatus` | Native: 700+ line CTE - complex project attendance view | **C** | **CRITICAL**: Most complex query - complete status derivation rewrite needed |
| 57 | `getProjectByMonthRangeAndEmpId` | Native: 100+ line CTE - project list by month/employee | **C** | Status filtering logic may need adjustment |
| 58 | `totalIshineNotFilledCountForAllEmpDash` | Native: Complex CTE with document status aggregation | **C** | Document status aggregation needs rewrite for new structure |
| 59 | `allTimesheetFilledDatesForDateRange` | JPQL: Date list query - may need activity-level grouping | **C** | May need to aggregate by activity if multiple activities per day |
| 60 | `getAllEmployeeDSROfRM` | Native: Complex JOIN with aggregation - references old tables | **C** | **HIGH RISK**: Used in reporting - needs activity-level aggregation |
| 61 | `getEmployeeSummaryOnExport` | Native: Complex query - export format | **C** | Export format may need adjustment for new structure |
| 62 | `getEmployeeSummaryOnExportAccordingToStatus` | Native: Complex query - export with status filtering | **C** | Status filtering logic needs rewrite |
| 63 | `getVmsDocumentApprovalStatusWiseCount` | Native: Document approval count query | **C** | Document approval logic may need adjustment for new document structure |
| 64 | `getLastFilledTimesheetByEmp` | Native: Last timesheet query - may need activity-level logic | **C** | May need to handle multiple activities per day |
| 65 | `getEmployeeByNameAndEmpidForTimesheet` | Native: Employee lookup - may need timesheet context adjustment | **C** | May need to adjust for activity-level context |
| 66 | `getClientSideIdByProjectId` | Native: Client side ID lookup | **C** | May need adjustment if client side ID moved to activity level |
| 67 | `getClientSideIdByProjectIdAndEmpId` | Native: Client side ID lookup with employee | **C** | May need adjustment if client side ID moved to activity level |
| 68 | `fetchEmploymentIdByEmpId` | Native: Employment ID lookup | **C** | May need timesheet context adjustment |
| 69 | `getEmployeesWithClientId` | Native: Employee list with client ID filter | **C** | May need activity-level filtering |
| 70 | `getTimesheetForEmployee` | Native: Timesheet retrieval - may need activity grouping | **C** | May need to group by activity |
| 71 | `getDocumentsByEmpAndDate` | Native: Document retrieval - may need activity context | **C** | May need activity-level document filtering |
| 72 | `getEmployeeTimesheetsByProject` | Native: Timesheet list by project - needs activity aggregation | **C** | **HIGH RISK**: Needs activity-level aggregation |
| 73 | `getAllEmployeeDSROfRM` | Native: DSR report - complex aggregation | **C** | **HIGH RISK**: DSR calculation needs activity-level rewrite |
| 74 | `getProjectViewForClientAttendanceStatus` | Native: 700+ line CTE - project view | **C** | **CRITICAL**: Already listed but critical enough to emphasize |
| 75 | `getEmployeeViewForClientAttendanceStatus` | Native: 700+ line CTE - employee view | **C** | **CRITICAL**: Already listed but critical enough to emphasize |
| 76 | `getTimesheetDashboardCountForEmployee` | Native: 400+ line CTE - employee dashboard | **C** | **CRITICAL**: Already listed but critical enough to emphasize |
| 77 | `getTimesheetDashboardCountForProject` | Native: 200+ line CTE - project dashboard | **C** | **CRITICAL**: Already listed but critical enough to emphasize |
| 78 | `getEmployeeTimesheetAsCalender` | Native: 300+ line CTE - calendar view | **C** | **CRITICAL**: Already listed but critical enough to emphasize |
| 79 | `getEmployeeTimesheetAsCalenderByProjectId` | Native: 300+ line CTE - calendar by project | **C** | **CRITICAL**: Already listed but critical enough to emphasize |
| 80 | `getAllEmpTimesheetDashboardCountForProject` | Native: 200+ line CTE - all employees project dashboard | **C** | **CRITICAL**: Already listed but critical enough to emphasize |

---

## QUERIES INVOLVED IN CALCULATIONS (SUM, COUNT, GROUP BY)

### High-Risk Calculation Queries (Must Validate Carefully):

1. **`getTimesheetDashboardCountForEmployee`** - Employee dashboard counts
2. **`getTimesheetDashboardCountForProject`** - Project dashboard counts
3. **`totalIshineFilledCount`** - Filled count calculation
4. **`totalvmsNotFilled`** - Not filled count calculation
5. **`totalIshineNotFilledCount`** - Not filled count with document status
6. **`getTotalVmsFilledCount`** - VMS filled count
7. **`getMyTeamsFilledEodCountByManagerId`** - Manager's team EOD count
8. **`getVmsDocumentApprovalStatusWiseCount`** - Document approval counts
9. **`getEmployeeSummaryReportClientSideApplicable`** - Summary report calculations
10. **`getEmployeeSummaryReportAll`** - All employee summary calculations
11. **`getEmployeeSummaryReportAllEMP`** - All employee summary (variant)
12. **`getAllEmployeeDSROfRM`** - DSR calculation for reporting manager
13. **`getEmployeeTimesheetAsCalender`** - Calendar view with daily aggregations
14. **`getEmployeeTimesheetAsCalenderByProjectId`** - Calendar view by project
15. **`getEmployeeTimesheetAsCalenderForAllEmp`** - Calendar view for all employees
16. **`getEmployeeTimesheetAsCalenderByProjectIdForAllEmp`** - Calendar view for all by project

---

## QUERIES AFFECTING APPROVAL/PAYMENT/COMPLIANCE

### Critical Approval Queries:

1. **`getMyReporteesTimesheetRequests`** - Manager approval queue
2. **`getMyReporteesApprovedTimesheetRequests2`** - Approved requests list
3. **`getMyReporteesApprovedTimesheets`** - Approved timesheets list
4. **`getPendingTimesheetsByEmpAndTeam`** - Pending timesheets for approval
5. **`getMyTimesheetRequests`** - Employee's pending requests
6. **`getRejectedTimesheetIdByEmpAndDateRange`** - Rejected timesheets
7. **`getVmsDocumentApprovalStatusWiseCount`** - Document approval status counts
8. **`getEmployeeViewForClientAttendanceStatus`** - Client attendance status (affects billing)
9. **`getProjectViewForClientAttendanceStatus`** - Project attendance status (affects billing)
10. **`getTimesheetDashboardCountForEmployee`** - Dashboard counts (affects compliance reporting)
11. **`getTimesheetDashboardCountForProject`** - Project dashboard (affects compliance reporting)

---

## HIGH-RISK QUERIES (Must be Validated Carefully)

### Category 1: Status Calculation Queries (Highest Risk)
- `getEmployeeViewForClientAttendanceStatus` - 700+ lines, affects billing
- `getProjectViewForClientAttendanceStatus` - 700+ lines, affects billing
- `getTimesheetDashboardCountForEmployee` - 400+ lines, affects compliance
- `getEmployeeTimesheetAsCalender` - 300+ lines, affects reporting
- `getEmployeeTimesheetAsCalenderByProjectId` - 300+ lines, affects reporting

### Category 2: Aggregation Queries (High Risk)
- `getAllEmployeeDSROfRM` - DSR calculation
- `getEmployeeSummaryReportClientSideApplicable` - Summary reports
- `getEmployeeSummaryReportAll` - Summary reports
- `getTimesheetDashboardCountForProject` - Project dashboard
- `getAllEmpTimesheetDashboardCountForProject` - All employees project dashboard

### Category 3: Approval Workflow Queries (High Risk)
- `getMyReporteesTimesheetRequests` - Approval queue
- `getPendingTimesheetsByEmpAndTeam` - Pending approvals
- `getMyTimesheetRequests` - Employee requests

---

## INTEGRATION PRIORITY ORDER

### Phase 1: Critical Path (Week 1-2)
1. `getMyReporteesTimesheetRequests` - Approval workflow
2. `getPendingTimesheetsByEmpAndTeam` - Approval workflow
3. `getMyTimesheetRequests` - Employee requests
4. `getAllMyTimesheets` - Core listing
5. `getAllMyTeamTimesheets` - Manager view

### Phase 2: Dashboard & Reporting (Week 3-4)
6. `getTimesheetDashboardCountForEmployee` - Employee dashboard
7. `getTimesheetDashboardCountForProject` - Project dashboard
8. `getEmployeeTimesheetAsCalender` - Calendar view
9. `getEmployeeTimesheetAsCalenderByProjectId` - Calendar by project

### Phase 3: Client Attendance & Billing (Week 5-6)
10. `getEmployeeViewForClientAttendanceStatus` - Client attendance (affects billing)
11. `getProjectViewForClientAttendanceStatus` - Project attendance (affects billing)
12. `getEmployeeSummaryReportClientSideApplicable` - Client-side reports

### Phase 4: Summary & Export (Week 7-8)
13. `getEmployeeSummaryReportAll` - Summary reports
14. `getEmployeeSummaryReportAllEMP` - Summary reports (variant)
15. `getAllEmployeeDSROfRM` - DSR reports

### Phase 5: Remaining Queries (Week 9-12)
16. All remaining queries in order of business priority

---

## NOTES

1. **Entity Mapping**: JPQL queries using `Timesheet` entity will work if `EmployeeTimesheetsNew` entity is properly mapped. Verify all field mappings.

2. **Status Calculation**: The new structure has project-level status in `project_timesheet_status_new`. Many queries assume day-level status - these need complete rewrite.

3. **Activity Aggregation**: Queries that aggregate timesheet data need to account for multiple activities per day. Old queries assume 1 timesheet = 1 project.

4. **Document Structure**: Document queries need to account for new document structure in `timesheet_document_details_new` and `final_document_new`.

5. **Testing Priority**: Focus testing on:
   - Approval workflow queries (Phase 1)
   - Dashboard queries (Phase 2)
   - Client attendance queries (Phase 3) - affects billing

---

**Document Version:** 1.0  
**Date:** 2025-01-30  
**Repository:** EmployeeTimesheetsNewRepository  
**Total Queries Analyzed:** 80  
**Status:** Complete Categorization

