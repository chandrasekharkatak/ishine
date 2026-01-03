# ProjectRepository Named Queries Migration - Complete

## Summary
This document summarizes changes made to Project-related named queries in `jpa-named-queries.properties` that reference old timesheet tables.

**Date:** 2025-01-30  
**Status:** ✅ **COMPLETE**

---

## Named Queries Updated

### 1. ✅ **`Project.poProjectTimesheetSync`** (Line ~1577)
- **Type:** Named Native Query
- **Repository Method:** `poProjectTimesheetSync(Set<Long> poProjectIdList)`
- **Changes:**
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Added project_id condition: `AND etam.project_id = t.project_id`
  - Added project_id condition: `AND p2.project_id = etam.project_id`
  - Updated subquery: `FROM employee_timesheets` → `FROM employee_timesheets_new`
- **Backup:** `Project.poProjectTimesheetSyncOLD` created

---

## Key Changes Summary

### Table Name Changes
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`

### Project ID Resolution
- Added: `AND etam.project_id = t.project_id` (to link activity mapping to correct project)
- Added: `AND p2.project_id = etam.project_id` (to ensure project consistency)

---

## Query Details

### Updated Query Structure

**Original:**
```sql
LEFT JOIN employee_timesheets et ON et.emp_id = etm.emp_id
LEFT JOIN employee_timesheet_activities_mapping etam ON etam.timesheet_id = et.timesheet_id
LEFT JOIN projects p2 ON p2.project_id = t2.project_id
INNER JOIN (
    SELECT emp_id, MAX(date) AS max_date
    FROM employee_timesheets
    GROUP BY emp_id
) et2 ON et.emp_id = et2.emp_id AND et.date = et2.max_date
```

**Updated:**
```sql
LEFT JOIN employee_timesheets_new et ON et.emp_id = etm.emp_id
LEFT JOIN employee_timesheet_activities_mapping_new etam ON etam.timesheet_id = et.timesheet_id AND etam.project_id = t.project_id
LEFT JOIN projects p2 ON p2.project_id = t2.project_id AND p2.project_id = etam.project_id
INNER JOIN (
    SELECT emp_id, MAX(date) AS max_date
    FROM employee_timesheets_new
    GROUP BY emp_id
) et2 ON et.emp_id = et2.emp_id AND et.date = et2.max_date
```

---

## Verification

- ✅ Named query updated in `jpa-named-queries.properties`
- ✅ Backup query created with `OLD` suffix
- ✅ Project ID conditions added for composite key matching
- ✅ Subquery updated to use new table

---

## Testing Recommendations

1. ✅ Test `poProjectTimesheetSync` method with new table structure
2. ✅ Verify project_id matching works correctly
3. ✅ Verify last timesheet filled project resolution
4. ✅ Test with real data

---

**Last Updated:** 2025-01-30  
**Status:** ✅ **MIGRATION COMPLETE**

