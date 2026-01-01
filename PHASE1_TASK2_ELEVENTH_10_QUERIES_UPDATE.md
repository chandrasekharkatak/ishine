# Phase 1 - Task 2: Eleventh Set of Queries Update (Queries 58, 64, and getTotalEmployeeCount)

## Summary
Updated 2 queries that still referenced old tables:
1. `totalIshineNotFilledCountForAllEmpDash` (Query 58)
2. `getTotalEmployeeCount` (not in categorization but needed update)

Note: `getLastTimesheetFiledByEmpId` (Query 64) was already updated in a previous set.

**Date:** 2025-01-30  
**Status:** Completed

---

## Queries Updated

### 1. `totalIshineNotFilledCountForAllEmpDash` (Query 58)
**Type:** Native Query (Complex CTE)  
**Status:** Updated

**Changes Made:**
- Updated `Document_Summary` CTE:
  - `timesheet_document_details` → `timesheet_document_details_new`
  - Added JOIN with `client_status_master_new` to get status string from status_id
  - Updated COUNT conditions to use `UPPER(csm.status)` instead of `client_approval_status`
  - Updated WHERE clause to use `tdd.created_on` and `tdd.emp_id`

---

### 2. `getTotalEmployeeCount`
**Type:** Native Query (Complex CTE - 500+ lines)  
**Status:** Updated

**Changes Made:**
- Updated `Employee_Timesheets_With_Activities` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs with `day_type_master_new` and `status_master_new`
  - Updated SELECT to use `dtm.day_type`, `sm.status` instead of `et.day_type`, `et.status`

---

## Summary of Table Updates

### Tables Updated:
1. `timesheet_document_details` → `timesheet_document_details_new`
2. `employee_timesheets` → `employee_timesheets_new`
3. `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`

### New Master Tables Added:
1. `client_status_master_new` - for `client_approval_status` string values
2. `day_type_master_new` - for `day_type` string values
3. `status_master_new` - for `status` string values

### Column Mapping Changes:
- `tdd.client_approval_status` (String) → `csm.status` (from `client_status_master_new`)
- `et.day_type` (String) → `dtm.day_type` (from `day_type_master_new`)
- `et.status` (String) → `sm.status` (from `status_master_new`)

---

## Notes

1. **Document Status Aggregation**: The `Document_Summary` CTE in `totalIshineNotFilledCountForAllEmpDash` now correctly uses `client_status_master_new` to get status strings from status_id values.

2. **Status Comparisons**: All status comparisons now use `UPPER(csm.status)` to ensure case-insensitive matching.

3. **Master Tables**: All status and day type lookups now go through master tables to get string values from integer IDs.

---

## Remaining Queries Status

Based on the categorization document and current progress:
- **Completed**: Queries 44-51, 55, 58, 64
- **Remaining**: Queries 52-54, 56-57, 59-63, 65-80 (some may not exist or may have different names)

**Estimated Remaining Sets**: Approximately 2-3 more sets of 10 queries each, depending on which queries actually exist in the repository.

---

**Document Version:** 1.0  
**Status:** Completed

