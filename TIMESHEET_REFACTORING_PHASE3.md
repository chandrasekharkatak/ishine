# Timesheet Service Refactoring - Phase 3: Document Service Extraction

## Overview
This document describes Phase 3 of the Timesheet service refactoring, which extracts document management logic from `TimesheetService` into a dedicated `TimesheetDocumentService`.

## Objectives
- **Separation of Concerns**: Move document operations out of the main service
- **Code Reusability**: Centralize document operations for reuse
- **Maintainability**: Single location for all document-related logic
- **Testability**: Enable isolated unit testing of document operations
- **Readability**: Make service methods cleaner and more focused

## Changes Made

### 1. New File Created
**File**: `src/main/java/com/apmosys/employeeportal/service/TimesheetDocumentService.java`

**Purpose**: 
- Encapsulates all document-related operations for timesheets
- Provides reusable document management methods
- Handles file upload, storage, retrieval, and approval operations

**Key Methods**:

1. **`handleDocumentUpload(TimesheetDTO, Timesheet, MultipartFile, boolean)`**
   - Handles document upload for a timesheet
   - Validates document metadata
   - Associates document with timesheet
   - Throws exceptions for validation failures

2. **`addTimesheetDocument(TimesheetDocumentDetailsDTO, String, MultipartFile)`**
   - Creates or updates a timesheet document
   - Handles both "Create" and "Update" operations
   - Processes multipart file and stores document data

3. **`buildNewDoc(TimesheetDocumentDetails, TimesheetDocumentDetailsDTO, MultipartFile)`** (private)
   - Helper method to build new document entity
   - Sets all document properties from DTO and file

4. **`fetchTimesheetDocument(Long docId, Long timesheetId)`**
   - Fetches document by docId or timesheetId
   - Returns DTO with Base64 encoded document data
   - Throws exception if neither parameter provided

5. **`getDocumentDataByDocId(Long docId)`**
   - Gets document data by document ID
   - Returns ServiceResponse with document details
   - Includes logging and error handling

6. **`approveOrRejectDocument(Long docId, Long approvedOrRejectedBy, String approvalStatus)`**
   - Approves or rejects a document
   - Updates HR approval status
   - Transactional operation

7. **`replaceAllTemporaryFileWithFinalFile(MultipartFile, LocalDate, LocalDate, Long)`**
   - Bulk operation to replace temporary files with final file
   - Handles complex logic for rejected timesheets
   - Updates timesheet statuses
   - Creates final documents for single temp docs
   - Transactional operation

8. **`getAllDisabledDateListForBulkDocSubmit(Integer projectId, Long empId)`**
   - Gets all disabled dates for bulk document submit
   - Considers unfilled dates, leave dates, and existing documents
   - Returns set of disabled dates

9. **`getAllLeaveDates(List<EmployeeLeave>)`** (public helper)
   - Helper method to extract all leave dates from employee leave list
   - Returns set of dates

### 2. Modified Files

#### TimesheetService.java
**Changes**:

- **Added import**:
  ```java
  import com.apmosys.employeeportal.service.TimesheetDocumentService;
  ```

- **Added dependency injection**:
  ```java
  @Autowired
  TimesheetDocumentService timesheetDocumentService;
  ```

- **Refactored `addTimesheet()` method**:
  - Replaced `handleDocumentUpload()` calls with `timesheetDocumentService.handleDocumentUpload()`
  - Reduced document handling code

- **Removed methods** (moved to TimesheetDocumentService):
  - `handleDocumentUpload()` - ~27 lines
  - `addTimesheetDocument()` - ~60 lines
  - `buildNewDoc()` - ~25 lines
  - `fetchTimesheetDocument()` - ~30 lines
  - `getDocumentDataByDocId()` - ~35 lines
  - `approveOrRejectDocument()` - ~30 lines
  - `replaceAllTemporaryFileWithFinalFile()` - ~220 lines
  - `getAllDisabledDateListForBulkDocSubmit()` - ~90 lines
  - `getAllLeaveDates()` - ~15 lines

- **Added delegate methods**:
  - `getDocumentDataByDocId()` - delegates to document service
  - `replaceAllTemporaryFileWithFinalFile()` - delegates to document service
  - `getAllDisabledDateListForBulkDocSubmit()` - delegates to document service
  - `approveOrRejectDocument()` - delegates to document service
  - `fetchTimesheetDocument()` - delegates to document service

**Total Lines Removed**: ~532 lines
**Total Lines Added**: ~5 lines (delegate methods)
**Net Reduction**: ~527 lines

## Benefits

### 1. Code Reduction
- **TimesheetService**: Reduced by ~527 lines
- **Eliminated Duplication**: Document logic was scattered across multiple methods
- **Cleaner Methods**: Service methods are now more focused on business logic

### 2. Improved Maintainability
- **Single Source of Truth**: All document operations are in one place
- **Easier Updates**: Changes to document logic only need to be made in one file
- **Better Organization**: Document logic is logically grouped

### 3. Enhanced Testability
- **Isolated Testing**: Document operations can be unit tested independently
- **Mocking**: Document service can be easily mocked in service tests
- **Test Coverage**: Each document method can have comprehensive test coverage

### 4. Better Code Organization
- **Separation of Concerns**: Service focuses on business logic, document service focuses on documents
- **Single Responsibility**: Each class has a clear, focused purpose
- **Reusability**: Document methods can be reused across different service methods

### 5. Improved Readability
- **Clear Intent**: Method names clearly indicate document operations
- **Reduced Complexity**: Service methods are easier to read and understand
- **Better Documentation**: Document logic is self-documenting through method names

## Code Metrics

### Before Phase 3
- **TimesheetService**: ~7,893 lines
- **Document Code**: ~532 lines (scattered across methods)
- **Document Methods**: 9 methods

### After Phase 3
- **TimesheetService**: ~7,366 lines (reduced by ~527 lines)
- **TimesheetDocumentService**: ~530 lines (new)
- **Document Code**: Centralized in document service
- **Document Methods**: 9 methods (moved to document service)

## Backup Location
All original files have been backed up to:
```
backups/timesheet_refactoring/phase3/
├── TimesheetController.java
└── TimesheetService.java
```

## Testing Recommendations

### Unit Tests for TimesheetDocumentService

1. **Test `handleDocumentUpload()`**
   - Valid: Document uploaded successfully
   - Invalid: Missing document metadata
   - Invalid: Document save fails

2. **Test `addTimesheetDocument()`**
   - Valid: Create operation
   - Valid: Update operation
   - Invalid: Missing file
   - Invalid: Invalid operation type

3. **Test `fetchTimesheetDocument()`**
   - Valid: Fetch by docId
   - Valid: Fetch by timesheetId
   - Invalid: Neither parameter provided

4. **Test `getDocumentDataByDocId()`**
   - Valid: Document found
   - Invalid: Document not found
   - Invalid: Document inactive

5. **Test `approveOrRejectDocument()`**
   - Valid: Document approved
   - Valid: Document rejected
   - Invalid: Invalid approval status
   - Invalid: Missing parameters

6. **Test `replaceAllTemporaryFileWithFinalFile()`**
   - Valid: Files replaced successfully
   - Invalid: Missing parameters
   - Invalid: File empty
   - Edge case: No documents found

7. **Test `getAllDisabledDateListForBulkDocSubmit()`**
   - Valid: Disabled dates returned
   - Invalid: Missing parameters
   - Edge case: No disabled dates

### Integration Tests for TimesheetService

1. **Test `addTimesheet()` with document service**
   - Verify document service is called correctly
   - Verify document upload succeeds
   - Verify document upload failures are handled

2. **Test document-related endpoints**
   - Verify delegate methods call document service correctly
   - Verify responses are returned correctly

## Migration Notes
- **Backward Compatible**: No changes to API contracts or data structures
- **No Database Changes**: This phase only affects code organization
- **No Breaking Changes**: Existing functionality remains unchanged
- **Error Messages**: Document error messages remain the same

## Dependencies
- **TimesheetDocumentService** depends on:
  - `TimesheetDocumentDetailsRepository` (for document persistence)
  - `TimesheetsRepository` (for bulk operations)
  - `EmployeeLeaveRepository` (for disabled dates calculation)
  - `LogService` (for logging)
  - `HttpServletRequest` (for logging)

## Circular Dependency Resolution
The document service needs access to `TimesheetsRepository` for bulk operations. This is a valid dependency as documents are related to timesheets.

## Next Steps (Phase 4)
Phase 4 will focus on extracting approval-related logic into a `TimesheetApprovalService`. This will further reduce the size of `TimesheetService` and improve separation of concerns.

## Files Summary
- **Created**: 1 file (TimesheetDocumentService.java - ~530 lines)
- **Modified**: 1 file (TimesheetService.java - reduced by ~527 lines)
- **Backed Up**: 2 files
- **Document Methods Extracted**: 9 methods
- **Lines Removed**: ~527 lines
- **Lines Added**: ~530 lines (document service with documentation)

## Impact Assessment
- **Risk Level**: Low (isolated change, document logic extracted cleanly)
- **Testing Required**: Unit tests for document service, integration tests for service
- **Rollback Plan**: Restore files from backup directory if needed
- **Performance Impact**: Negligible (same document logic, just reorganized)

