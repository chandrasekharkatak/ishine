# Complex Queries Batch 4 - Final Batch Plan

## Overview
This document outlines the plan for updating the final batch of complex queries (Category C) - Batch 4.

**Date:** 2025-01-30  
**Batch:** 4 of 4 (Final batch)

---

## Target Queries (Batch 4)

Based on remaining old table references and tracker, I need to:

1. **Verify remaining 11 old table references** - Check if they're in active queries or backups
2. **Update any active queries** that still reference old tables
3. **Complete any missing queries** from the tracker

### Priority Queries from Tracker (if they exist):

1. 🔴 **`getLastFilledTimesheetByEmp`**
   - **Risk:** MEDIUM
   - **Status:** Need to locate and verify

2. 🔴 **`getEmployeeByNameAndEmpidForTimesheet`**
   - **Risk:** MEDIUM
   - **Status:** Need to locate and verify

3. 🔴 **`getClientSideIdByProjectId`**
   - **Risk:** MEDIUM
   - **Status:** Need to locate and verify

4. 🔴 **`getClientSideIdByProjectIdAndEmpId`**
   - **Risk:** MEDIUM
   - **Status:** Need to locate and verify

5. 🔴 **`fetchEmploymentIdByEmpId`**
   - **Risk:** MEDIUM
   - **Status:** Need to locate and verify

6. 🔴 **`getEmployeesWithClientId`**
   - **Risk:** MEDIUM
   - **Status:** Need to locate and verify

7. 🔴 **`getTimesheetForEmployee`**
   - **Risk:** MEDIUM
   - **Status:** Need to locate and verify

8. 🔴 **`getDocumentsByEmpAndDate`**
   - **Risk:** MEDIUM
   - **Status:** Need to locate and verify

9. 🔴 **`getEmployeeTimesheetsByProject`**
   - **Risk:** MEDIUM (HIGH RISK - needs activity aggregation)
   - **Status:** Need to locate and verify

---

## Strategy

1. **Identify remaining old table references** - Check each of the 11 references
2. **Verify if they're in active queries** or backups/comments
3. **Update active queries** using established patterns
4. **Document completion** of all batches

---

## Update Patterns (From Previous Batches)

### Pattern 1: Table Name Updates
- `employee_timesheets` → `employee_timesheets_new`
- `timesheet_document_details` → `timesheet_document_details_new`
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`

### Pattern 2: Master Table JOINs
- Add `LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id`
- Add `LEFT JOIN status_master_new sm ON et.status = sm.status_id`
- Add `LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id`

### Pattern 3: Column References
- `et.day_type` → `dtm.day_type`
- `et.status` → `sm.status`
- `tdd.client_approval_status` → `csm.status`

---

**Status:** Planning - Ready to Begin Updates

