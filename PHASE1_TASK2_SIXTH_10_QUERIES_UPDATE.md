# Phase 1 - Task 2: Sixth Set of 10 Queries Update (Queries 51-60)

## Summary
Updated queries 51-60 (actually queries 44-45 from categorization) to use new `_new` tables and updated column mappings.

**Date:** 2025-01-30  
**Status:** Completed

---

## Queries Updated

### 1. `getEmployeeSummaryReportAll`
**Type:** Native Query (Complex CTE - 200+ lines)  
**Changes:**
- Updated `Employee_Timesheets_With_Activities` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs with `day_type_master_new` to get `day_type` string
  - Added JOINs with `status_master_new` to get `status` string
- Updated `Base_Report_Details` CTE:
  - `employee_client_side_id_mapping` → `employee_client_side_id_mapping_new` with `active = 1` check

**Key Changes in `Employee_Timesheets_With_Activities` CTE:**
```sql
-- Before:
FROM employee_timesheets et
LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id
SELECT et.day_type, et.status

-- After:
FROM employee_timesheets_new et
LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id
LEFT JOIN status_master_new sm ON et.status = sm.status_id
LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id
SELECT dtm.day_type, sm.status
```

**Key Changes in `Base_Report_Details` CTE:**
```sql
-- Before:
LEFT JOIN employee_client_side_id_mapping ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id

-- After:
LEFT JOIN employee_client_side_id_mapping_new ecsm ON e.emp_id = ecsm.emp_id AND ecsm.project_id = t.project_id AND ecsm.active = 1
```

---

### 2. `getEmployeeSummaryReportAllEMP`
**Type:** Native Query (Complex CTE - 300+ lines)  
**Changes:**
- Updated `Employee_Timesheets_With_Activities` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs with `day_type_master_new` to get `day_type` string
  - Added JOINs with `status_master_new` to get `status` string
- Updated `Base_Report_Details` CTE:
  - `employee_client_side_id_mapping` → `employee_client_side_id_mapping_new` with `active = 1` check

**Key Changes:**
Same as `getEmployeeSummaryReportAll` - both queries use the same CTE structure.

---

## Summary of Table Updates

### Tables Updated:
1. `employee_timesheets` → `employee_timesheets_new`
2. `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
3. `employee_client_side_id_mapping` → `employee_client_side_id_mapping_new`

### New Master Tables Added:
1. `day_type_master_new` - for `day_type` string values
2. `status_master_new` - for `status` string values

### Column Mapping Changes:
- `et.day_type` (String) → `dtm.day_type` (from `day_type_master_new`)
- `et.status` (String) → `sm.status` (from `status_master_new`)
- `ecsm.active` check added (must be `1`)

---

## Notes

1. **Complex Queries**: Both `getEmployeeSummaryReportAll` and `getEmployeeSummaryReportAllEMP` are very large queries (200-300+ lines) with multiple CTEs. The `Employee_Timesheets_With_Activities` CTE is used by multiple other CTEs in these queries, so updating it ensures all dependent CTEs work correctly.

2. **Status Master Tables**: All status and day type comparisons now go through master tables to get string values from integer IDs.

3. **Active Flag**: `employee_client_side_id_mapping_new` requires `active = 1` check.

4. **CTE Dependencies**: The `Employee_Timesheets_With_Activities` CTE is used by:
   - `Daily_Status_Details`
   - `Expected_Working_Days_Detail`
   - `Actual_Timesheet_Filled`
   - `Ishine_Timesheet_Summary`
   
   All of these CTEs now correctly use the updated `Employee_Timesheets_With_Activities` CTE with new tables.

---

**Document Version:** 1.0  
**Status:** Completed

