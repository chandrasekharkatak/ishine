# Phase 1 - Task 2: Fifth Set of 10 Queries Update (Queries 41-50)

## Summary
Updated queries 41-50 (actually queries 36-40 from previous set plus new queries) to use new `_new` tables and updated column mappings.

**Date:** 2025-01-30  
**Status:** Completed

---

## Queries Updated

### 1. `totalIshineNotFilledCountForAllEmpDash`
**Type:** Native Query (CTE)  
**Changes:**
- Updated `timesheet_document_details` → `timesheet_document_details_new`
- Added JOIN with `client_status_master_new` to get status string from `client_approval_status_id`
- Updated status filtering to use `csm.status` instead of direct string comparison

**Key Changes:**
```sql
-- Before:
FROM timesheet_document_details
WHERE client_approval_status = 'pending'

-- After:
FROM timesheet_document_details_new tdd
LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id
WHERE csm.status = 'pending'
```

---

### 2. `getEmployeeSummaryReportClientSideApplicable`
**Type:** Native Query (Complex CTE - 250+ lines)  
**Changes:**
- Updated `employee_client_side_id_mapping` → `employee_client_side_id_mapping_new` with `active = 1` check
- Updated `employee_timesheets` → `employee_timesheets_new` in multiple CTEs
- Updated `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- Updated `timesheet_document_details` → `timesheet_document_details_new`
- Added JOINs with `day_type_master_new` to get `day_type` string
- Added JOINs with `client_status_master_new` to get `client_approval_status` string
- Added JOINs with `project_timesheet_status_new` to get project-level fields (`client_in_time`, `client_out_time`, `shadow_emp_id`, `project_id`)

**Key Changes in `Timesheet_Base_Data` CTE:**
```sql
-- Before:
FROM employee_timesheets et
LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id
SELECT et.day_type, et.client_in_time, et.client_out_time, et.shadow_emp_id, t.project_id

-- After:
FROM employee_timesheets_new et
LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id
LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id
LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id
SELECT dtm.day_type, pts.client_in_time, pts.client_out_time, pts.shadow_emp_id, pts.project_id
```

**Key Changes in `Employee_Document_Summary_Details` CTE:**
```sql
-- Before:
FROM timesheet_document_details tdd
SELECT tdd.client_approval_status

-- After:
FROM timesheet_document_details_new tdd
LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id
SELECT csm.status AS client_approval_status
```

**Key Changes in `Expected_Client_Side_Base_DSR` CTE:**
```sql
-- Before:
SELECT 1 FROM employee_timesheets et1 WHERE et1.emp_id = bpe.emp_id AND adir.dt = et1.date
AND UPPER(et1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY', 'WEEK OFF')

-- After:
SELECT 1 FROM employee_timesheets_new et1
LEFT JOIN day_type_master_new dtm1 ON et1.day_type_id = dtm1.day_type_id
WHERE et1.emp_id = bpe.emp_id AND adir.dt = et1.date
AND UPPER(dtm1.day_type) IN ('LEAVE', 'CLIENT HOLIDAY', 'PUBLIC HOLIDAY', 'WEEK OFF')
```

**Key Changes in `Daily_Status_Details` CTE:**
```sql
-- Before:
LEFT JOIN employee_timesheets global_ts ON bpe.emp_id = global_ts.emp_id AND adir.dt = global_ts.date
WHEN UPPER(global_ts.day_type) LIKE '%WEEK%OFF%' THEN 'WO'

-- After:
LEFT JOIN employee_timesheets_new global_ts ON bpe.emp_id = global_ts.emp_id AND adir.dt = global_ts.date
LEFT JOIN day_type_master_new dtm ON global_ts.day_type_id = dtm.day_type_id
LEFT JOIN project_timesheet_status_new pts ON pts.timesheet_id = global_ts.timesheet_id
WHEN UPPER(dtm.day_type) LIKE '%WEEK%OFF%' THEN 'WO'
SELECT pts.client_in_time, pts.client_out_time, pts.shadow_emp_id
```

**Key Changes in `Employee_Document_Summary` CTE:**
```sql
-- Before:
NOT EXISTS (SELECT 1 FROM timesheet_document_details WHERE timesheet_id = edsd.timesheet_id AND UPPER(client_approval_status) = 'APPROVED')

-- After:
NOT EXISTS (SELECT 1 FROM timesheet_document_details_new tdd2 
LEFT JOIN client_status_master_new csm2 ON tdd2.client_approval_status_id = csm2.status_id 
WHERE tdd2.timesheet_id = edsd.timesheet_id AND UPPER(csm2.status) = 'APPROVED')
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
3. `project_timesheet_status_new` - for project-level timesheet fields

### Column Mapping Changes:
- `et.day_type` (String) → `dtm.day_type` (from `day_type_master_new`)
- `et.client_in_time`, `et.client_out_time`, `et.shadow_emp_id` → `pts.client_in_time`, `pts.client_out_time`, `pts.shadow_emp_id` (from `project_timesheet_status_new`)
- `et.project_id` → `pts.project_id` (from `project_timesheet_status_new`)
- `tdd.client_approval_status` (String) → `csm.status` (from `client_status_master_new`)
- `ecsm.active` check added (must be `1`)

---

## Notes

1. **Complex Query**: `getEmployeeSummaryReportClientSideApplicable` is a very large query (250+ lines) with multiple CTEs. All table references have been updated to use `_new` tables.

2. **Composite Key Access**: `project_timesheet_status_new` uses composite key (`timesheet_id`, `project_id`), so JOINs must include both conditions: `pts.timesheet_id = et.timesheet_id AND pts.project_id = etam.project_id`.

3. **Status Master Tables**: All status and day type comparisons now go through master tables to get string values from integer IDs.

4. **Active Flag**: `employee_client_side_id_mapping_new` requires `active = 1` check.

---

**Document Version:** 1.0  
**Status:** Completed

