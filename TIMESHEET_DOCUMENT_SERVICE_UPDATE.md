# Timesheet Document Service Update - Colleague's Changes Integration

## Overview
This document describes the integration of colleague's changes to document-related methods in `TimesheetService` into `TimesheetDocumentService`.

## Changes Made by Colleague

### 1. `replaceAllTemporaryFileWithFinalFile` Method
**New Logic:**
- Creates a single `FinalDocument` entity instead of creating multiple `TimesheetDocumentDetails`
- Links all temporary documents to the `FinalDocument` via `bulkApprovedDocId`
- Updates temporary documents to set `finalFlag=true` and link to `FinalDocument`
- Updates timesheet statuses to "Pending" and "Approved"

**Key Changes:**
- Uses `FinalDocument` entity and `FinalDocumentRepository`
- Single `FinalDocument` is created and shared across all valid temporary documents
- All valid temp docs are updated with `bulkApprovedDocId` pointing to the `FinalDocument`

### 2. `handleDocumentUpload` Method
**New Logic:**
- When `isFinal=true`: 
  - Creates a `FinalDocument` entity
  - Updates existing `TimesheetDocumentDetails` to link to `FinalDocument` via `bulkApprovedDocId`
  - Sets `finalFlag=true` on the existing document
- When `isFinal=false`: 
  - Creates a new `TimesheetDocumentDetails` (existing logic)

**Key Changes:**
- Final documents are now stored in `FinalDocument` table
- `TimesheetDocumentDetails` references `FinalDocument` via `bulkApprovedDocId`
- Existing document is updated instead of creating a new one when `isFinal=true`

## Integration Changes

### 1. Updated `TimesheetDocumentService.java`

#### Added Dependencies:
```java
@Autowired
private FinalDocumentRepository finalDocumentRepository;
```

#### Updated `handleDocumentUpload` Method:
- **When `isFinal=true`**:
  - Creates `FinalDocument` entity
  - Saves `FinalDocument` to database
  - Retrieves existing `TimesheetDocumentDetails` by `timesheetId`
  - Updates existing document with:
    - `finalFlag = true`
    - `clientApprovalStatus` from DTO
    - `bulkApprovedDocId` = `FinalDocument.docId`
  - Saves updated `TimesheetDocumentDetails`

- **When `isFinal=false`**:
  - Creates new `TimesheetDocumentDetails` using `addTimesheetDocument`
  - Saves new document

#### Updated `replaceAllTemporaryFileWithFinalFile` Method:
- **Simplified Logic**:
  - Validates inputs
  - Fetches temporary documents by employee and date range
  - Caches timesheets for performance
  - Filters valid temp docs (Working/Non-Working day types, not already final)
  - Creates single `FinalDocument` entity
  - Updates all valid temp docs to link to `FinalDocument` via `bulkApprovedDocId`
  - Updates timesheet statuses
  - Bulk saves updated documents

#### Updated `addTimesheetDocument` Method:
- Added handling for `Update` operation with `finalFlag=false`
- Added `bulkApprovedDocId` field assignment
- Maintains backward compatibility

#### Updated `buildNewDoc` Helper Method:
- Added `bulkApprovedDocId` field assignment

### 2. Updated `TimesheetService.java`

#### Delegated Methods:
- `handleDocumentUpload()` → delegates to `timesheetDocumentService.handleDocumentUpload()`
- `replaceAllTemporaryFileWithFinalFile()` → delegates to `timesheetDocumentService.replaceAllTemporaryFileWithFinalFile()`
- `addTimesheetDocument()` → delegates to `timesheetDocumentService.addTimesheetDocument()`

#### Removed Duplicate Implementations:
- Removed `handleDocumentUpload` implementation (now delegated)
- Removed `replaceAllTemporaryFileWithFinalFile` implementation (now delegated)
- Kept `addTimesheetDocument` as delegate method (still used internally in some update flows)

## Key Architectural Changes

### FinalDocument Entity Usage
- **Purpose**: Stores final/approved documents separately from temporary documents
- **Relationship**: `TimesheetDocumentDetails.bulkApprovedDocId` → `FinalDocument.docId`
- **Benefits**:
  - Single document storage for bulk operations
  - Better data normalization
  - Easier document management

### Document Flow Changes

#### Before (Old Flow):
1. Create `TimesheetDocumentDetails` for each document
2. Each document stored separately
3. No centralized final document storage

#### After (New Flow):
1. **Temporary Documents**: Create `TimesheetDocumentDetails` with `finalFlag=false`
2. **Final Documents**: 
   - Create `FinalDocument` entity
   - Update `TimesheetDocumentDetails` to link via `bulkApprovedDocId`
   - Set `finalFlag=true`
3. **Bulk Operations**: Single `FinalDocument` shared across multiple timesheets

## Dependencies Added

### New Repository:
- `FinalDocumentRepository` - For managing `FinalDocument` entities

### New Entity:
- `FinalDocument` - Stores final/approved document data
  - Fields: `docId`, `docName`, `docData`, `docMimeType`, `createdOn`, `createdBy`, `updatedOn`, `updatedBy`

## Method Signatures

### Updated Methods in TimesheetDocumentService:

1. **`handleDocumentUpload(TimesheetDTO, Timesheet, MultipartFile, boolean)`**
   - Handles both final and non-final document uploads
   - Creates `FinalDocument` when `isFinal=true`
   - Updates existing document or creates new one

2. **`replaceAllTemporaryFileWithFinalFile(MultipartFile, LocalDate, LocalDate, Long)`**
   - Creates single `FinalDocument`
   - Links all valid temp docs to it
   - Updates timesheet statuses

3. **`addTimesheetDocument(TimesheetDocumentDetailsDTO, String, MultipartFile)`**
   - Handles `Update` operation for both `finalFlag=true` and `finalFlag=false`
   - Sets `bulkApprovedDocId` field

## Benefits

1. **Data Normalization**: Final documents stored separately, reducing duplication
2. **Performance**: Single document for bulk operations instead of multiple copies
3. **Maintainability**: Clear separation between temporary and final documents
4. **Consistency**: All document operations go through `TimesheetDocumentService`
5. **Backward Compatibility**: Existing code continues to work via delegate methods

## Testing Recommendations

### Test Cases for `handleDocumentUpload`:
1. **Final Document Upload**:
   - Verify `FinalDocument` is created
   - Verify existing `TimesheetDocumentDetails` is updated with `bulkApprovedDocId`
   - Verify `finalFlag` is set to `true`

2. **Non-Final Document Upload**:
   - Verify new `TimesheetDocumentDetails` is created
   - Verify `finalFlag` is set to `false`
   - Verify document is saved correctly

### Test Cases for `replaceAllTemporaryFileWithFinalFile`:
1. **Valid Temp Docs**:
   - Verify single `FinalDocument` is created
   - Verify all valid temp docs are linked via `bulkApprovedDocId`
   - Verify timesheet statuses are updated

2. **No Valid Temp Docs**:
   - Verify appropriate error is thrown
   - Verify no `FinalDocument` is created

3. **Filtering**:
   - Verify only Working/Non-Working day types are processed
   - Verify already-final docs (not rejected) are excluded

## Files Modified

1. **`TimesheetDocumentService.java`**:
   - Added `FinalDocumentRepository` dependency
   - Updated `handleDocumentUpload()` method
   - Updated `replaceAllTemporaryFileWithFinalFile()` method
   - Updated `addTimesheetDocument()` method
   - Updated `buildNewDoc()` helper method

2. **`TimesheetService.java`**:
   - Delegated `handleDocumentUpload()` to document service
   - Delegated `replaceAllTemporaryFileWithFinalFile()` to document service
   - Delegated `addTimesheetDocument()` to document service

## Migration Notes

- **Backward Compatible**: All changes are backward compatible
- **No Database Schema Changes**: Uses existing `FinalDocument` table
- **No API Changes**: Method signatures remain the same
- **Error Handling**: Maintains existing error handling patterns

## Notes

- The `FinalDocument` entity is used for bulk document operations
- `TimesheetDocumentDetails.bulkApprovedDocId` links to `FinalDocument.docId`
- All document operations are now centralized in `TimesheetDocumentService`
- TimesheetService methods delegate to document service for consistency

