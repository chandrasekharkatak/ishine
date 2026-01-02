# TimesheetDocumentDetailsRepository → TimesheetDocumentDetailsNewRepository Migration - Complete

## Summary
This document summarizes all changes made to migrate queries from `TimesheetDocumentDetailsRepository` to `TimesheetDocumentDetailsNewRepository`.

**Date:** 2025-01-30  
**Status:** ✅ **COMPLETE**

---

## Changes Made

### 1. Repository File Updates (`TimesheetDocumentDetailsNewRepository.java`)

#### Import Changes
- ✅ Removed: `import com.apmosys.employeeportal.model.TimesheetDocumentDetails;`
- ✅ Added: `import com.apmosys.employeeportal.model.ClientStatusMasterNew;`
- ✅ Added: `import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;`
- ✅ Kept: `import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;`
- ✅ Kept: `import com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO;`

#### Method Return Type Updates
- ✅ All methods updated to return `TimesheetDocumentDetailsNew` instead of `TimesheetDocumentDetails`
- ✅ Created `_old` backup methods for all original methods

#### Query Updates

**Query 1: `findByTimesheetId`**
- ✅ Created backup: `findByTimesheetId_old`
- ✅ Updated entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`

**Query 2: `findDocIdByTimesheetId`**
- ✅ Created backup: `findDocIdByTimesheetId_old`
- ✅ Updated entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`

**Query 3: `findDocIdsByTimesheetId`**
- ✅ Created backup: `findDocIdsByTimesheetId_old`
- ✅ Updated entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`

**Query 4: `getDocsByEmpAndDateRange`**
- ✅ Created backup: `getDocsByEmpAndDateRange_old`
- ✅ Updated entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
- ✅ Updated JOIN entity: `Timesheet` → `EmployeeTimesheetsNew`

**Query 5: `findAllDocIdByTimesheetId` (DTO Constructor)**
- ✅ Created backup: `findAllDocIdByTimesheetId_old`
- ✅ Updated entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
- ✅ Updated JOIN entity: `Timesheet` → `EmployeeTimesheetsNew`
- ✅ **CRITICAL CHANGES:**
  - `t.empId` → `et.empId` (from joined `EmployeeTimesheetsNew`)
  - `t.clientApprovalStatus` → `COALESCE(csm.status, '')` (from joined `ClientStatusMasterNew`)
  - `t.rmApprovalStatus` → `''` (field removed, set to empty string)
  - `t.hrApprovalStatus` → `''` (field removed, set to empty string)
- ✅ Added JOIN: `LEFT JOIN ClientStatusMasterNew csm ON csm.statusId = tdd.clientApprovalStatusId`

**Query 6: `findDocumentsByEmpIdAndDate`**
- ✅ Created backup: `findDocumentsByEmpIdAndDate_old`
- ✅ Updated entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
- ✅ Updated JOIN entity: `Timesheet` → `EmployeeTimesheetsNew`

---

## Key Changes Summary

### Entity References
- `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
- `Timesheet` → `EmployeeTimesheetsNew` (in JOINs)

### Field Changes
- `clientApprovalStatus` (String) → `clientApprovalStatusId` (Integer) - resolved via `ClientStatusMasterNew` JOIN
- `rmApprovalStatus` → **REMOVED** (set to empty string in DTO)
- `hrApprovalStatus` → **REMOVED** (set to empty string in DTO)
- `empId` → **REMOVED from document table** (retrieved from joined `EmployeeTimesheetsNew`)

### DTO Constructor Updates
The DTO constructor query now:
- Gets `empId` from `EmployeeTimesheetsNew` via JOIN
- Gets `clientApprovalStatus` from `ClientStatusMasterNew` via LEFT JOIN
- Sets `rmApprovalStatus` and `hrApprovalStatus` to empty strings (fields no longer exist)

---

## Queries Updated

1. ✅ `findByDocIdAndActive` (Method - return type updated)
2. ✅ `findByDocIdAndFinalFlag` (Method - return type updated)
3. ✅ `findTopByTimesheetIdAndActive` (Method - return type updated)
4. ✅ `findByTimesheetId` (JPQL Query)
5. ✅ `findDocIdByTimesheetId` (JPQL Query)
6. ✅ `findDocIdsByTimesheetId` (JPQL Query)
7. ✅ `getDocsByEmpAndDateRange` (JPQL Query with JOIN)
8. ✅ `findAllDocIdByTimesheetId` (JPQL Query with DTO constructor)
9. ✅ `findDocumentsByEmpIdAndDate` (JPQL Query with JOIN)
10. ✅ `findByDocId` (Method - return type updated)

---

## Backup Methods/Queries Created

1. ✅ `findByDocIdAndActive_old`
2. ✅ `findByDocIdAndFinalFlag_old`
3. ✅ `findTopByTimesheetIdAndActive_old`
4. ✅ `findByTimesheetId_old`
5. ✅ `findDocIdByTimesheetId_old`
6. ✅ `findDocIdsByTimesheetId_old`
7. ✅ `getDocsByEmpAndDateRange_old`
8. ✅ `findAllDocIdByTimesheetId_old`
9. ✅ `findDocumentsByEmpIdAndDate_old`
10. ✅ `findByDocId_old`

---

## Testing Recommendations

1. ✅ Verify all simple finder methods work with new entity
2. ✅ Verify `getDocsByEmpAndDateRange` returns correct documents with new JOIN
3. ✅ Verify `findAllDocIdByTimesheetId` returns correct DTO with:
   - `empId` from joined timesheet
   - `clientApprovalStatus` from master table
   - Empty strings for removed status fields
4. ✅ Verify `findDocumentsByEmpIdAndDate` works with new JOIN
5. ✅ Test all queries with real data

---

## Notes

- All queries now use the new `TimesheetDocumentDetailsNew` entity
- JOINs updated to use `EmployeeTimesheetsNew` instead of `Timesheet`
- DTO constructor query handles removed fields gracefully
- `clientApprovalStatus` resolved via master table JOIN
- All backup methods/queries preserved with `_old` suffix

---

**Last Updated:** 2025-01-30  
**Status:** ✅ **MIGRATION COMPLETE**

