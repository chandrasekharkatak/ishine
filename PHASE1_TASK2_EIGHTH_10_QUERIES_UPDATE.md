# Phase 1 - Task 2: Eighth Set of Queries Update (Queries 44-45)

## Summary
Updated queries 44-45 (`getTimesheetDashboardCountForEmployee` and `getTimesheetDashboardCountForProject`) to use new `_new` tables and updated column mappings.

**Date:** 2025-01-30  
**Status:** Completed

---

## Queries Updated

### 1. `getTimesheetDashboardCountForEmployee`
**Type:** Native Query (Complex CTE - 400+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Timesheet_Base_Data` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOIN with `day_type_master_new` to get `day_type` string
  - Added JOIN with `project_timesheet_status_new` to get project-specific status fields (`client_in_time`, `client_out_time`, `shadow_emp_id`, `project_id`)
  - Updated SELECT to use `pts.project_id`, `dtm.day_type`, `pts.client_in_time`, `pts.client_out_time`, `pts.shadow_emp_id`
- Updated `Employee_Document_Summary_Details` CTE:
  - `timesheet_document_details` → `timesheet_document_details_new`
  - Added JOIN with `client_status_master_new` to get `client_approval_status` string
  - Updated SELECT to use `csm.status AS client_approval_status`
- Updated `Expected_Client_Side_Base_DSR` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - Added JOIN with `day_type_master_new` to check day types
  - Updated day type comparison to use `dtm1.day_type`
- Updated `Actual_Client_Side_Submissions` CTE:
  - Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`
- Updated `Employee_Document_Summary` CTE:
  - Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`

**Key Changes:**

```sql
-- Timesheet_Base_Data CTE:
-- Before:
FROM employee_timesheets et
LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id
SELECT et.timesheet_id, et.emp_id, t.project_id, etm.employee_team_map_id,
       et.date, et.day_type, et.client_in_time, et.client_out_time, et.shadow_emp_id

-- After:
FROM employee_timesheets_new et
LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id
LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id
LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id
SELECT et.timesheet_id, et.emp_id, pts.project_id, etm.employee_team_map_id,
       et.date, dtm.day_type, pts.client_in_time, pts.client_out_time, pts.shadow_emp_id
```

```sql
-- Employee_Document_Summary_Details CTE:
-- Before:
FROM timesheet_document_details tdd
SELECT tdd.client_approval_status

-- After:
FROM timesheet_document_details_new tdd
LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id
SELECT csm.status AS client_approval_status
```

---

### 2. `getTimesheetDashboardCountForProject`
**Type:** Native Query (Complex CTE - 200+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Employee_Timesheets_With_Activities` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs with `day_type_master_new` to get `day_type` string
  - Added JOINs with `status_master_new` to get `status` string
  - Updated SELECT to use `dtm.day_type`, `sm.status`
- Updated `Actual_Client_Side_Submissions` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `timesheet_document_details` → `timesheet_document_details_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOIN with `client_status_master_new` to get `client_approval_status` string
  - Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`
- Updated `Document_Summary` CTE:
  - `timesheet_document_details` → `timesheet_document_details_new`
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOIN with `client_status_master_new` to get `client_approval_status` string
  - Updated subquery to use `timesheet_document_details_new` and `client_status_master_new`

**Key Changes:**

```sql
-- Employee_Timesheets_With_Activities CTE:
-- Before:
FROM employee_timesheets et
LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id
SELECT et.emp_id, et.date, et.day_type, et.status

-- After:
FROM employee_timesheets_new et
LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id
LEFT JOIN status_master_new sm ON et.status = sm.status_id
LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id
SELECT et.emp_id, et.date, dtm.day_type, sm.status
```

```sql
-- Actual_Client_Side_Submissions CTE:
-- Before:
FROM employee_timesheets et
INNER JOIN timesheet_document_details tdd ON et.timesheet_id = tdd.timesheet_id
SELECT tdd.client_approval_status

-- After:
FROM employee_timesheets_new et
INNER JOIN timesheet_document_details_new tdd ON et.timesheet_id = tdd.timesheet_id
LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id
SELECT csm.status AS client_approval_status
```

```sql
-- Document_Summary CTE:
-- Before:
FROM timesheet_document_details tdd
INNER JOIN employee_timesheets et ON tdd.timesheet_id = et.timesheet_id
LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id
SELECT COUNT(DISTINCT CASE WHEN upper(tdd.client_approval_status) = 'APPROVED' ...)

-- After:
FROM timesheet_document_details_new tdd
LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id
INNER JOIN employee_timesheets_new et ON tdd.timesheet_id = et.timesheet_id
LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id
SELECT COUNT(DISTINCT CASE WHEN upper(csm.status) = 'APPROVED' ...)
```

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

1. **Complex Queries**: Both queries are very large (200-400+ lines) with multiple CTEs. All CTEs that reference old tables have been updated to use the new `_new` tables and master tables.

2. **Project-Specific Status**: The queries now correctly use `project_timesheet_status_new` to get project-specific status fields (`client_in_time`, `client_out_time`, `shadow_emp_id`, `project_id`) instead of getting them directly from `employee_timesheets_new`.

3. **Master Tables**: All status and day type comparisons now go through master tables to get string values from integer IDs.

4. **CTE Dependencies**: The following CTEs were updated:
   - `Employee_Timesheets_With_Activities`
   - `Timesheet_Base_Data`
   - `Employee_Document_Summary_Details`
   - `Expected_Client_Side_Base_DSR`
   - `Actual_Client_Side_Submissions`
   - `Document_Summary`
   - `Employee_Document_Summary`

---

**Document Version:** 1.0  
**Status:** Completed

