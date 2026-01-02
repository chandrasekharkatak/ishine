# Phase 1 - Task 2: Tenth Set of Queries Update (Query 55: getEmployeeViewForClientAttendanceStatus)

## Summary
Updated the complex `getEmployeeViewForClientAttendanceStatus` query (700+ lines) to use new `_new` tables and updated column mappings.

**Date:** 2025-01-30  
**Status:** Completed

---

## Query Updated

### 1. `getEmployeeViewForClientAttendanceStatus` (Query 55)
**Type:** Native Query (Complex CTE - 700+ lines)  
**Status:** Updated

**Changes Made:**

#### 1. `Timesheet_Base_Data` CTE:
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- Added JOINs with `day_type_master_new` and `project_timesheet_status_new`
- Updated SELECT to use:
  - `pts.project_id` (from `project_timesheet_status_new`)
  - `dtm.day_type` (from `day_type_master_new`)
  - `pts.client_in_time`, `pts.client_out_time`, `pts.shadow_emp_id` (from `project_timesheet_status_new`)

#### 2. `Employee_Document_Summary_Details` CTE:
- `timesheet_document_details` → `timesheet_document_details_new`
- Added JOIN with `client_status_master_new` to get status string from status_id
- Updated SELECT to use `csm.status AS client_approval_status`

#### 3. `Expected_Client_Side_Base_DSR` CTE:
- `employee_timesheets` → `employee_timesheets_new`
- Added JOIN with `day_type_master_new` to get day_type string from day_type_id
- Updated subquery to use `UPPER(dtm1.day_type)` instead of `UPPER(et1.day_type)`

#### 4. `Actual_Client_Side_Submissions` CTE:
- Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`
- Changed `timesheet_document_details` → `timesheet_document_details_new`
- Added JOIN with `client_status_master_new` to check approval status

#### 5. `Employee_Document_Summary` CTE:
- Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`
- Changed `timesheet_document_details` → `timesheet_document_details_new`
- Added JOIN with `client_status_master_new` to check approval status

#### 6. `Daily_Status_Details` CTE:
- `employee_timesheets` → `employee_timesheets_new` (in subqueries)
- Added JOINs with `day_type_master_new` and `project_timesheet_status_new`
- Updated `global_ts` JOIN to use `employee_timesheets_new` with `day_type_master_new` for day_type lookup
- Updated SELECT to get `client_in_time`, `client_out_time`, `shadow_emp_id` from `ts_data_relevant` (which comes from `Timesheet_Base_Data` CTE)
- Updated subquery in `doc_pending` to use `timesheet_document_details_new` and `client_status_master_new`

---

## Summary of Table Updates

### Tables Updated:
1. `employee_timesheets` → `employee_timesheets_new`
2. `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
3. `timesheet_document_details` → `timesheet_document_details_new`

### New Master Tables Added:
1. `day_type_master_new` - for `day_type` string values
2. `client_status_master_new` - for `client_approval_status` string values
3. `project_timesheet_status_new` - for project-specific timesheet status fields (`client_in_time`, `client_out_time`, `shadow_emp_id`, `project_id`)

### Column Mapping Changes:
- `et.day_type` (String) → `dtm.day_type` (from `day_type_master_new`)
- `et.client_in_time`, `et.client_out_time`, `et.shadow_emp_id` → `pts.client_in_time`, `pts.client_out_time`, `pts.shadow_emp_id` (from `project_timesheet_status_new`)
- `t.project_id` → `pts.project_id` (from `project_timesheet_status_new`)
- `tdd.client_approval_status` (String) → `csm.status` (from `client_status_master_new`)

---

## Notes

1. **Complex Query**: This is one of the most complex queries (700+ lines) with multiple CTEs and extensive status calculation logic. All CTEs that reference old tables have been updated to use the new `_new` tables and master tables.

2. **Project-Specific Status**: The query now correctly uses `project_timesheet_status_new` to get project-specific status fields (`client_in_time`, `client_out_time`, `shadow_emp_id`, `project_id`) instead of getting them directly from `employee_timesheets_new`.

3. **Master Tables**: All status and day type comparisons now go through master tables to get string values from integer IDs.

4. **CTE Dependencies**: The following CTEs were updated:
   - `Timesheet_Base_Data`
   - `Employee_Document_Summary_Details`
   - `Expected_Client_Side_Base_DSR`
   - `Actual_Client_Side_Submissions`
   - `Employee_Document_Summary`
   - `Daily_Status_Details`

5. **Client Approval Status**: All references to `client_approval_status` now go through `client_status_master_new` to get the status string from the status_id.

---

**Document Version:** 1.0  
**Status:** Completed

