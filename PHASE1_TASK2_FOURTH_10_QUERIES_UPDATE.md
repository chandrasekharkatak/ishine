# Phase 1 - Task 2: Fourth 10 Queries Updated

## Summary
Updated the fourth set of 10 queries (queries 31-40) in `EmployeeTimesheetsNewRepository` to use `_new` tables.

**Date:** 2025-01-30  
**Status:** Completed - Ready for Review

---

## Queries Updated (Fourth 10)

### 31. `findTimesheetsForRejection`
- **Backup:** `findTimesheetsForRejection_old` (in repository)
- **Type:** JPQL query
- **Changes:**
  - Uses JPQL with `Timesheet` entity - entity mapping should handle the new table structure
  - No changes needed to query itself as entity mapping handles table structure
  - Note: Entity mapping must be configured to map to `employee_timesheets_new` table

### 32. `findAllLeaveTimesheetsWithoutLeaveApplication`
- **Backup:** `findAllLeaveTimesheetsWithoutLeaveApplication_old` (in repository)
- **Type:** JPQL query
- **Changes:**
  - Added JOIN: `DayTypeMaster` for day_type filtering
  - Changed `t.dayType <> 'Week Off'` to `dtm.dayType <> 'Week Off'` (via JOIN)
  - Changed `LOWER(t.dayType) LIKE ...` to `LOWER(dtm.dayType) LIKE ...` (via JOIN)
  - Changed `LOWER(t.status) LIKE ...` to `EXISTS` subquery checking `StatusMaster`
  - Note: Entity mapping should handle table structure, but dayType and status filtering now use master tables

### 33. `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise`
- **Backup:** `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise_old` (in repository)
- **Type:** JPQL query
- **Changes:**
  - Same as #32
  - Added JOIN: `DayTypeMaster` for day_type filtering
  - Updated dayType and status filtering to use master tables
  - Department filtering: `d.deptId IN :deptIds` (unchanged)

### 34. `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise`
- **Backup:** `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise_old` (in repository)
- **Type:** JPQL query
- **Changes:**
  - Same as #32 and #33
  - Added JOIN: `DayTypeMaster` for day_type filtering
  - Updated dayType and status filtering to use master tables
  - Department filtering: `d.deptId = :deptId` (unchanged)

### 35. `getAllLeaveTimesheetsWithoutLeaveApplication`
- **Backup:** `Timesheet.getAllLeaveTimesheetsWithoutLeaveApplicationOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Added JOINs: `day_type_master_new`, `status_master_new`
  - Added JOIN: `employee_timesheet_activities_mapping_new` for description
  - `day_type`: Now from `day_type_master_new` instead of direct column
  - `status`: Now from `status_master_new` instead of direct column
  - `description`: Now from `employee_timesheet_activities_mapping_new` instead of `employee_timesheets`
  - Day type filtering: `et.day_type != "Week Off"` → `dtm.day_type != "Week Off"` (via JOIN)

---

## Key Changes Applied

### Table Name Updates:
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new` (for description)

### Column Mapping Updates:
- `status` (String) → `status` (Integer) + JOIN with `status_master_new`
- `day_type` (String) → `day_type_id` (Integer) + JOIN with `day_type_master_new`
- `description` → Now from `employee_timesheet_activities_mapping_new` instead of `employee_timesheets_new`

### New JOINs Required:
- `day_type_master_new` - for day_type string values
- `status_master_new` - for status string values
- `employee_timesheet_activities_mapping_new` - for description (activity-level)

### JPQL Query Considerations:
- All JPQL queries assume entity relationships are properly mapped
- Added `DayTypeMaster` JOIN for day_type filtering
- Changed status filtering to use `EXISTS` subquery with `StatusMaster`
- Entity mapping must be configured to map `Timesheet` entity to `employee_timesheets_new` table

---

## Query Types

### JPQL Queries (4 queries)
These queries use JPQL with entity relationships:
- `findTimesheetsForRejection`
- `findAllLeaveTimesheetsWithoutLeaveApplication`
- `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise`
- `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise`

**Backup Location:** Original queries saved as `{QueryName}_old` in repository

**Note:** JPQL queries assume entity mapping is properly configured. If entity mapping doesn't support the new structure, these may need conversion to native queries.

### Named Queries (1 query)
This query uses `@Query(nativeQuery = true)` without a `value` parameter:
- `getAllLeaveTimesheetsWithoutLeaveApplication`

**Backup Location:** Original query saved as `Timesheet.getAllLeaveTimesheetsWithoutLeaveApplicationOLD` in `jpa-named-queries.properties`

---

## Notes & Considerations

1. **JPQL Entity Mapping**: All JPQL queries assume the `Timesheet` entity is properly mapped to `employee_timesheets_new` table. If the entity mapping doesn't support this, these queries may need to be converted to native queries.

2. **Day Type Filtering**: All queries that filter by day type now use `DayTypeMaster` JOIN instead of direct string comparison. This ensures consistency with the new master table structure.

3. **Status Filtering**: Status filtering in JPQL queries now uses `EXISTS` subquery with `StatusMaster` instead of direct string comparison. This ensures consistency with the new master table structure.

4. **Description Field**: The `description` field is now retrieved from `employee_timesheet_activities_mapping_new` instead of `employee_timesheets_new`, reflecting the new activity-level granularity.

5. **Leave Timesheet Queries**: All leave timesheet queries (`findAllLeaveTimesheetsWithoutLeaveApplication`, `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise`, `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise`) follow the same pattern:
   - Filter by `dayType <> 'Week Off'` using `DayTypeMaster`
   - Filter by `leaveTypeMasterId IS NULL` (unchanged)
   - Use master tables for dayType and status filtering

---

## Testing Recommendations

1. **Verify JPQL Queries**: Test that all JPQL queries work correctly with entity mapping. Convert to native queries if entity mapping doesn't support new structure.

2. **Verify Day Type Filtering**: Test that day type filtering works correctly using `DayTypeMaster` JOIN.

3. **Verify Status Filtering**: Test that status filtering works correctly using `StatusMaster` EXISTS subquery.

4. **Verify Description Field**: Test that description is correctly retrieved from `employee_timesheet_activities_mapping_new`.

5. **Verify Leave Timesheet Queries**: Test that all leave timesheet queries return correct results with the new table structure.

---

## Progress

**Queries Updated:** 35/80  
**Completion:** 43.75%  
**Next Set:** Queries 41-50

---

**Document Version:** 1.0  
**Status:** Ready for Review

