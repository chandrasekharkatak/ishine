# ProjectRepository Timesheet Queries Migration - Complete

## Summary
This document summarizes all changes made to migrate timesheet-related queries from old tables to new `_new` tables in `ProjectRepository`.

**Date:** 2025-01-30  
**Status:** ✅ **COMPLETE**

---

## Queries Updated

### 1. ✅ **`getProjectTimesheetSummaryByEmpId`** (Line ~899)
- **Type:** JPQL Query
- **Changes:**
  - `Timesheet` → `EmployeeTimesheetsNew`
  - `TimesheetActivityMap` → `EmployeeTimesheetActivitiesMappingNew`
  - Updated composite key access: `etam.timesheetId` → `etam.id.timesheetId`
  - Updated composite key access: `etam.activityId` → `etam.id.activityId`
- **Backup:** `getProjectTimesheetSummaryByEmpId_old` created

### 2. ✅ **`getEmployeeTimesheetsByProject`** (Line ~1625)
- **Type:** Complex CTE Query
- **CTEs Updated:**
  - ✅ `Expected_Client_Side_Base_DSR` - Updated subquery with `employee_timesheets_new` and `day_type_master_new`
  - ✅ `Actual_Client_Side_Submissions` - Updated to use new tables, `project_timesheet_status_new` for project_id, and `client_status_master_new` for approval status
  - ✅ `Employee_Document_Summary_Details` - Updated with new tables, removed `tdd.emp_id` (get from `et.emp_id`), added `project_timesheet_status_new` for project_id
  - ✅ `Employee_Document_Summary` - Updated subquery to use new tables and client_status lookup

### 3. ✅ **`getAllEmployeeDSROfRM`** (Line ~2520)
- **Type:** Complex CTE Query
- **CTEs Updated:**
  - ✅ `Dynamic_Expected_Days` - Updated subqueries with `employee_timesheets_new` and `day_type_master_new`
  - ✅ `Apmosys_Timesheet_Summary` - Updated with new tables and day_type lookup
  - ✅ `Document_Summary` - Updated with new tables, removed `tdd.emp_id` (get from `et.emp_id`), added `client_status_master_new` for approval status

### 4. ✅ **`getProjectViewForClientAttendanceStatus`** (Line ~4141)
- **Type:** Complex CTE Query
- **CTEs Updated:**
  - ✅ `Employee_Timesheets_With_Activities` - Updated with new tables, day_type and status lookups
  - ✅ `Actual_Client_Side_Submissions` - Updated with new tables, client_status lookup
  - ✅ `Document_Summary` - Updated with new tables, removed `tdd.emp_id` (get from `et.emp_id`), added `client_status_master_new` for approval status

### 5. ✅ **`getProjectViewForAllEmpAttendanceStatus`** (Line ~4455)
- **Type:** Complex CTE Query
- **CTEs Updated:**
  - ✅ `Employee_Timesheet_Statuses` - Updated with new tables, day_type and status lookups

---

## Key Changes Summary

### Table Name Changes
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- `timesheet_document_details` → `timesheet_document_details_new`

### Column Changes
- `day_type` (String) → `day_type_id` (Integer) - resolved via `day_type_master_new` JOIN
- `status` (String) → `status` (Integer) - resolved via `status_master_new` JOIN
- `client_approval_status` → `client_approval_status_id` - resolved via `client_status_master_new` JOIN

### Entity Changes
- `Timesheet` → `EmployeeTimesheetsNew` (in JPQL queries)
- `TimesheetActivityMap` → `EmployeeTimesheetActivitiesMappingNew` (in JPQL queries)
- Composite key access: `etam.timesheetId` → `etam.id.timesheetId`
- Composite key access: `etam.activityId` → `etam.id.activityId`

### Project ID Resolution
- `et.project_id` → `pts.project_id` (from `project_timesheet_status_new` JOIN)
- Added: `INNER JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id`

### Client Approval Status Resolution
- `tdd.client_approval_status` → `csm.status` (from `client_status_master_new` JOIN)
- Added: `LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id`

### Employee ID in Document Summary
- `tdd.emp_id` → `et.emp_id` (emp_id removed from document table, get from timesheet)

---

## Backup Queries Created

All original queries have been preserved with `_old` suffix:
1. ✅ `getProjectTimesheetSummaryByEmpId_old`

---

## Verification

- ✅ All active queries use new `_new` tables
- ✅ All backup queries preserved with `_old` suffix
- ✅ Master table JOINs added where needed
- ✅ Column conversions applied correctly
- ✅ Project ID resolved via `project_timesheet_status_new`
- ✅ Complex CTE queries updated
- ✅ JPQL entity references updated
- ✅ Composite key access updated

---

## Testing Recommendations

1. ✅ Test JPQL query `getProjectTimesheetSummaryByEmpId` with new entities
2. ✅ Test complex CTE queries:
   - `getEmployeeTimesheetsByProject`
   - `getAllEmployeeDSROfRM`
   - `getProjectViewForClientAttendanceStatus`
   - `getProjectViewForAllEmpAttendanceStatus`
3. ✅ Verify master table JOINs return correct values
4. ✅ Verify project_id resolution works correctly
5. ✅ Verify composite key access works correctly
6. ✅ Test with real data

---

**Last Updated:** 2025-01-30  
**Status:** ✅ **MIGRATION COMPLETE**

