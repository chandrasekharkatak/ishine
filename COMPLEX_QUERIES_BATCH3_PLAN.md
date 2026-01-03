# Complex Queries Batch 3 - Next 10 Queries Plan

## Overview
This document outlines the plan for updating the next 10 complex queries (Category C) - Batch 3.

**Date:** 2025-01-30  
**Batch:** 3 of 4 (10 queries per batch)

---

## Target Queries (Batch 3)

Based on the tracker and remaining old table references, I need to identify queries that:
1. Still reference old tables
2. Are complex queries (Category C - REWRITE)
3. Haven't been updated yet

### Priority Queries from Tracker:

1. 🔴 **`getProjectViewForClientAttendanceStatus`**
   - **Risk:** CRITICAL (Affects Billing)
   - **Status:** Need to locate and update
   - **Action:** Similar to `getEmployeeViewForClientAttendanceStatus` (already updated)

2. 🔴 **`getAllEmployeeDSROfRM`**
   - **Risk:** HIGH (DSR Calculation)
   - **Status:** Need to locate and update
   - **Action:** Complex JOIN with aggregation - needs activity-level aggregation

3. 🔴 **`getEmployeeSummaryOnExport`**
   - **Risk:** HIGH (Export Format)
   - **Status:** Need to locate and update
   - **Action:** Export format may need adjustment for new structure

4. 🔴 **`getEmployeeSummaryOnExportAccordingToStatus`**
   - **Risk:** HIGH (Export with Status)
   - **Status:** Need to locate and update
   - **Action:** Status filtering logic needs rewrite

5. 🔴 **`getVmsDocumentApprovalStatusWiseCount`**
   - **Risk:** HIGH (Document Approval)
   - **Status:** Need to locate and update
   - **Action:** Document approval count query

### Additional Queries to Identify:

6-10. **Queries with remaining old table references**
   - Need to identify which active queries still use old tables
   - Update them systematically

---

## Strategy

1. **Search for missing queries** from the tracker
2. **Identify queries** that still reference old tables
3. **Group by similarity** (CTEs, structure, etc.)
4. **Update systematically** using patterns from Batch 1 & 2
5. **Verify each fix** before moving to next

---

## Update Patterns (From Previous Batches)

### Pattern 1: `Employee_Timesheets_With_Activities` CTE
- Update table names to `_new`
- Add master table JOINs
- Update column references

### Pattern 2: `Timesheet_Base_Data` CTE
- Update table names to `_new`
- Fix project-level columns to come from `project_timesheet_status_new`
- Add master table JOINs

### Pattern 3: `Employees_With_ClientID` CTE
- Use `employee_client_side_id_mapping_new` instead of `employee_timesheets`

### Pattern 4: NOT EXISTS Subqueries
- Update to use `employee_timesheets_new` with master table JOINs

---

**Status:** Planning - Ready to Begin Updates

