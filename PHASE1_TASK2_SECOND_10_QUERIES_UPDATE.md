# Phase 1 - Task 2: Second 10 Queries Updated

## Summary
Updated the second set of 10 queries (queries 11-20) in `EmployeeTimesheetsNewRepository` to use `_new` tables.

**Date:** 2025-01-30  
**Status:** Completed - Ready for Review

---

## Queries Updated (Second 10)

### 11. `getAllMyTeamTimesheets`
- **Backup:** `Timesheet.getAllMyTeamTimesheetsOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Added JOINs: `day_type_master_new`, `status_master_new`, `project_timesheet_status_new`, `client_status_master_new`, `employee_client_side_id_mapping_new`, `timesheet_rejection_details_new`
  - `total_time`: Now uses `ROUND(et.total_activities_minutes / 60, 2)`
  - `total_working_hours`: Now uses `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`
  - Removed: `description`, `remarks` (not in new structure)
  - Rejection reason: Now from `timesheet_rejection_details_new` instead of `timesheet_rejection_reasons_master`

### 12. `getAllMyTimesheets`
- **Backup:** `Timesheet.getAllMyTimesheetsOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Same as #11
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Added JOINs for master tables and project-level data
  - Updated `total_time` and `total_working_hours` calculations

### 13. `findTimesheetOnLeaveDate`
- **Backup:** `Timesheet.findTimesheetOnLeaveDateOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Simple query - only table name change needed
  - Fixed date column reference: `date` → `et.date`

### 14. `getInactiveActivitiesByTimesheetId`
- **Backup:** `Timesheet.getInactiveActivitiesByTimesheetIdOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Return value: Changed from `timesheet_activity_map_id` to composite key string `CONCAT(timesheet_id, '_', activity_id, '_', project_id)`
  - Note: Composite key now requires concatenation for backward compatibility

### 15. `getMyTeamsFilledEodCountByManagerId`
- **Backup:** `Timesheet.getMyTeamsFilledEodCountByManagerIdOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Fixed date column reference: `date` → `et.date`
  - Simple COUNT query - only table name change needed

### 16. `getTimesheetFilledByMember`
- **Backup:** `Timesheet.getTimesheetFilledByMemberOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Simple query - only table name change needed

### 17. `getAllEmployeeTimesheetsBetweenDates`
- **Backup:** `getAllEmployeeTimesheetsBetweenDates_old` (in repository)
- **Type:** Inline query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs: `day_type_master_new`, `status_master_new`, `project_timesheet_status_new`
  - `total_time`: Now uses `ROUND(et.total_activities_minutes / 60, 2)`
  - Removed: `remarks`, `timesheet_activity_map_id` (composite key now)
  - Project-level fields now from `project_timesheet_status_new`
  - Activity description from `employee_timesheet_activities_mapping_new`

### 18. `getTimesheetsByDepartmentAndDateRange`
- **Backup:** `getTimesheetsByDepartmentAndDateRange_old` (in repository)
- **Type:** Inline query
- **Changes:**
  - Same as #17
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs for master tables and project-level data
  - Updated `total_time` calculation

### 19. `getLastFilledTimesheet`
- **Backup:** `getLastFilledTimesheet_old` (in repository)
- **Type:** Inline query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new` (in main query and subquery)
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOIN: `day_type_master_new` for day_type filtering in subquery
  - Added JOIN: `project_timesheet_status_new` for project-level data
  - `total_working_hours`: Now uses `TIME_FORMAT(SEC_TO_TIME(ets.total_working_minutes * 60), '%H:%i')`
  - `completion_time`: Changed from `completion_time` to `CAST(etam.duration_minutes AS DECIMAL(10,2))/60`
  - Removed: `description` (timesheet description not in new structure)
  - Project ID now from `project_timesheet_status_new`

### 20. `checkEmployeeActiveOrNot`
- **Backup:** `checkEmployeeActiveOrNot_old` (in repository)
- **Type:** Inline query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new` (in main query and subquery)
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOIN: `day_type_master_new` for day_type filtering in subquery
  - Day type filtering: `day_type = 'Working'` → `dtm.day_type = 'Working'` (via JOIN)

---

## Key Changes Applied

### Table Name Updates:
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`

### Column Mapping Updates:
- `status` (String) → `status` (Integer) + JOIN with `status_master_new`
- `day_type` (String) → `day_type_id` (Integer) + JOIN with `day_type_master_new`
- `total_time` (Float) → `ROUND(et.total_activities_minutes / 60, 2)`
- `total_working_hours` → `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`
- `completion_time` → `CAST(etam.duration_minutes AS DECIMAL(10,2))/60`

### New JOINs Required:
- `day_type_master_new` - for day_type string values
- `status_master_new` - for status string values
- `project_timesheet_status_new` - for project-level status, client approval, client times, etc.
- `client_status_master_new` - for client approval status string values
- `employee_client_side_id_mapping_new` - for client side ID mapping
- `timesheet_rejection_details_new` - for rejection reasons (replaces `timesheet_rejection_reasons_master`)

### Columns Removed (Not in New Structure):
- `description` - not in `employee_timesheets_new` (may be in activities mapping)
- `remarks` - not in `employee_timesheets_new` (now in `timesheet_rejection_details_new`)

### Composite Key Handling:
- `employee_timesheet_activities_mapping_new`: Composite key (timesheet_id, activity_id, project_id)
  - For `getInactiveActivitiesByTimesheetId`: Return as concatenated string for backward compatibility
- `project_timesheet_status_new`: Composite key (timesheet_id, project_id)
- `timesheet_rejection_details_new`: Composite key (timesheet_id, project_id, rejection_id)

---

## Query Types

### Named Queries (6 queries)
These queries use `@Query(nativeQuery = true)` without a `value` parameter:
- `getAllMyTeamTimesheets`
- `getAllMyTimesheets`
- `findTimesheetOnLeaveDate`
- `getInactiveActivitiesByTimesheetId`
- `getMyTeamsFilledEodCountByManagerId`
- `getTimesheetFilledByMember`

**Backup Location:** Original queries saved as `Timesheet.{QueryName}OLD` in `jpa-named-queries.properties`

### Inline Queries (4 queries)
These queries have `@Query(nativeQuery = true, value = "...")` with inline SQL:
- `getAllEmployeeTimesheetsBetweenDates`
- `getTimesheetsByDepartmentAndDateRange`
- `getLastFilledTimesheet`
- `checkEmployeeActiveOrNot`

**Backup Location:** Original queries saved as `{QueryName}_old` in repository

---

## Notes & Considerations

1. **Rejection Reasons**: Changed from `timesheet_rejection_reasons_master` (single rejection_id) to `timesheet_rejection_details_new` (composite key with project_id). Rejection reason now comes from `trd.remarks` field.

2. **Composite Keys**: `getInactiveActivitiesByTimesheetId` now returns a concatenated string representation of the composite key for backward compatibility.

3. **Project-Level Data**: Fields like `client_in_time`, `client_out_time`, `client_approval_status`, `project_id`, `shadow_emp_id`, `is_night_shift` are now in `project_timesheet_status_new` and require JOIN with project.

4. **Activity Description**: Now comes from `employee_timesheet_activities_mapping_new.description` instead of separate field.

5. **Subquery Updates**: Queries with subqueries (`getLastFilledTimesheet`, `checkEmployeeActiveOrNot`) need to update both main query and subquery.

---

## Testing Recommendations

1. **Verify Composite Keys**: Test `getInactiveActivitiesByTimesheetId` returns correct format.
2. **Verify Rejection Reasons**: Test that rejection reasons are correctly retrieved from new table.
3. **Verify Project-Level Data**: Test that client approval status, client times, etc. are correctly retrieved.
4. **Verify Date Filtering**: Test that date range filtering works correctly with `et.date` references.
5. **Verify Activity Descriptions**: Test that activity descriptions are correctly retrieved.

---

## Progress

**Queries Updated:** 20/80  
**Completion:** 25%  
**Next Set:** Queries 21-30

---

**Document Version:** 1.0  
**Status:** Ready for Review

