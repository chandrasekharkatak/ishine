# EmployeeRepository Timesheet Queries Migration Analysis

## Overview
This document analyzes all timesheet-related queries in `EmployeeRepository` that need to be updated to use the new `_new` tables.

**Date:** 2025-01-30

---

## Queries to Update

### 1. `getTimesheetDataByEmpId` (Line ~503)
- **Table:** `employee_timesheets` → `employee_timesheets_new`
- **Table:** `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- **Changes:** Add master table JOINs for `day_type`, `status`

### 2. `getTimesheetDataByProjectId` (Line ~519)
- **Table:** `employee_timesheets` → `employee_timesheets_new`
- **Table:** `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- **Changes:** Add master table JOINs for `day_type`, `status`

### 3. `getTimesheetDataByTeamName` (Line ~537)
- **Table:** `employee_timesheets` → `employee_timesheets_new`
- **Table:** `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- **Changes:** Add master table JOINs for `day_type`, `status`

### 4. `getDynamicTimesheetData` (Line ~554)
- **Table:** `employee_timesheets` → `employee_timesheets_new`
- **Table:** `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- **Changes:** Add master table JOINs for `day_type`, `status`

### 5. `getTimesheetData` (Line ~576)
- **Table:** `employee_timesheets` → `employee_timesheets_new`
- **Table:** `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- **Column:** `et.day_type` → `dtm.day_type` (via JOIN)
- **Column:** `et.status` → `sm.status` (via JOIN)
- **Column:** `et.total_time` → `ROUND(et.total_activities_minutes / 60, 2)`
- **Changes:** Add master table JOINs

### 6. `getActivityData` (Line ~601)
- **Table:** `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- **Column:** `etam.completion_time` → `CAST(etam.duration_minutes AS DECIMAL(10,2))/60`
- **Changes:** Convert minutes to hours

### 7. `findEmployeeAndTimesheetDetailsWithoutPagination` (Line ~1023)
- **JPQL Query:** `Timesheet` → `EmployeeTimesheetsNew`
- **Changes:** Update entity reference

### 8. Complex CTE Query (Line ~1939)
- **Table:** `employee_timesheets` → `employee_timesheets_new`
- **Table:** `timesheet_document_details` → `timesheet_document_details_new`
- **Changes:** Multiple CTEs need updates

### 9. Complex CTE Query (Line ~2955)
- **Table:** `employee_timesheets` → `employee_timesheets_new`
- **Table:** `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- **Changes:** Multiple CTEs need updates

### 10. `countPendingTimesheetsByEmployeeAndDate` (Line ~3294)
- **Table:** `employee_timesheets` → `employee_timesheets_new`
- **Table:** `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- **Changes:** Add master table JOINs

### 11. `findPendingTimesheetProjectNames` (Line ~3312)
- **Table:** `employee_timesheets` → `employee_timesheets_new`
- **Table:** `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- **Changes:** Add master table JOINs

---

## Key Changes Summary

### Table Name Changes
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- `timesheet_document_details` → `timesheet_document_details_new`

### Column Changes
- `day_type` (String) → `day_type_id` (Integer) - need master table JOIN
- `status` (String) → `status` (Integer) - need master table JOIN
- `completion_time` (Float) → `duration_minutes` (Short) - convert to hours
- `total_time` → `ROUND(total_activities_minutes / 60, 2)`

### Entity Changes
- `Timesheet` → `EmployeeTimesheetsNew` (in JPQL queries)

---

**Status:** Ready to implement

