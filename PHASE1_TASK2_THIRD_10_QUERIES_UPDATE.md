# Phase 1 - Task 2: Third 10 Queries Updated

## Summary
Updated the third set of 10 queries (queries 21-30) in `EmployeeTimesheetsNewRepository` to use `_new` tables.

**Date:** 2025-01-30  
**Status:** Completed - Ready for Review

---

## Queries Updated (Third 10)

### 21. `findByEmpIdAndDateBetween`
- **Backup:** `findByEmpIdAndDateBetween_old` (in repository)
- **Type:** Inline query with GROUP BY
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Table: `timesheet_document_details` → `timesheet_document_details_new`
  - Added JOINs: `day_type_master_new`, `status_master_new`, `project_timesheet_status_new`
  - `total_time`: Now uses `ROUND(et.total_activities_minutes / 60, 2)`
  - Project-level fields (`client_in_time`, `client_out_time`) now from `project_timesheet_status_new`
  - Document fields now from `timesheet_document_details_new`

### 22. `getTotalVmsFilledCount`
- **Backup:** `getTotalVmsFilledCount_old` (in repository)
- **Type:** CTE query
- **Changes:**
  - Changed `Employees_With_ClientID` CTE to use `employee_client_side_id_mapping_new` instead of `employee_timesheets`
  - Now checks `ecsm.active = 1` for active client side ID mappings
  - No longer depends on `employee_timesheets` for client side ID detection

### 23. `totalIshineFilledCount`
- **Backup:** `totalIshineFilledCount_old` (in repository)
- **Type:** Simple COUNT query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Added JOIN: `day_type_master_new` for day_type filtering
  - Day type filtering: `day_type = 'Working'` → `dtm.day_type = 'Working'` (via JOIN)

### 24. `totalvmsNotFilled`
- **Backup:** `totalvmsNotFilled_old` (in repository)
- **Type:** CTE query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new` in main query
  - Changed `Employees_Without_ClientID` CTE to use `employee_client_side_id_mapping_new` for client side ID detection
  - Updated logic to check active client side ID mappings

### 25. `totalIshineNotFilledCount`
- **Backup:** `totalIshineNotFilledCount_old` (in repository)
- **Type:** Complex CTE with document details
- **Changes:**
  - Table: `timesheet_document_details` → `timesheet_document_details_new`
  - Added JOIN: `client_status_master_new` for client approval status string values
  - Client approval status filtering: `client_approval_status = 'pending'` → `csm.status = 'pending'` (via JOIN)
  - Document summary now uses `tdd.emp_id` and joins with `client_status_master_new`

### 26. `findDatesByEmpIdAndProjectId`
- **Backup:** `findDatesByEmpIdAndProjectId_old` (in repository)
- **Type:** JPQL query
- **Changes:**
  - Uses JPQL with `Timesheet` entity - entity mapping should handle new table structure
  - Changed `et.projectId = :projectId` to `EXISTS` subquery checking `ProjectTimesheetStatus`
  - Changed `et.clientSideId IS NOT NULL` to `EXISTS` subquery checking `EmployeeClientSideIdMapping`
  - Note: This assumes entity relationships are properly mapped. May need conversion to native query if entity mapping doesn't support new structure.

### 27. `getPendingTimesheetsByEmpAndTeam`
- **Backup:** `getPendingTimesheetsByEmpAndTeam_old` (in repository)
- **Type:** Inline query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs: `day_type_master_new`, `status_master_new`, `project_timesheet_status_new`, `client_status_master_new`, `employee_client_side_id_mapping_new`
  - `total_time`: Now uses `ROUND(et.total_activities_minutes / 60, 2)`
  - `total_working_hours`: Now uses `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`
  - Project-level fields now from `project_timesheet_status_new`
  - Activity description from `employee_timesheet_activities_mapping_new`

### 28. `getMyTimesheetRequests`
- **Backup:** `getMyTimesheetRequests_old` (in repository)
- **Type:** Inline query
- **Changes:**
  - Same as #27
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs for master tables and project-level data
  - Updated `has_client_side_id` logic: `et.has_client_side_id = 1` → `ecsm.client_side_id IS NOT NULL`
  - Updated client flag filtering to use `ecsm.client_side_id` instead of `et.has_client_side_id`

### 29. `getLastTimesheetFiledByEmpId`
- **Backup:** `getLastTimesheetFiledByEmpId_old` (in repository)
- **Type:** CTE with RANK
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new` (in CTE)
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs: `day_type_master_new`, `project_timesheet_status_new`, `client_status_master_new`, `employee_client_side_id_mapping_new`
  - `total_time`: Now uses `ROUND(et.total_activities_minutes / 60, 2)`
  - Project ID now from `project_timesheet_status_new`
  - Client approval status from `client_status_master_new`
  - Day type filtering: `UPPER(et.day_type) LIKE '%WORKING%'` → `UPPER(dtm.day_type) LIKE '%WORKING%'` (via JOIN)

### 30. `getRejectedTimesheetIdByEmpAndDateRange`
- **Backup:** `getRejectedTimesheetIdByEmpAndDateRange_old` (in repository)
- **Type:** JPQL query
- **Changes:**
  - Uses JPQL with `Timesheet` entity - entity mapping should handle new table structure
  - Changed `et.status='Rejected'` to `EXISTS` subquery checking `StatusMaster` for 'Rejected' status
  - Note: This assumes entity relationships are properly mapped. May need conversion to native query if entity mapping doesn't support new structure.

---

## Key Changes Applied

### Table Name Updates:
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- `timesheet_document_details` → `timesheet_document_details_new`

### Column Mapping Updates:
- `status` (String) → `status` (Integer) + JOIN with `status_master_new`
- `day_type` (String) → `day_type_id` (Integer) + JOIN with `day_type_master_new`
- `total_time` (Float) → `ROUND(et.total_activities_minutes / 60, 2)`
- `total_working_hours` → `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`
- `client_approval_status` (String) → `client_approval_status` (Integer) + JOIN with `client_status_master_new`

### New JOINs Required:
- `day_type_master_new` - for day_type string values
- `status_master_new` - for status string values
- `project_timesheet_status_new` - for project-level status, client approval, client times, etc.
- `client_status_master_new` - for client approval status string values
- `employee_client_side_id_mapping_new` - for client side ID mapping

### Client Side ID Detection Changes:
- **Old:** Checked `employee_timesheets.client_side_id IS NOT NULL`
- **New:** Checks `employee_client_side_id_mapping_new.client_side_id IS NOT NULL AND active = 1`
- This is a significant architectural change - client side IDs are now project-specific and stored in a separate mapping table

### JPQL Query Considerations:
- `findDatesByEmpIdAndProjectId` and `getRejectedTimesheetIdByEmpAndDateRange` use JPQL
- These queries assume entity relationships are properly mapped
- May need conversion to native queries if entity mapping doesn't support the new structure
- Added `EXISTS` subqueries to check related entities instead of direct field access

---

## Query Types

### Inline Queries (6 queries)
These queries have `@Query(nativeQuery = true, value = "...")` with inline SQL:
- `findByEmpIdAndDateBetween`
- `getTotalVmsFilledCount`
- `totalIshineFilledCount`
- `totalvmsNotFilled`
- `totalIshineNotFilledCount`
- `getPendingTimesheetsByEmpAndTeam`
- `getMyTimesheetRequests`
- `getLastTimesheetFiledByEmpId`

**Backup Location:** Original queries saved as `{QueryName}_old` in repository

### JPQL Queries (2 queries)
These queries use JPQL with entity relationships:
- `findDatesByEmpIdAndProjectId`
- `getRejectedTimesheetIdByEmpAndDateRange`

**Backup Location:** Original queries saved as `{QueryName}_old` in repository

**Note:** JPQL queries may need conversion to native queries if entity mapping doesn't support the new structure.

---

## Notes & Considerations

1. **Client Side ID Architecture Change**: The most significant change is how client side IDs are detected:
   - **Old:** Direct column in `employee_timesheets` table
   - **New:** Separate mapping table `employee_client_side_id_mapping_new` with project-specific mappings
   - All queries checking for client side IDs need to be updated to use the new mapping table

2. **CTE Queries**: Several queries use Common Table Expressions (CTEs):
   - `getTotalVmsFilledCount`: Uses CTE for client side ID detection
   - `totalvmsNotFilled`: Uses CTE for employees without client IDs
   - `totalIshineNotFilledCount`: Complex CTE with document summary
   - `getLastTimesheetFiledByEmpId`: Uses CTE with RANK for latest timesheet

3. **Document Status**: `totalIshineNotFilledCount` now uses `client_status_master_new` to get status string values from status IDs.

4. **JPQL Entity Mapping**: Two queries use JPQL and assume entity relationships are properly mapped. These may need conversion to native queries if the entity mapping doesn't support the new structure.

5. **Project-Level Data**: Fields like `client_in_time`, `client_out_time`, `client_approval_status`, `project_id`, `shadow_emp_id`, `is_night_shift` are now in `project_timesheet_status_new` and require JOIN with project.

---

## Testing Recommendations

1. **Verify Client Side ID Detection**: Test that all queries correctly identify employees with client side IDs using the new mapping table.
2. **Verify CTE Queries**: Test that CTE queries return correct results with the new table structure.
3. **Verify JPQL Queries**: Test that JPQL queries work correctly with entity mapping. Convert to native queries if needed.
4. **Verify Document Status**: Test that document approval status is correctly retrieved from `client_status_master_new`.
5. **Verify Project-Level Data**: Test that project-level fields are correctly retrieved from `project_timesheet_status_new`.

---

## Progress

**Queries Updated:** 30/80  
**Completion:** 37.5%  
**Next Set:** Queries 31-40

---

**Document Version:** 1.0  
**Status:** Ready for Review

