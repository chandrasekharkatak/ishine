# EmployeeRepository Timesheet Queries Migration - Complete

## Summary
This document summarizes all changes made to migrate timesheet-related queries from old tables to new `_new` tables in `EmployeeRepository`.

**Date:** 2025-01-30  
**Status:** ✅ **COMPLETE**

---

## Queries Updated

### Simple Queries (Updated with Master Table JOINs)

1. ✅ **`getTimesheetDataByEmpId`**
   - Updated tables: `employee_timesheets` → `employee_timesheets_new`
   - Updated tables: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
   - Added JOINs: `day_type_master_new`, `status_master_new`
   - Updated columns: `et.day_type` → `dtm.day_type`, `et.status` → `sm.status`
   - Updated column: `et.total_working_hours` → `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`
   - Added project_id JOIN: `AND p.project_id = etam.project_id`

2. ✅ **`getTimesheetDataByProjectId`**
   - Same updates as above

3. ✅ **`getTimesheetDataByTeamName`**
   - Same updates as above

4. ✅ **`getDynamicTimesheetData`**
   - Same updates as above
   - Updated column: `et.total_time` → `ROUND(et.total_activities_minutes / 60, 2)`

5. ✅ **`getTimesheetData`**
   - Same updates as above
   - Updated column: `et.total_time` → `ROUND(et.total_activities_minutes / 60, 2)`

6. ✅ **`getActivityData`**
   - Updated table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
   - Updated column: `etam.completion_time` → `CAST(etam.duration_minutes AS DECIMAL(10,2))/60`
   - Added project_id JOIN: `AND p.project_id = etam.project_id`

7. ✅ **`findEmployeeAndTimesheetDetailsWithoutPagination`**
   - Updated JPQL entity: `Timesheet` → `EmployeeTimesheetsNew`

8. ✅ **`countPendingTimesheetsByEmployeeAndDate`**
   - Updated tables with master table JOINs
   - Updated status and day_type lookups

9. ✅ **`findPendingTimesheetProjectNames`**
   - Updated tables with master table JOINs
   - Updated status and day_type lookups

### Complex CTE Queries

10. ✅ **`getEmployeeViewForClientAttendanceStatus`** (Complex CTE Query)
    
    **CTEs Updated:**
    - ✅ `Expected_Client_Side_Base_DSR` - Updated subquery with `employee_timesheets_new` and `day_type_master_new`
    - ✅ `Actual_Client_Side_Submissions` - Updated to use new tables and `project_timesheet_status_new` for project_id
    - ✅ `Employee_Actual_Working_Days` - Updated EXISTS subquery with new tables
    - ✅ `Missing_Days` - Updated NOT EXISTS subquery with new tables and day_type lookup
    - ✅ `Apmosys_Timesheet_Summary` - Updated with new tables, day_type lookup, and project_id from `project_timesheet_status_new`
    - ✅ `Employee_Document_Summary` - Updated with new tables, client_status lookup, and project_id from `project_timesheet_status_new`

11. ✅ **`getEmployeeViewForAllEmpAttendanceStatus`** (Complex CTE Query)
    
    **CTEs Updated:**
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
- `completion_time` (Float) → `CAST(duration_minutes AS DECIMAL(10,2))/60` (convert minutes to hours)
- `total_time` → `ROUND(total_activities_minutes / 60, 2)`
- `total_working_hours` → `TIME_FORMAT(SEC_TO_TIME(total_working_minutes * 60), '%H:%i')`

### Entity Changes
- `Timesheet` → `EmployeeTimesheetsNew` (in JPQL queries)

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
1. ✅ `getTimesheetDataByEmpId_old`
2. ✅ `getTimesheetDataByProjectId_old`
3. ✅ `getTimesheetDataByTeamName_old`
4. ✅ `getDynamicTimesheetData_old`
5. ✅ `getTimesheetData_old`
6. ✅ `getActivityData_old`
7. ✅ `findEmployeeAndTimesheetDetailsWithoutPagination_old`
8. ✅ `countPendingTimesheetsByEmployeeAndDate_old`
9. ✅ `findPendingTimesheetProjectNames_old`

---

## Verification

- ✅ All active queries use new `_new` tables
- ✅ All backup queries preserved with `_old` suffix
- ✅ Master table JOINs added where needed
- ✅ Column conversions applied correctly
- ✅ Project ID resolved via `project_timesheet_status_new`
- ✅ Complex CTE queries updated

---

## Testing Recommendations

1. ✅ Test all simple queries with new table structure
2. ✅ Test complex CTE queries `getEmployeeViewForClientAttendanceStatus` and `getEmployeeViewForAllEmpAttendanceStatus`
3. ✅ Verify master table JOINs return correct values
4. ✅ Verify project_id resolution works correctly
5. ✅ Verify column conversions (minutes to hours, etc.)
6. ✅ Test with real data

---

**Last Updated:** 2025-01-30  
**Status:** ✅ **MIGRATION COMPLETE**

