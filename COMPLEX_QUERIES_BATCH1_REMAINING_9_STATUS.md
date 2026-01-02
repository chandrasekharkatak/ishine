# Complex Queries Batch 1 - Remaining 9 Queries Status

## Summary
This document tracks the status of the remaining 9 queries in Batch 1.

**Date:** 2025-01-30  
**Batch:** 1 of 4 (10 queries per batch)

---

## ✅ Query 1: `getEmployeeSummaryReportAllEMP`
- **Status:** ✅ **FIXED**
- **Location:** Line ~6742
- **Issue:** CTE `Employee_Timesheets_With_Activities` was using old tables
- **Fix Applied:** Updated to use `employee_timesheets_new`, `employee_timesheet_activities_mapping_new`, and master table JOINs

---

## Queries 2-5: Calendar View Queries

### Query 2: `getEmployeeTimesheetAsCalenderByProjectId`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line ~5527
- **Verification:** Uses `employee_timesheets_new` and master tables (verified at line 5348-5360)

### Query 3: `getEmployeeTimesheetAsCalender`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line ~5788
- **Verification:** Similar structure to Query 2, already using new tables

### Query 4: `getEmployeeTimesheetAsCalenderForAllEmp`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line ~6019
- **Verification:** Similar structure to Query 2, already using new tables

### Query 5: `getEmployeeTimesheetAsCalenderByProjectIdForAllEmp`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line ~6245
- **Verification:** Similar structure to Query 2, already using new tables

---

## Queries 6-10: Other Complex Queries

### Query 6: `getEmployeeViewForClientAttendanceStatus`
- **Status:** ⚠️ **NEEDS FIX**
- **Location:** Line ~7874
- **Issue:** `Timesheet_Base_Data` CTE (line ~7979) uses old tables:
  - `FROM employee_timesheets et` → should be `employee_timesheets_new`
  - `LEFT JOIN employee_timesheet_activities_mapping etam` → should be `employee_timesheet_activities_mapping_new`
  - `et.day_type` → should use `day_type_master_new` JOIN
  - `et.client_in_time, et.client_out_time` → these columns don't exist in `employee_timesheets_new` (they're in `project_timesheet_status_new`)

### Query 7: Similar Query (likely `getProjectViewForClientAttendanceStatus` or another variant)
- **Status:** ⚠️ **NEEDS FIX**
- **Location:** Line ~8123 (likely `getTotalEmployeeCountForClientApplicable`)
- **Issue:** `Timesheet_Base_Data` CTE (line ~8225) uses old tables - same issues as Query 6

### Query 8: `getAllEmployeeDSROfRM`
- **Status:** ❓ **NOT FOUND**
- **Action:** Need to search with different patterns or verify if it exists

### Query 9: `getEmployeeSummaryOnExport`
- **Status:** ❓ **NOT FOUND**
- **Action:** Need to search with different patterns or verify if it exists

### Query 10: `getEmployeeSummaryOnExportAccordingToStatus`
- **Status:** ❓ **NOT FOUND**
- **Action:** Need to search with different patterns or verify if it exists

### Query 11: `getVmsDocumentApprovalStatusWiseCount`
- **Status:** ❓ **NOT FOUND**
- **Action:** Need to search with different patterns or verify if it exists

---

## Issues Found

### Issue 1: `getEmployeeViewForClientAttendanceStatus` - `Timesheet_Base_Data` CTE
**Location:** Line ~7979-7988

**Current Code:**
```java
+ "        Timesheet_Base_Data AS (\n"
+ "        SELECT DISTINCT\n"
+ "            et.timesheet_id, et.emp_id, t.project_id, etm.employee_team_map_id,\n"
+ "            et.date, et.day_type, et.client_in_time, et.client_out_time, et.shadow_emp_id,t.team_id team_id, a.team_id as a_team_id\n"
+ "        FROM employee_timesheets et\n"
+ "        LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id\n"
+ "        LEFT JOIN activities a ON etam.activity_id = a.activity_id\n"
+ "        LEFT JOIN teams t ON a.team_id = t.team_id\n"
+ "        LEFT JOIN employee_team_mapping etm ON et.emp_id = etm.emp_id and etm.team_id = t.team_id AND et.date >= DATE(etm.start_date) AND (etm.end_date IS NULL OR et.date <= DATE(etm.end_date))\n"
+ "        WHERE et.date BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters)\n"
+ "    ),\n"
```

**Required Changes:**
1. `employee_timesheets et` → `employee_timesheets_new et`
2. `employee_timesheet_activities_mapping etam` → `employee_timesheet_activities_mapping_new etam`
3. `et.day_type` → Add `LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id` and use `dtm.day_type`
4. `et.client_in_time, et.client_out_time` → These are in `project_timesheet_status_new`, need to JOIN and get from there
5. `et.shadow_emp_id` → This might also be in `project_timesheet_status_new`

### Issue 2: Similar Query - `Timesheet_Base_Data` CTE
**Location:** Line ~8225-8234

Same issues as Issue 1.

---

## Next Steps

1. ✅ Fix Query 1 - DONE
2. ✅ Verify Queries 2-5 - Already updated
3. ⚠️ Fix Query 6 - `getEmployeeViewForClientAttendanceStatus` CTE
4. ⚠️ Fix Query 7 - Similar query CTE
5. ❓ Locate Queries 8-11 or verify they don't exist

---

**Last Updated:** 2025-01-30  
**Status:** 1/10 Complete, 4/10 Already Updated, 2/10 Need Fix, 4/10 Not Found

