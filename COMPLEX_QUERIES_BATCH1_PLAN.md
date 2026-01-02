# Complex Queries Batch 1 - First 10 Queries Plan

## Overview
This document outlines the plan for updating the first 10 complex queries (Category C).

**Date:** 2025-01-30  
**Batch:** 1 of 4 (10 queries per batch)

---

## Target Queries (First 10)

### Priority 1: Partially Complete Query
1. ⚠️ **`getEmployeeSummaryReportAllEMP`** (Line ~7263)
   - **Status:** Partially complete
   - **Issue:** `Employee_Timesheets_With_Activities` CTE (line ~6742) still uses old tables
   - **Action:** Fix CTE to use `_new` tables

### Priority 2: Calendar View Queries (Similar Structure)
2. 🔴 **`getEmployeeTimesheetAsCalenderByProjectId`** (Line ~5527)
   - **Status:** Need to verify if fully updated
   - **Lines:** 300+
   - **Risk:** CRITICAL (Affects Reporting)

3. 🔴 **`getEmployeeTimesheetAsCalender`** (Line ~5788)
   - **Status:** Need to verify if fully updated
   - **Lines:** 300+
   - **Risk:** CRITICAL (Affects Reporting)

4. 🔴 **`getEmployeeTimesheetAsCalenderForAllEmp`** (Line ~6019)
   - **Status:** Need to verify if fully updated
   - **Lines:** 300+
   - **Risk:** CRITICAL (Affects Reporting)

5. 🔴 **`getEmployeeTimesheetAsCalenderByProjectIdForAllEmp`** (Line ~6245)
   - **Status:** Need to verify if fully updated
   - **Lines:** 300+
   - **Risk:** CRITICAL (Affects Reporting)

### Priority 3: Other Complex Queries
6. 🔴 **`getProjectViewForClientAttendanceStatus`**
   - **Status:** Need to locate and verify
   - **Lines:** 700+
   - **Risk:** CRITICAL (Affects Billing)
   - **Note:** Similar to `getEmployeeViewForClientAttendanceStatus` (already updated)

7. 🔴 **`getAllEmployeeDSROfRM`**
   - **Status:** Need to locate and verify
   - **Risk:** HIGH (DSR Calculation)

8. 🔴 **`getEmployeeSummaryOnExport`**
   - **Status:** Need to locate and verify
   - **Risk:** HIGH (Export Format)

9. 🔴 **`getEmployeeSummaryOnExportAccordingToStatus`**
   - **Status:** Need to locate and verify
   - **Risk:** HIGH (Export with Status)

10. 🔴 **`getVmsDocumentApprovalStatusWiseCount`**
    - **Status:** Need to locate and verify
    - **Risk:** HIGH (Document Approval)

---

## Update Strategy

### Step 1: Verify Current Status
- Check each query to see if it's already updated
- Identify which parts still use old tables
- Document findings

### Step 2: Create Backups
- Create `_old` backup methods for each query
- Ensure backups are properly named

### Step 3: Update Queries
- Update table names to `_new` versions
- Add master table JOINs
- Update column references
- Fix CTEs and complex logic
- Update status calculation logic

### Step 4: Verify Updates
- Check all table references
- Verify master table JOINs
- Test column mappings
- Verify CTE structures

---

## Common Update Patterns

### 1. CTE Updates
- `Employee_Timesheets_With_Activities` CTE:
  - `employee_timesheets` → `employee_timesheets_new`
  - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Add JOINs to `day_type_master_new` and `status_master_new`
  - Update column references

### 2. Status Calculation
- Old: Day-level status
- New: Project-level status via `project_timesheet_status_new`
- Activity-level aggregation needed

### 3. Document References
- `timesheet_document_details` → `timesheet_document_details_new`
- `client_approval_status` → Use `client_status_master_new` JOIN

### 4. Time Calculations
- `total_time` → `ROUND(et.total_activities_minutes / 60, 2)`
- `total_working_hours` → `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`

---

## Reference Queries

Use these already-updated queries as patterns:
1. ✅ `getEmployeeViewForClientAttendanceStatus` - For `getProjectViewForClientAttendanceStatus`
2. ✅ `getTimesheetDashboardCountForEmployee` - For calendar views
3. ✅ `getEmployeeSummaryReportClientSideApplicable` - For summary reports

---

## Next Steps

1. Start with `getEmployeeSummaryReportAllEMP` (fix CTE)
2. Verify calendar queries status
3. Locate and update remaining queries
4. Document all changes

---

**Status:** Planning Complete - Ready to Begin Updates

