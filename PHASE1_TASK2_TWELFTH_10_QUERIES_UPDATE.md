# Phase 1, Task 2: Twelfth Set of Queries Update

## Summary
This document tracks the updates made to queries in the `EmployeeTimesheetsNewRepository` repository as part of Phase 1, Task 2, focusing on queries that still need updates.

## Status: IN PROGRESS

### Query: `getEmployeeSummaryReportAllEMP` (Line 7260)

**Status**: ⚠️ **PARTIALLY COMPLETE** - CTE update needed

**Issue**: The `Employee_Timesheets_With_Activities` CTE in this query (starting around line 6742) still references:
- Old table: `employee_timesheets` → Should be `employee_timesheets_new`
- Old table: `employee_timesheet_activities_mapping` → Should be `employee_timesheet_activities_mapping_new`
- Direct column references: `et.day_type` and `et.status` → Should use master table JOINs

**Required Changes**:
```sql
-- OLD (lines 6742-6749):
Employee_Timesheets_With_Activities AS (
    SELECT DISTINCT et.emp_id, et.date, et.day_type, et.status, et.office_in_time, et.office_out_time,
                    a.team_id AS activity_team_id
    FROM employee_timesheets et
    JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date
    LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id
    LEFT JOIN activities a ON a.activity_id = etam.activity_id
),

-- NEW (should be):
Employee_Timesheets_With_Activities AS (
    SELECT DISTINCT et.emp_id, et.date, dtm.day_type, sm.status, et.office_in_time, et.office_out_time,
                    a.team_id AS activity_team_id
    FROM employee_timesheets_new et
    LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id
    LEFT JOIN status_master_new sm ON et.status = sm.status_id
    JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date
    LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id
    LEFT JOIN activities a ON a.activity_id = etam.activity_id
),
```

**Note**: This query is complex and contains multiple CTEs. The replacement pattern appears multiple times in the file, making a targeted replacement challenging. The user has requested to proceed with other queries first before tackling complex queries like `getTotalEmployeeCount`.

## Next Steps

1. **Immediate**: Update the `Employee_Timesheets_With_Activities` CTE in `getEmployeeSummaryReportAllEMP` query (around line 6742)
2. **Next Set**: Identify and update the next set of simpler queries before tackling complex queries like `getTotalEmployeeCount`
3. **Future**: Address `getTotalEmployeeCount` and other complex CTE-based queries

## Files Modified
- `src/main/java/com/apmosys/employeeportal/repository/EmployeeTimesheetsNewRepository.java` (Partially - needs completion)

