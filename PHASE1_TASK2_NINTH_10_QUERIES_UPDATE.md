# Phase 1 - Task 2: Ninth Set of Queries Update (Queries 46-51)

## Summary
Updated queries 46-51 (calendar and dashboard queries) to use new `_new` tables and updated column mappings.

**Date:** 2025-01-30  
**Status:** Completed

---

## Queries Updated

### 1. `getEmployeeTimesheetAsCalenderByProjectId` (Query 46)
**Type:** Native Query (Complex CTE - 300+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Timesheet_Base_Data` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs with `day_type_master_new` and `project_timesheet_status_new`
  - Updated SELECT to use `pts.project_id`, `dtm.day_type`, `pts.client_in_time`, `pts.client_out_time`, `pts.shadow_emp_id`
- Updated `Employee_Document_Summary_Details` CTE:
  - `timesheet_document_details` → `timesheet_document_details_new`
  - Added JOIN with `client_status_master_new`
- Updated `Expected_Client_Side_Base_DSR` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - Added JOIN with `day_type_master_new`
- Updated `Actual_Client_Side_Submissions` CTE:
  - Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`
- Updated `Employee_Document_Summary` CTE:
  - Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`
- Updated `Daily_Status_Details` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - Added JOINs with `day_type_master_new` and `project_timesheet_status_new`
  - Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`

---

### 2. `getEmployeeTimesheetAsCalender` (Query 47)
**Type:** Native Query (Complex CTE - 300+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Timesheet_Base_Data` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs with `day_type_master_new` and `project_timesheet_status_new`
  - Updated SELECT to use `pts.project_id`, `dtm.day_type`, `pts.client_in_time`, `pts.client_out_time`, `pts.shadow_emp_id`
- Updated `Employee_Document_Summary_Details` CTE:
  - `timesheet_document_details` → `timesheet_document_details_new`
  - Added JOIN with `client_status_master_new`
- Updated `Expected_Client_Side_Base_DSR` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - Added JOIN with `day_type_master_new`
- Updated `Actual_Client_Side_Submissions` CTE:
  - Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`
- Updated `Employee_Document_Summary` CTE:
  - Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`
- Updated `Daily_Status_Details` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - Added JOINs with `day_type_master_new` and `project_timesheet_status_new`
  - Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`

---

### 3. `getEmployeeTimesheetAsCalenderForAllEmp` (Query 48)
**Type:** Native Query (Complex CTE - 300+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Employee_Timesheets_With_Activities` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs with `day_type_master_new` and `status_master_new`
  - Updated SELECT to use `dtm.day_type`, `sm.status`

---

### 4. `getEmployeeTimesheetAsCalenderByProjectIdForAllEmp` (Query 49)
**Type:** Native Query (Complex CTE - 300+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Employee_Timesheets_With_Activities` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs with `day_type_master_new` and `status_master_new`
  - Updated SELECT to use `dtm.day_type`, `sm.status`

---

### 5. `getAllEmpTimesheetDashboardCountForProject` (Query 50)
**Type:** Native Query (Complex CTE - 200+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Employee_Timesheet_Statuses` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs with `day_type_master_new` and `status_master_new`
  - Updated SELECT to use `UPPER(dtm.day_type) AS day_type_upper`, `sm.status`

---

### 6. `getTimesheetDashboardCountForAllEmployee` (Query 51)
**Type:** Native Query (Complex CTE - 200+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Employee_Timesheets_With_Activities` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs with `day_type_master_new` and `status_master_new`
  - Updated SELECT to use `dtm.day_type`, `sm.status`

---

## Summary of Table Updates

### Tables Updated:
1. `employee_timesheets` → `employee_timesheets_new`
2. `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
3. `timesheet_document_details` → `timesheet_document_details_new`

### New Master Tables Added:
1. `day_type_master_new` - for `day_type` string values
2. `status_master_new` - for `status` string values
3. `client_status_master_new` - for `client_approval_status` string values
4. `project_timesheet_status_new` - for project-specific timesheet status fields

### Column Mapping Changes:
- `et.day_type` (String) → `dtm.day_type` (from `day_type_master_new`)
- `et.status` (String) → `sm.status` (from `status_master_new`)
- `et.client_in_time`, `et.client_out_time`, `et.shadow_emp_id` → `pts.client_in_time`, `pts.client_out_time`, `pts.shadow_emp_id` (from `project_timesheet_status_new`)
- `t.project_id` → `pts.project_id` (from `project_timesheet_status_new`)
- `tdd.client_approval_status` (String) → `csm.status` (from `client_status_master_new`)

---

## Notes

1. **Complex Calendar Queries**: All calendar queries (46-49) are very large (300+ lines) with multiple CTEs. All CTEs that reference old tables have been updated to use the new `_new` tables and master tables.

2. **Dashboard Queries**: Both dashboard queries (50-51) use similar CTE structures and have been updated consistently.

3. **Project-Specific Status**: The queries now correctly use `project_timesheet_status_new` to get project-specific status fields (`client_in_time`, `client_out_time`, `shadow_emp_id`, `project_id`) instead of getting them directly from `employee_timesheets_new`.

4. **Master Tables**: All status and day type comparisons now go through master tables to get string values from integer IDs.

5. **CTE Dependencies**: The following CTEs were updated across multiple queries:
   - `Employee_Timesheets_With_Activities`
   - `Employee_Timesheet_Statuses`
   - `Timesheet_Base_Data`
   - `Employee_Document_Summary_Details`
   - `Expected_Client_Side_Base_DSR`
   - `Actual_Client_Side_Submissions`
   - `Document_Summary`
   - `Employee_Document_Summary`
   - `Daily_Status_Details`

---

**Document Version:** 1.0  
**Status:** Completed

