# Phase 1 - Task 2: Seventh Set of Queries Update (Queries 55-56)

## Summary
Updated queries 55-56 (`getEmployeeViewForClientAttendanceStatus` and `getProjectByMonthRangeAndEmpId`) to use new `_new` tables and updated column mappings.

**Date:** 2025-01-30  
**Status:** Completed

---

## Queries Updated

### 1. `getEmployeeViewForClientAttendanceStatus`
**Type:** Native Query (Complex CTE - 700+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Base_Project_Employees` CTE:
  - `employee_client_side_id_mapping` → `employee_client_side_id_mapping_new` with `active = 1` check
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
- Updated `Daily_Status_Details` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - Added JOINs with `day_type_master_new` and `project_timesheet_status_new`
  - Updated day type comparisons to use `dtm.day_type`
  - Updated client time fields to use `pts.client_in_time`, `pts.client_out_time`, `pts.shadow_emp_id`
  - Updated subquery in `doc_pending` JOIN to use `timesheet_document_details_new` and `client_status_master_new`

**Key Changes:**

```sql
-- Base_Project_Employees CTE:
-- Before:
LEFT JOIN employee_client_side_id_mapping ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id

-- After:
LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1
```

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

```sql
-- Daily_Status_Details CTE:
-- Before:
LEFT JOIN employee_timesheets global_ts ON bpe.emp_id = global_ts.emp_id AND adir.dt = global_ts.date
WHEN UPPER(global_ts.day_type) LIKE '%WEEK%OFF%' THEN 'WO'

-- After:
LEFT JOIN employee_timesheets_new global_ts ON bpe.emp_id = global_ts.emp_id AND adir.dt = global_ts.date
LEFT JOIN day_type_master_new dtm ON global_ts.day_type_id = dtm.day_type_id
LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = global_ts.timesheet_id
WHEN UPPER(dtm.day_type) LIKE '%WEEK%OFF%' THEN 'WO'
```

---

### 2. `getProjectByMonthRangeAndEmpId`
**Type:** Native Query (CTE - 100+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Base_Project_Employees` CTE:
  - `employee_client_side_id_mapping` → `employee_client_side_id_mapping_new` with `active = 1` check

**Key Changes:**

```sql
-- Base_Project_Employees CTE:
-- Before:
LEFT JOIN employee_client_side_id_mapping ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id

-- After:
LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1
```

---

## Summary of Table Updates

### Tables Updated:
1. `employee_timesheets` → `employee_timesheets_new`
2. `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
3. `timesheet_document_details` → `timesheet_document_details_new`
4. `employee_client_side_id_mapping` → `employee_client_side_id_mapping_new`

### New Master Tables Added:
1. `day_type_master_new` - for `day_type` string values
2. `client_status_master_new` - for `client_approval_status` string values
3. `project_timesheet_status_new` - for project-specific timesheet status fields

### Column Mapping Changes:
- `et.day_type` (String) → `dtm.day_type` (from `day_type_master_new`)
- `et.client_in_time`, `et.client_out_time`, `et.shadow_emp_id` → `pts.client_in_time`, `pts.client_out_time`, `pts.shadow_emp_id` (from `project_timesheet_status_new`)
- `t.project_id` → `pts.project_id` (from `project_timesheet_status_new`)
- `tdd.client_approval_status` (String) → `csm.status` (from `client_status_master_new`)
- `ecsm.active` check added (must be `1`)

---

## Notes

1. **Complex Query**: `getEmployeeViewForClientAttendanceStatus` is a very large query (700+ lines) with multiple CTEs. All CTEs that reference old tables have been updated to use the new `_new` tables and master tables.

2. **Project-Specific Status**: The query now correctly uses `project_timesheet_status_new` to get project-specific status fields (`client_in_time`, `client_out_time`, `shadow_emp_id`, `project_id`) instead of getting them directly from `employee_timesheets_new`.

3. **Master Tables**: All status and day type comparisons now go through master tables to get string values from integer IDs.

4. **Active Flag**: `employee_client_side_id_mapping_new` requires `active = 1` check.

5. **CTE Dependencies**: The following CTEs were updated:
   - `Base_Project_Employees`
   - `Timesheet_Base_Data`
   - `Employee_Document_Summary_Details`
   - `Expected_Client_Side_Base_DSR`
   - `Actual_Client_Side_Submissions`
   - `Employee_Document_Summary`
   - `Daily_Status_Details`

---

**Document Version:** 1.0  
**Status:** Completed

