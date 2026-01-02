# TimesheetDocumentDetailsRepository → TimesheetDocumentDetailsNewRepository Migration Analysis

## Overview
This document analyzes the changes required to migrate queries from `TimesheetDocumentDetailsRepository` to `TimesheetDocumentDetailsNewRepository`.

**Date:** 2025-01-30

---

## Entity Structure Changes

### Old Entity: `TimesheetDocumentDetails`
- **Table:** `timesheet_document_details`
- **Fields:**
  - `docId` (Long)
  - `docName` (String)
  - `docData` (byte[]) - **REMOVED in new**
  - `docMimeType` (String) - **CHANGED to `mimeTypeId` (Integer)**
  - `timesheetId` (Long)
  - `empId` (Long) - **REMOVED in new**
  - `createdOn` (LocalDateTime)
  - `createdBy` (Long)
  - `updatedOn` (LocalDateTime)
  - `updatedBy` (Long)
  - `active` (Boolean)
  - `clientApprovalStatus` (String) - **CHANGED to `clientApprovalStatusId` (Integer)**
  - `rmApprovalStatus` (String) - **REMOVED in new**
  - `hrApprovalStatus` (String) - **REMOVED in new**
  - `finalFlag` (Boolean)
  - `bulkApprovedDocId` (Long)

### New Entity: `TimesheetDocumentDetailsNew`
- **Table:** `timesheet_document_details_new`
- **Fields:**
  - `docId` (Long)
  - `timesheetId` (Long)
  - `fileUrl` (String) - **NEW field**
  - `docName` (String)
  - `mimeTypeId` (Integer) - **CHANGED from String**
  - `clientApprovalStatusId` (Integer) - **CHANGED from String**
  - `createdBy` (Long)
  - `createdOn` (LocalDateTime)
  - `updatedBy` (Long)
  - `updatedOn` (LocalDateTime)
  - `active` (Boolean)
  - `finalFlag` (Boolean)
  - `bulkApprovedDocId` (Long)

---

## Queries to Update

### 1. `findByDocIdAndActive` (Method)
- **Status:** ✅ Should work (fields exist in both)
- **Changes:** Return type needs update

### 2. `findByDocIdAndFinalFlag` (Method)
- **Status:** ✅ Should work (fields exist in both)
- **Changes:** Return type needs update

### 3. `findTopByTimesheetIdAndActive` (Method)
- **Status:** ✅ Should work (fields exist in both)
- **Changes:** Return type needs update

### 4. `findByTimesheetId` (JPQL Query)
- **Location:** Line 25-26
- **Changes Needed:**
  - Entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
  - Should work as-is (fields exist)

### 5. `findDocIdByTimesheetId` (JPQL Query)
- **Location:** Line 28-29
- **Changes Needed:**
  - Entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
  - Should work as-is

### 6. `findDocIdsByTimesheetId` (JPQL Query)
- **Location:** Line 31-32
- **Changes Needed:**
  - Entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
  - Should work as-is

### 7. `getDocsByEmpAndDateRange` (JPQL Query)
- **Location:** Line 34-42
- **Changes Needed:**
  - Entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
  - Entity: `Timesheet` → `EmployeeTimesheetsNew`
  - Field: `et.timesheetId` → `et.timesheetId` (should work)
  - Field: `et.empId` → `et.empId` (should work)
  - Field: `et.date` → `et.date` (should work)

### 8. `findAllDocIdByTimesheetId` (JPQL Query with DTO)
- **Location:** Line 44-49
- **Changes Needed:**
  - Entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
  - **CRITICAL:** DTO constructor parameters need update:
    - `t.empId` → **REMOVED** (field doesn't exist)
    - `t.clientApprovalStatus` → `t.clientApprovalStatusId` (or JOIN to master)
    - `t.rmApprovalStatus` → **REMOVED** (field doesn't exist)
    - `t.hrApprovalStatus` → **REMOVED** (field doesn't exist)
  - Need to check DTO structure

### 9. `findDocumentsByEmpIdAndDate` (JPQL Query)
- **Location:** Line 52-57
- **Changes Needed:**
  - Entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
  - Entity: `Timesheet` → `EmployeeTimesheetsNew`
  - Should work as-is (JOIN fields exist)

### 10. `findByDocId` (Method)
- **Status:** ✅ Should work (fields exist in both)
- **Changes:** Return type needs update

---

## Key Changes Summary

### Entity References
- `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
- `Timesheet` → `EmployeeTimesheetsNew` (in JOINs)

### Field Changes
- `docMimeType` (String) → `mimeTypeId` (Integer) - may need master table JOIN
- `clientApprovalStatus` (String) → `clientApprovalStatusId` (Integer) - may need master table JOIN
- `rmApprovalStatus` → **REMOVED**
- `hrApprovalStatus` → **REMOVED**
- `empId` → **REMOVED** (from document table)
- `docData` → **REMOVED** (likely stored elsewhere)

### DTO Constructor
- Need to check `TimesheetDocumentDetailsDTO` constructor
- May need to update DTO or query to handle removed fields

---

## Implementation Plan

1. ✅ Update all entity references in JPQL queries
2. ✅ Update return types in method signatures
3. ✅ Update JOIN entity references (`Timesheet` → `EmployeeTimesheetsNew`)
4. ✅ Handle removed fields in DTO constructor query
5. ✅ Create backups of old queries
6. ✅ Test all queries

---

**Status:** Ready to implement

