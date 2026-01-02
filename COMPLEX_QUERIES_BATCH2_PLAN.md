# Complex Queries Batch 2 - Next 10 Queries Plan

## Overview
This document outlines the plan for updating the next 10 complex queries (Category C) - Batch 2.

**Date:** 2025-01-30  
**Batch:** 2 of 4 (10 queries per batch)

---

## Target Queries (Batch 2)

### Priority 1: Critical Queries from Tracker

1. 🔴 **`getTotalEmployeeCount`** (Line ~7523)
   - **Status:** Need to verify and update
   - **Risk:** HIGH (Count query)
   - **Action:** Check if uses old tables, update CTEs if needed

2. 🔴 **`getProjectViewForClientAttendanceStatus`**
   - **Status:** Need to locate
   - **Risk:** CRITICAL (Affects Billing)
   - **Action:** Similar to `getEmployeeViewForClientAttendanceStatus` (already updated)

### Priority 2: Remaining Queries with Old Table References

Based on grep results, there are still ~18 references to old tables. Let me identify which queries these belong to:

3-12. **Queries with remaining old table references**
   - Need to identify which active queries still use `employee_timesheets` (without `_new`)
   - Update them systematically

---

## Strategy

1. **Identify all queries** that still reference old tables
2. **Group by similarity** (CTEs, structure, etc.)
3. **Update systematically** using patterns from Batch 1
4. **Verify each fix** before moving to next

---

## Update Patterns (From Batch 1)

### Pattern 1: `Employee_Timesheets_With_Activities` CTE
- Update table names to `_new`
- Add master table JOINs
- Update column references

### Pattern 2: `Timesheet_Base_Data` CTE
- Update table names to `_new`
- Fix project-level columns to come from `project_timesheet_status_new`
- Add master table JOINs

### Pattern 3: Direct table references
- `employee_timesheets` → `employee_timesheets_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- Add master table JOINs for `day_type` and `status`

---

**Status:** Planning - Ready to Begin Updates

