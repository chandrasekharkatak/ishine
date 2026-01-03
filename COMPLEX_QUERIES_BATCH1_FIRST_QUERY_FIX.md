# Complex Queries Batch 1 - First Query Fix

## Query: `getEmployeeSummaryReportAllEMP`

### Issue
The `Employee_Timesheets_With_Activities` CTE (lines 6742-6749) still uses old tables.

### Current Code (Lines 6742-6749):
```java
+ "    Employee_Timesheets_With_Activities AS (\n"
+ "        SELECT DISTINCT et.emp_id, et.date, et.day_type, et.status, et.office_in_time, et.office_out_time,\n"
+ "                        a.team_id AS activity_team_id\n"
+ "        FROM employee_timesheets et\n"
+ "        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
+ "        LEFT JOIN employee_timesheet_activities_mapping etam ON et.timesheet_id = etam.timesheet_id\n"
+ "        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
+ "    ),\n"
```

### Required Changes:
1. Change `FROM employee_timesheets et` to `FROM employee_timesheets_new et`
2. Change `LEFT JOIN employee_timesheet_activities_mapping etam` to `LEFT JOIN employee_timesheet_activities_mapping_new etam`
3. Change `et.day_type, et.status` to use master table JOINs:
   - Add `LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id`
   - Add `LEFT JOIN status_master_new sm ON et.status = sm.status_id`
   - Change `et.day_type` to `dtm.day_type`
   - Change `et.status` to `sm.status`

### Fixed Code:
```java
+ "    Employee_Timesheets_With_Activities AS (\n"
+ "        SELECT DISTINCT et.emp_id, et.date, dtm.day_type, sm.status, et.office_in_time, et.office_out_time,\n"
+ "                        a.team_id AS activity_team_id\n"
+ "        FROM employee_timesheets_new et\n"
+ "        LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id\n"
+ "        LEFT JOIN status_master_new sm ON et.status = sm.status_id\n"
+ "        JOIN Date_Parameters dp ON et.date BETWEEN dp.from_date AND dp.to_date\n"
+ "        LEFT JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id\n"
+ "        LEFT JOIN activities a ON a.activity_id = etam.activity_id\n"
+ "    ),\n"
```

### Location
- **File:** `src/main/java/com/apmosys/employeeportal/repository/EmployeeTimesheetsNewRepository.java`
- **Lines:** 6742-6749
- **Method:** `getEmployeeSummaryReportAllEMP`

### Status
⚠️ **MANUAL FIX REQUIRED** - The automated replacement failed due to file size/complexity. Please apply the changes manually using the fixed code above.

---

**Next Steps:**
1. Apply the fix manually to `getEmployeeSummaryReportAllEMP`
2. Verify the fix
3. Proceed with remaining 9 queries in Batch 1

