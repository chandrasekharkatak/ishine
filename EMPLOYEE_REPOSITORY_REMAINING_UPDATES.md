# EmployeeRepository - Remaining Query Updates

## Summary
This document tracks the remaining queries in `EmployeeRepository` that still need to be updated to use the new `_new` tables.

**Date:** 2025-01-30

---

## Queries Already Updated ✅

1. ✅ `getTimesheetDataByEmpId` - Updated with master table JOINs
2. ✅ `getTimesheetDataByProjectId` - Updated with master table JOINs
3. ✅ `getTimesheetDataByTeamName` - Updated with master table JOINs
4. ✅ `getDynamicTimesheetData` - Updated with master table JOINs
5. ✅ `getTimesheetData` - Updated with master table JOINs
6. ✅ `getActivityData` - Updated with duration_minutes conversion
7. ✅ `findEmployeeAndTimesheetDetailsWithoutPagination` - Updated JPQL entity
8. ✅ `countPendingTimesheetsByEmployeeAndDate` - Updated with master table JOINs
9. ✅ `findPendingTimesheetProjectNames` - Updated with master table JOINs
10. ✅ `Expected_Client_Side_Base_DSR` CTE (first part) - Updated

---

## Queries Still Needing Updates

### Complex CTE Query 1: `getEmployeeViewForClientAttendanceStatus`

**Location:** Line ~1963

**CTEs Needing Updates:**

1. **`Actual_Client_Side_Submissions` CTE** (Line ~2100)
   - `employee_timesheets` → `employee_timesheets_new`
   - `timesheet_document_details` → `timesheet_document_details_new`
   - `et.project_id` → `pts.project_id` (from `project_timesheet_status_new`)
   - `tdd.client_approval_status` → `csm.status` (from `client_status_master_new` JOIN)

2. **`Employee_Actual_Working_Days` CTE** (Line ~2129)
   - `employee_timesheets` → `employee_timesheets_new`
   - `timesheet_document_details` → `timesheet_document_details_new`
   - `et.project_id` → `pts.project_id` (from `project_timesheet_status_new`)

3. **`Missing_Days` CTE** (Line ~2146)
   - `employee_timesheets` → `employee_timesheets_new`
   - `et.project_id` → `pts.project_id` (from `project_timesheet_status_new`)
   - `et.day_type` → `dtm.day_type` (from `day_type_master_new` JOIN)

4. **`Apmosys_Timesheet_Summary` CTE** (Line ~2163)
   - `employee_timesheets` → `employee_timesheets_new`
   - `et.project_id` → `pts.project_id` (from `project_timesheet_status_new`)
   - `et.day_type` → `dtm.day_type` (from `day_type_master_new` JOIN)

5. **`Employee_Document_Summary` CTE** (Line ~2177)
   - `timesheet_document_details` → `timesheet_document_details_new`
   - `employee_timesheets` → `employee_timesheets_new`
   - `tdd.emp_id` → `et.emp_id` (emp_id removed from document table)
   - `et.project_id` → `pts.project_id` (from `project_timesheet_status_new`)
   - `tdd.client_approval_status` → `csm.status` (from `client_status_master_new` JOIN)

### Complex CTE Query 2: `getEmployeeViewForAllEmpAttendanceStatus`

**Location:** Line ~2900+

**CTEs Needing Updates:**

1. **`Employee_Timesheet_Statuses` CTE** (Line ~3111)
   - `employee_timesheets` → `employee_timesheets_new`
   - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
   - `et.day_type` → `dtm.day_type` (from `day_type_master_new` JOIN)
   - `et.status` → `sm.status` (from `status_master_new` JOIN)

---

## Update Patterns Required

### Pattern 1: Table Updates
- `employee_timesheets` → `employee_timesheets_new`
- `timesheet_document_details` → `timesheet_document_details_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`

### Pattern 2: Project ID Resolution
- `et.project_id` → `pts.project_id` (from `project_timesheet_status_new` JOIN)
- Add: `INNER JOIN project_timesheet_status_new pts ON pts.timesheet_id = et.timesheet_id`

### Pattern 3: Day Type Resolution
- `et.day_type` → `dtm.day_type` (from `day_type_master_new` JOIN)
- Add: `LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id`

### Pattern 4: Status Resolution
- `et.status` → `sm.status` (from `status_master_new` JOIN)
- Add: `LEFT JOIN status_master_new sm ON et.status = sm.status_id`

### Pattern 5: Client Approval Status Resolution
- `tdd.client_approval_status` → `csm.status` (from `client_status_master_new` JOIN)
- Add: `LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id`

### Pattern 6: Employee ID in Document Summary
- `tdd.emp_id` → `et.emp_id` (emp_id removed from document table, get from timesheet)

---

## Next Steps

1. Update `Actual_Client_Side_Submissions` CTE
2. Update `Employee_Actual_Working_Days` CTE
3. Update `Missing_Days` CTE
4. Update `Apmosys_Timesheet_Summary` CTE
5. Update `Employee_Document_Summary` CTE
6. Update `Employee_Timesheet_Statuses` CTE

---

**Status:** In Progress - Need to complete CTE updates

