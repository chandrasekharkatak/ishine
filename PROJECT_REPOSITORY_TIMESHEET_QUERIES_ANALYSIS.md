# ProjectRepository Timesheet Queries Migration Analysis

## Overview
This document analyzes all timesheet-related queries in `ProjectRepository` that need to be updated to use the new `_new` tables.

**Date:** 2025-01-30

---

## Queries to Update

### 1. `getProjectTimesheetSummaryByEmpId` (Line ~899)
- **Type:** JPQL Query
- **Entity:** `Timesheet` → `EmployeeTimesheetsNew`
- **Entity:** `TimesheetActivityMap` → `EmployeeTimesheetActivitiesMappingNew`
- **Changes:** Update entity references in JPQL

### 2. `getEmployeeTimesheetsByProject` (Line ~1505)
- **Type:** Complex CTE Query
- **Tables:** Multiple references to old tables
- **CTEs to Update:**
  - `Expected_Client_Side_Base_DSR` - subquery with `employee_timesheets`
  - `Actual_Client_Side_Submissions` - uses `employee_timesheets` and `timesheet_document_details`
  - `Employee_Document_Summary_Details` - uses `timesheet_document_details` and `employee_timesheets`
  - `Employee_Document_Summary` - uses `timesheet_document_details`

### 3. Complex Query (Line ~2428)
- **Type:** Complex CTE Query
- **CTEs to Update:**
  - `Dynamic_Expected_Days` - subqueries with `employee_timesheets`
  - `Apmosys_Timesheet_Summary` - uses `employee_timesheets`
  - `Document_Summary` - uses `timesheet_document_details`

### 4. Additional Queries (Lines ~3885, 3973, 3980, 4009, 4010, 4256)
- **Type:** Various queries with old table references
- **Need to identify method names**

---

## Key Changes Required

### Table Name Changes
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- `timesheet_document_details` → `timesheet_document_details_new`

### Column Changes
- `day_type` (String) → `day_type_id` (Integer) - need master table JOIN
- `status` (String) → `status` (Integer) - need master table JOIN
- `client_approval_status` → `client_approval_status_id` - need master table JOIN
- `tdd.emp_id` → `et.emp_id` (emp_id removed from document table)

### Entity Changes
- `Timesheet` → `EmployeeTimesheetsNew` (in JPQL queries)
- `TimesheetActivityMap` → `EmployeeTimesheetActivitiesMappingNew` (in JPQL queries)

### Project ID Resolution
- `et.project_id` → `pts.project_id` (from `project_timesheet_status_new` JOIN)

---

**Status:** Ready to implement

