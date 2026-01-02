# Phase 1 - Task 2: First 10 Queries Updated

## Summary
Updated the first 10 queries in `EmployeeTimesheetsNewRepository` to use `_new` tables.

**Important:** Queries that use named queries from `jpa-named-queries.properties` have been updated in the properties file, not as inline queries.

**Date:** 2025-01-30  
**Status:** Completed - Ready for Review

---

## Queries Updated (First 10)

### 1. `getTimesheetDataByEmpIdAndDate`
- **Backup:** `getTimesheetDataByEmpIdAndDate_old` (in repository)
- **Type:** Inline query (has `value` parameter)
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Added JOIN with `status_master_new` to get status string
  - Status now retrieved as string from master table

### 2. `getMyReporteesTimesheetRequests`
- **Backup:** `Timesheet.getMyReporteesTimesheetRequestsOLD` (in jpa-named-queries.properties)
- **Type:** Named query (no `value` parameter - uses properties file)
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Added JOINs: `day_type_master_new`, `status_master_new`, `project_timesheet_status_new`, `client_status_master_new`, `employee_client_side_id_mapping_new`
  - Converted `total_time` → `total_working_minutes / 60`
  - Converted `day_type` (string) → `day_type_id` with JOIN
  - Converted `status` (string) → `status` (Integer) with JOIN
  - `current_manager_id` → `e.manager_id` (from employee table)
  - Project-level fields now from `project_timesheet_status_new`
  - Client side ID from `employee_client_side_id_mapping_new`

### 3. `getMyReporteesApprovedTimesheetRequests2`
- **Backup:** `Timesheet.getMyReporteesApprovedTimesheetRequests2OLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Same as #2
  - Added support for `:empIds` parameter filtering
  - Status filtering via JOIN with `status_master_new`

### 4. `countMyReporteesTimesheetRequests`
- **Backup:** `Timesheet.countMyReporteesTimesheetRequestsOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Added JOIN with `status_master_new` for status filtering
  - Status comparison: `et.status = 'Pending'` → `sm.status = 'Pending'`
  - `current_manager_id` → `e.manager_id`

### 5. `getMyReporteesApprovedTimesheets`
- **Backup:** `Timesheet.getMyReporteesApprovedTimesheetsOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Added JOINs: `day_type_master_new`, `status_master_new`, `project_timesheet_status_new`
  - Converted `total_time` → `total_working_minutes / 60`
  - Status filtering: `et.status != 'Pending'` → `sm.status != 'Pending'`
  - Note: `description` and `remarks` columns removed (not in new structure)

### 6. `getLast7DaysTimesheetsByEmpId`
- **Backup:** `Timesheet.getLast7DaysTimesheetsByEmpIdOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Simple query - only table name change needed

### 7. `getTimesheetsForHomePageByEmpId`
- **Backup:** `Timesheet.getTimesheetsForHomePageByEmpIdOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added JOINs: `day_type_master_new`, `status_master_new`
  - `completion_time` → `duration_minutes` (in activities mapping)
  - Converted duration from minutes to hours: `SUM(duration_minutes) / 60.0`
  - Note: `description` column removed (not in new structure)

### 8. `getAllTimesheetData`
- **Backup:** `Timesheet.getAllTimesheetDataOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Added JOINs: `day_type_master_new`, `status_master_new`
  - Converted `total_time` → `total_working_minutes / 60`
  - `timesheet_status_updated_by` → `updated_by` (column name change)
  - Note: `description` column removed (not in new structure)

### 9. `getLast9DaysPendingTimesheetReport`
- **Backup:** `Timesheet.getLast9DaysPendingTimesheetReportOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Simple COUNT query - only table name change needed

### 10. `getLast9DaysFilledTimesheetReport`
- **Backup:** `Timesheet.getLast9DaysFilledTimesheetReportOLD` (in jpa-named-queries.properties)
- **Type:** Named query
- **Changes:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Added JOINs: `day_type_master_new`, `status_master_new`
  - Converted `total_time` → `total_working_minutes / 60`
  - Status now from `status_master_new` JOIN

---

## Key Changes Applied

### Table Name Updates:
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- `timesheet_document_details` → `timesheet_document_details_new` (for future queries)

### Column Mapping Updates:
- `status` (String) → `status` (Integer) + JOIN with `status_master_new`
- `day_type` (String) → `day_type_id` (Integer) + JOIN with `day_type_master_new`
- `total_time` (Float) → `total_working_minutes` (Integer) / 60
- `total_working_hours` → `total_working_minutes / 60`
- `completion_time` → `duration_minutes` (in activities mapping)
- `current_manager_id` → `e.manager_id` (from employee table)

### New JOINs Required:
- `day_type_master_new` - for day_type string values
- `status_master_new` - for status string values
- `project_timesheet_status_new` - for project-level status, client approval, client times, etc.
- `client_status_master_new` - for client approval status string values
- `employee_client_side_id_mapping_new` - for client side ID mapping

### Columns Removed (Not in New Structure):
- `description` - not in `employee_timesheets_new` (may be in activities mapping)
- `remarks` - not in `employee_timesheets_new`
- `current_manager_id` - use `e.manager_id` from employee table instead

### Composite Key Handling:
- `project_timesheet_status_new`: Use `pts.timesheet_id` and `pts.project_id` directly
- `employee_client_side_id_mapping_new`: Use `ecsm.emp_id`, `ecsm.client_side_id`, `ecsm.project_id` directly
- `employee_timesheet_activities_mapping_new`: Use `etam.timesheet_id`, `etam.activity_id`, `etam.project_id` directly

---

## Query Types

### Named Queries (9 queries)
These queries use `@Query(nativeQuery = true)` without a `value` parameter, meaning they reference named queries from `jpa-named-queries.properties`:
- `getMyReporteesTimesheetRequests`
- `getMyReporteesApprovedTimesheetRequests2`
- `countMyReporteesTimesheetRequests`
- `getMyReporteesApprovedTimesheets`
- `getLast7DaysTimesheetsByEmpId`
- `getTimesheetsForHomePageByEmpId`
- `getAllTimesheetData`
- `getLast9DaysPendingTimesheetReport`
- `getLast9DaysFilledTimesheetReport`

**Backup Location:** Original queries saved as `Timesheet.{QueryName}OLD` in `jpa-named-queries.properties`

### Inline Queries (1 query)
This query has `@Query(nativeQuery = true, value = "...")` with inline SQL:
- `getTimesheetDataByEmpIdAndDate`

**Backup Location:** Original query saved as `getTimesheetDataByEmpIdAndDate_old` in repository

## Notes & Considerations

1. **Multiple Projects Per Day**: The new structure allows multiple projects per day. Queries may return multiple rows per timesheet if not properly grouped.

2. **Status as Integer**: All status comparisons now use JOIN with `status_master_new` to get string values for filtering.

3. **Day Type as Integer**: All day_type references now use JOIN with `day_type_master_new`.

4. **Missing Columns**: Some columns like `description` and `remarks` are not in the new structure. These have been removed from SELECT clauses. If needed, they may be available in activities mapping or need to be added to the new structure.

5. **Client Side ID**: Now comes from `employee_client_side_id_mapping_new` table, which requires JOIN with project.

6. **Project-Level Data**: Fields like `client_in_time`, `client_out_time`, `client_approval_status`, `project_id`, `shadow_emp_id` are now in `project_timesheet_status_new` and require JOIN.

---

## Testing Recommendations

1. **Verify Status Filtering**: Test that status filtering works correctly with the new JOIN.
2. **Verify Day Type Filtering**: Test that day type comparisons work correctly.
3. **Verify Multiple Projects**: Test queries that may return multiple rows for same timesheet due to multiple projects.
4. **Verify Client Side ID**: Test that client side ID retrieval works correctly.
5. **Verify Aggregations**: Test GROUP BY queries to ensure they work with new structure.

---

## Next Steps

After review and approval, proceed with next 10 queries:
- `getAllMyTeamTimesheets`
- `getAllMyTimesheets`
- `findTimesheetOnLeaveDate`
- `getInactiveActivitiesByTimesheetId`
- `getMyTeamsFilledEodCountByManagerId`
- `getTimesheetFilledByMember`
- `getAllEmployeeTimesheetsBetweenDates`
- `getTimesheetsByDepartmentAndDateRange`
- `getLastFilledTimesheet`
- `checkEmployeeActiveOrNot`

---

**Document Version:** 1.0  
**Queries Updated:** 10/80  
**Completion:** 12.5%

