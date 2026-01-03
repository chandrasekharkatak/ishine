# Timesheet `_new` Tables Integration - Complete Impact Analysis

## Executive Summary

**Integration Readiness Score: 35/100**

**Critical Blockers:**
1. Status calculation logic must be implemented 
2. Day-level vs Project-level data separation requires complete business logic rewrite
3. 200+ native SQL queries need migration
4. Document storage model change (BLOB → file_url) requires file system integration
5. Status type change (String → Integer) breaks all existing status comparisons

**Safe Integration Order:**
1. Master tables (day_type_master_new, status_master_new, doc_mime_type_master_new, client_status_master_new)
2. Read-only queries for reporting (Phase 1)
3. Document upload/download with file_url (Phase 2)
4. Activity-level CRUD operations (Phase 3)
5. Status calculation and approval flows (Phase 4)
6. Full write operations migration (Phase 5)

---

## PHASE 1: DATABASE ALIGNMENT ANALYSIS

### 1.1 Table Comparison Matrix

#### **employee_timesheets_new vs EmployeeTimesheets**

| Aspect | Old Table | New Table | Impact |
|--------|-----------|----------|--------|
| **Table Name** | `EmployeeTimesheets` | `employee_timesheets_new` | All queries need update |
| **Status** | `String status` ("Pending", "Approved", "Rejected") | `Integer status` (calculated) | **HIGH** - All status logic breaks |
| **Day Type** | `String dayType` | `Integer dayTypeId` (FK to master) | **HIGH** - Requires master lookup |
| **Total Time** | `Float totalTime` | `Integer totalWorkingMinutes` | **MEDIUM** - Unit conversion needed |
| **Project Info** | `Integer projectId` (at day level) | Moved to `project_timesheet_status_new` | **HIGH** - Data model change |
| **Client Info** | `clientInTime`, `clientOutTime`, `clientApprovalStatus`, `shadowEmpId` at day level | Moved to `project_timesheet_status_new` | **HIGH** - Requires JOIN |
| **Rejection** | `Long rejectionId` | Moved to `timesheet_rejection_details_new` | **MEDIUM** - Requires JOIN |
| **Description** | `String description` (at day level) | Removed from day, moved to activity level | **HIGH** - Logic change |
| **Common Properties** | Embedded `CommonProperties` | Explicit `created_by`, `created_on`, `updated_by`, `updated_on` | **LOW** - Mapping change |
| **Removed Fields** | `remarks`, `currentManagerId`, `timesheetStatusUpdatedBy`, `clientSideId`, `hasClientSideId`, `escalationFlag`, `currentEscalationLevel`, `totalWorkingHours`, `totalClientWorkingHours`, `isShadowTimesheet` | N/A | **MEDIUM** - Business logic may depend on these |

**Key Architectural Change:**
- **Old Model**: 1 timesheet = 1 day = 1 project (denormalized)
- **New Model**: 1 timesheet = 1 day, multiple activities = multiple projects (normalized)

---

#### **employee_timesheet_activities_mapping_new vs EmployeeTimesheetActivitiesMapping**

| Aspect | Old Table | New Table | Impact |
|--------|-----------|----------|--------|
| **Primary Key** | `Long timesheetActivityMapId` (auto-increment) | Composite: `(timesheetId, activityId, projectId)` | **HIGH** - All lookups change |
| **Project** | Derived from activity → team → project | `Long projectId` (direct FK) | **MEDIUM** - Simpler queries |
| **Duration** | `Float completionTime` | `Short durationMinutes` | **MEDIUM** - Unit/type conversion |
| **Description** | `String description` | `String description` (moved from day level) | **MEDIUM** - Logic change |
| **Client Location** | `Integer clientLocationId` | Removed | **LOW** - May need to add back if used |

**Key Change:**
- Activities now directly reference projects (no need to traverse team hierarchy)
- Multiple activities per timesheet can belong to different projects

---

#### **project_timesheet_status_new (NEW TABLE)**

| Field | Type | Purpose | Impact |
|-------|------|---------|--------|
| `timesheet_id` + `project_id` | Composite PK | Links timesheet to project | **HIGH** - New JOIN required |
| `po_no` | VARCHAR(255) | PO number per project | **MEDIUM** - New field to handle |
| `client_in_time` | DATETIME | Client check-in time | **MEDIUM** - Moved from day level |
| `client_out_time` | DATETIME | Client check-out time | **MEDIUM** - Moved from day level |
| `is_night_shift` | TINYINT | Night shift flag | **LOW** - Moved from day level |
| `client_approval_status` | TINYINT (FK) | Client approval status ID | **HIGH** - Now project-level, not day-level |
| `status` | TINYINT | Project-level status | **HIGH** - New concept |
| `shadow_emp_id` | BIGINT | Shadow employee | **MEDIUM** - Moved from day level |
| `total_client_working_minutes` | SMALLINT | Client working time | **MEDIUM** - Moved from day level |

**Critical Insight:**
- **Same day can have different approval statuses per project**
- **Status must be aggregated from project-level to day-level**

---

#### **timesheet_document_details_new vs timesheet_document_details**

| Aspect | Old Table | New Table | Impact |
|--------|-----------|----------|--------|
| **File Storage** | `byte[] docData` (BLOB) | `String fileUrl` (VARCHAR) | **HIGH** - Requires file system |
| **MIME Type** | `String docMimeType` | `Integer mimeTypeId` (FK) | **MEDIUM** - Requires master lookup |
| **Client Approval** | `String clientApprovalStatus` | `Integer clientApprovalStatusId` (FK) | **HIGH** - Requires master lookup |
| **RM/HR Approval** | `String rmApprovalStatus`, `String hrApprovalStatus` | **REMOVED** | **HIGH** - Approval logic breaks |
| **Emp ID** | `Long empId` | **REMOVED** | **MEDIUM** - Must derive from timesheet |

**Critical Change:**
- Documents now stored as files (S3/local storage), not in database
- Approval hierarchy simplified (only client approval tracked)

---

#### **timesheet_rejection_details_new (NEW TABLE)**

| Field | Type | Purpose | Impact |
|-------|------|---------|--------|
| `timesheet_id` + `project_id` + `rejection_id` | Composite PK | Project-level rejection | **HIGH** - Rejection now project-specific |
| `remarks` | VARCHAR(512) | Rejection remarks | **MEDIUM** - Moved from main table |

**Key Change:**
- Rejection is now **project-level**, not day-level
- Same day can be rejected for one project, approved for another

---

### 1.2 Missing Foreign Keys & Indexes

**Required Foreign Keys:**
1. `employee_timesheets_new.day_type_id` → `day_type_master_new.day_type_id`
2. `employee_timesheets_new.leave_type_master_id` → `leave_type_master.leave_type_master_id`
3. `employee_timesheet_activities_mapping_new.project_id` → `projects.project_id`
4. `employee_timesheet_activities_mapping_new.activity_id` → `activities.activity_id`
5. `project_timesheet_status_new.timesheet_id` → `employee_timesheets_new.timesheet_id`
6. `project_timesheet_status_new.project_id` → `projects.project_id`
7. `project_timesheet_status_new.client_approval_status` → `client_status_master_new.status_id`
8. `timesheet_document_details_new.mime_type_id` → `doc_mime_type_master_new.mime_type_id`
9. `timesheet_document_details_new.client_approval_status_id` → `client_status_master_new.status_id`
10. `timesheet_rejection_details_new.rejection_id` → `timesheet_rejection_reasons_master.rejection_id`

**Required Indexes:**
1. `employee_timesheets_new(emp_id, date)` - For employee date queries
2. `employee_timesheets_new(date)` - For date range queries
3. `employee_timesheet_activities_mapping_new(timesheet_id)` - For activity lookups
4. `project_timesheet_status_new(timesheet_id)` - For status aggregation
5. `timesheet_document_details_new(timesheet_id, active)` - For document queries
6. `timesheet_rejection_details_new(timesheet_id)` - For rejection lookups

---

### 1.3 Table Replacement Matrix

| Old Table | New Table(s) | Coexistence Required? | Migration Strategy |
|-----------|--------------|----------------------|-------------------|
| `EmployeeTimesheets` | `employee_timesheets_new` | **YES** (dual-write initially) | Gradual migration |
| `EmployeeTimesheetActivitiesMapping` | `employee_timesheet_activities_mapping_new` | **YES** | Gradual migration |
| `timesheet_document_details` | `timesheet_document_details_new` | **YES** | Gradual migration + file migration |
| N/A | `project_timesheet_status_new` | **NO** (new concept) | New writes only |
| N/A | `timesheet_rejection_details_new` | **NO** (new concept) | New writes only |

---

## PHASE 2: BACKEND IMPACT ANALYSIS

### 2.1 Controller Layer Impact

#### **APIs Requiring Request/Response Changes**

| Endpoint | Current Behavior | Required Change | Risk Level | Postponable? |
|----------|-----------------|-----------------|------------|--------------|
| `/api/addTimesheetWithClient` | Accepts single projectId in DTO | Must accept multiple activities with projectIds | **HIGH** | **NO** |
| `/api/updateTimesheet` | Updates single project timesheet | Must update multiple project statuses | **HIGH** | **NO** |
| `/api/getAllMyTimesheetsByEmpId` | Returns flat list | Must group by project, return nested structure | **MEDIUM** | **YES** |
| `/api/getAllMyActivitiesByTimesheetId` | Returns activities | Must include projectId in response | **LOW** | **YES** |
| `/api/getMyReporteesTimesheetRequests` | Filters by day-level status | Must aggregate project-level statuses | **HIGH** | **NO** |
| `/api/updateTimesheetRequestById` | Updates day-level status | Must update project-level statuses | **HIGH** | **NO** |
| `/api/bulkApproveTimesheetRequest` | Approves day-level | Must approve project-level | **HIGH** | **NO** |
| `/api/bulkRejectTimesheetRequest` | Rejects day-level | Must reject project-level with remarks | **HIGH** | **NO** |
| `/api/getDocumentDataByDocId` | Returns BLOB | Must return file URL or stream file | **HIGH** | **NO** |
| `/api/bulkFinalDocumentUpload` | Stores BLOB | Must store file and save URL | **HIGH** | **NO** |

#### **APIs Needing Status Calculation Logic**

| Endpoint | Current Logic | Required Change | Risk Level |
|----------|---------------|-----------------|------------|
| `/api/getMyReporteesTimesheetRequests` | Direct status filter | Aggregate project statuses → day status | **HIGH** |
| `/api/getMyReporteesApprovedTimesheets` | Direct status filter | Aggregate project statuses → day status | **HIGH** |
| `/api/getTimesheetsForHomePageByEmpId` | Direct status filter | Aggregate project statuses → day status | **HIGH** |
| `/api/getAllMyTeamTimesheets` | Direct status filter | Aggregate project statuses → day status | **MEDIUM** |

**Status Aggregation Rules (TO BE DEFINED):**
- If ANY project is "Pending" → Day status = "Pending"
- If ALL projects are "Approved" → Day status = "Approved"
- If ANY project is "Rejected" → Day status = "Rejected"?
- Mixed statuses → Day status = "Partially Approved"?

---

### 2.2 Service Layer Impact

#### **Services Requiring Complete Rewrite**

**1. TimesheetService.addTimesheet()**
- **Location**: `TimesheetService.java:729`
- **Current Logic**: Creates single timesheet with single projectId
- **Required Change**: 
  - Create `EmployeeTimesheetsNew` (day-level)
  - Create multiple `EmployeeTimesheetActivitiesMappingNew` (one per activity)
  - Create multiple `ProjectTimesheetStatusNew` (one per project)
  - Calculate and set day-level status from project statuses
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**2. TimesheetService.updateTimesheet()**
- **Location**: `TimesheetService.java:1461`
- **Current Logic**: Updates single timesheet entity
- **Required Change**:
  - Update `EmployeeTimesheetsNew`
  - Update/Insert `EmployeeTimesheetActivitiesMappingNew` entries
  - Update/Insert `ProjectTimesheetStatusNew` entries
  - Recalculate day-level status
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**3. TimesheetApprovalService.updateTimesheetRequestById()**
- **Location**: `TimesheetApprovalService.java`
- **Current Logic**: Updates day-level status
- **Required Change**:
  - Update project-level statuses in `project_timesheet_status_new`
  - Recalculate day-level status in `employee_timesheets_new`
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**4. TimesheetApprovalService.bulkApproveTimesheetRequest()**
- **Location**: `TimesheetApprovalService.java`
- **Current Logic**: Bulk updates day-level status
- **Required Change**:
  - Bulk update project-level statuses
  - Recalculate day-level statuses
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**5. TimesheetDocumentService.handleDocumentUpload()**
- **Location**: `TimesheetDocumentService.java:76`
- **Current Logic**: Stores BLOB in database
- **Required Change**:
  - Upload file to storage (S3/local)
  - Store file URL in `timesheet_document_details_new`
  - Map MIME type string to `mime_type_id`
  - Map approval status string to `client_approval_status_id`
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**6. TimesheetDocumentService.replaceAllTemporaryFileWithFinalFile()**
- **Location**: `TimesheetDocumentService.java:383`
- **Current Logic**: Updates BLOB references
- **Required Change**:
  - Upload file to storage
  - Update file URLs in `timesheet_document_details_new`
  - Update `bulk_approved_doc_id` to reference `final_document_new`
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

#### **Services Requiring Logic Changes**

**1. TimesheetQueryService.getAllMyTimesheetsByEmpId()**
- **Location**: `TimesheetQueryService.java`
- **Current Logic**: Single query returns flat list
- **Required Change**: 
  - JOIN `employee_timesheets_new` + `employee_timesheet_activities_mapping_new` + `project_timesheet_status_new`
  - Group activities by timesheet
  - Group projects by timesheet
  - Aggregate statuses
- **Risk Level**: **MEDIUM**
- **Postponable**: **YES**

**2. TimesheetQueryService.getAllMyTeamTimesheets()**
- **Location**: `TimesheetQueryService.java`
- **Current Logic**: Single query with project filter
- **Required Change**: 
  - JOIN multiple tables
  - Filter by project in `project_timesheet_status_new`
  - Aggregate statuses
- **Risk Level**: **MEDIUM**
- **Postponable**: **YES**

**3. TimesheetDashboardService.getTimesheetDashboardCountForEmployee()**
- **Location**: `TimesheetDashboardService.java`
- **Current Logic**: Counts by day-level status
- **Required Change**:
  - Aggregate project-level statuses
  - Count by aggregated day-level status
- **Risk Level**: **MEDIUM**
- **Postponable**: **YES**

#### **Logic Currently Assuming 1 Timesheet = 1 Project**

**Affected Methods:**
1. `TimesheetService.addTimesheet()` - **HIGH**
2. `TimesheetService.updateTimesheet()` - **HIGH**
3. `TimesheetApprovalService.approveTimesheetRequest()` - **HIGH**
4. `TimesheetApprovalService.revokeApprovedTimesheet()` - **HIGH**
5. All status filtering logic - **HIGH**

**Required Changes:**
- All methods must handle multiple projects per day
- Status must be aggregated from project-level
- Approval/rejection must be project-specific

---

### 2.3 Repository Layer Impact

#### **Repositories to be Replaced/Duplicated**

| Old Repository | New Repository | Strategy |
|----------------|----------------|----------|
| `TimesheetsRepository` | `EmployeeTimesheetsNewRepository` | Create new, keep old for migration |
| `TimesheetActivityMapRepository` | `TimesheetActivityMapNewRepository` | Create new, keep old for migration |
| `TimesheetDocumentDetailsRepository` | `TimesheetDocumentDetailsNewRepository` | Create new, keep old for migration |
| N/A | `ProjectTimesheetStatusNewRepository` | Create new |
| N/A | `TimesheetRejectionDetailsNewRepository` | Create new |

#### **Native SQL Queries Requiring Migration**

**Count: ~200+ queries across repositories**

**Critical Queries:**

**1. TimesheetsRepository.getAllEmployeeTimesheetsBetweenDates()**
- **Location**: `TimesheetsRepository.java:303`
- **Current**: `FROM employee_timesheets et LEFT JOIN employee_timesheet_activities_mapping map`
- **Required**: `FROM employee_timesheets_new et LEFT JOIN employee_timesheet_activities_mapping_new map LEFT JOIN project_timesheet_status_new pts`
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**2. TimesheetsRepository.getMyReporteesTimesheetRequests()**
- **Location**: `TimesheetsRepository.java:433`
- **Current**: Filters by `et.status = :status`
- **Required**: Aggregate project statuses, then filter
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**3. TimesheetsRepository.getProjectViewForClientAttendanceStatus()**
- **Location**: `TimesheetsRepository.java:4709` (700+ lines of complex CTE)
- **Current**: Uses `employee_timesheets`, `timesheet_document_details`
- **Required**: Complete rewrite with new tables
- **Risk Level**: **CRITICAL**
- **Postponable**: **NO**

**4. EmployeeRepository.getTimesheetData()**
- **Location**: `EmployeeRepository.java:576`
- **Current**: `FROM employee_timesheets et INNER JOIN employee_timesheet_activities_mapping etam`
- **Required**: Update to `_new` tables + JOIN `project_timesheet_status_new`
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**5. TimesheetActivityMapRepository.activitiesByTimesheetId()**
- **Location**: `TimesheetActivityMapRepository.java:14`
- **Current**: `FROM employee_timesheet_activities_mapping map`
- **Required**: `FROM employee_timesheet_activities_mapping_new map`
- **Risk Level**: **MEDIUM**
- **Postponable**: **YES**

#### **JPQL Queries Broken Due to Column Mismatch**

**All queries using:**
- `status` (String) → `status` (Integer)
- `dayType` (String) → `dayTypeId` (Integer)
- `totalTime` (Float) → `totalWorkingMinutes` (Integer)
- `projectId` (in main table) → Moved to `project_timesheet_status_new`

**Example:**
```java
// OLD (BROKEN)
@Query("SELECT t FROM Timesheet t WHERE t.status = :status")
List<Timesheet> findByStatus(String status);

// NEW (REQUIRED)
@Query("SELECT t FROM EmployeeTimesheetsNew t WHERE t.status = :status")
List<EmployeeTimesheetsNew> findByStatus(Integer status);
```

---

### 2.4 DTOs & Mapping Impact

#### **DTOs Needing New Fields**

**1. TimesheetDTO**
- **Location**: `TimesheetDTO.java`
- **Current Fields**: `projectId` (single), `status` (String), `dayType` (String)
- **Required Changes**:
  - Remove `projectId` (single)
  - Add `activities: List<ActivityDTO>` (each with `projectId`, `activityId`, `durationMinutes`, `description`)
  - Change `status` to `Integer` or keep `String` with conversion
  - Change `dayType` to `dayTypeId: Integer` or keep `String` with lookup
  - Add `projectStatuses: List<ProjectStatusDTO>` (for project-level statuses)
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**2. TimesheetDocumentDetailsDTO**
- **Location**: `TimesheetDocumentDetailsDTO.java`
- **Current Fields**: `docData` (byte[]), `docMimeType` (String), `clientApprovalStatus` (String)
- **Required Changes**:
  - Remove `docData` (byte[])
  - Add `fileUrl: String`
  - Change `docMimeType` to `mimeTypeId: Integer` or keep `String` with lookup
  - Change `clientApprovalStatus` to `clientApprovalStatusId: Integer` or keep `String` with lookup
  - Remove `rmApprovalStatus`, `hrApprovalStatus` (if not needed)
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

#### **DTOs Incorrectly Flattening Data**

**1. TimesheetDTO (Current)**
```java
// CURRENT (FLATTENED)
projectId: Integer
status: String
```

**Required (Nested)**
```java
// REQUIRED (NESTED)
activities: [
  {
    projectId: Integer,
    activityId: Long,
    durationMinutes: Short,
    description: String
  }
]
projectStatuses: [
  {
    projectId: Integer,
    status: Integer,
    clientApprovalStatus: Integer,
    clientInTime: LocalDateTime,
    clientOutTime: LocalDateTime
  }
]
```

#### **Mapping Logic Needing Activity-Wise Grouping**

**All methods mapping `Timesheet` entity to `TimesheetDTO` must:**
1. Group activities by `timesheetId`
2. Group project statuses by `timesheetId`
3. Aggregate status from project statuses
4. Map `dayTypeId` → `dayType` string (via master lookup)

**Affected Methods:**
- `TimesheetService.addTimesheet()` - **HIGH**
- `TimesheetService.updateTimesheet()` - **HIGH**
- `TimesheetQueryService.getAllMyTimesheetsByEmpId()` - **MEDIUM**
- `TimesheetQueryService.getAllMyTeamTimesheets()` - **MEDIUM**

---

## PHASE 3: APPROVAL & STATUS FLOW IMPACT

### 3.1 Day-Level vs Project-Level Approval

**Current Model:**
- Approval is **day-level**
- One approval status per day
- Same status applies to all projects

**New Model:**
- Approval is **project-level**
- Different projects can have different approval statuses
- Day-level status must be **calculated** from project statuses

**Impact:**
- **All approval logic must be rewritten**
- **Status aggregation rules must be defined**
- **UI must show project-level approval statuses**

### 3.2 Mixed Approval Status Handling

**Scenario:** Same day, multiple projects:
- Project A: "Approved"
- Project B: "Pending"
- Project C: "Rejected"

**Day-Level Status Options:**
1. **Most Restrictive**: "Rejected" (if any rejected)
2. **Most Permissive**: "Pending" (if any pending)
3. **Weighted**: Calculate based on project importance
4. **Separate Tracking**: Don't aggregate, show project-wise

**Required Decision:** Which aggregation rule to use?

### 3.3 Overall Day Status Derivation Rules

**Status Calculation Logic (TO BE IMPLEMENTED):**

```java
// PSEUDOCODE
Integer calculateDayStatus(Long timesheetId) {
    List<ProjectTimesheetStatusNew> projectStatuses = 
        projectTimesheetStatusRepository.findByTimesheetId(timesheetId);
    
    if (projectStatuses.isEmpty()) {
        return STATUS_PENDING; // No projects = pending
    }
    
    boolean hasRejected = projectStatuses.stream()
        .anyMatch(ps -> ps.getStatus() == STATUS_REJECTED);
    if (hasRejected) {
        return STATUS_REJECTED; // Any rejected = day rejected
    }
    
    boolean hasPending = projectStatuses.stream()
        .anyMatch(ps -> ps.getStatus() == STATUS_PENDING);
    if (hasPending) {
        return STATUS_PENDING; // Any pending = day pending
    }
    
    boolean allApproved = projectStatuses.stream()
        .allMatch(ps -> ps.getStatus() == STATUS_APPROVED);
    if (allApproved) {
        return STATUS_APPROVED; // All approved = day approved
    }
    
    return STATUS_PENDING; // Default
}
```

**Affected Methods:**
- `TimesheetService.addTimesheet()` - Must calculate after insert
- `TimesheetService.updateTimesheet()` - Must recalculate after update
- `TimesheetApprovalService.approveTimesheetRequest()` - Must recalculate
- `TimesheetApprovalService.bulkApproveTimesheetRequest()` - Must recalculate
- All query methods - Must aggregate in queries

### 3.4 Client-Side Approval Mapping

**Current:**
- `clientApprovalStatus` stored as String in main table
- Single value per day

**New:**
- `clientApprovalStatus` stored as Integer (FK) in `project_timesheet_status_new`
- Multiple values per day (one per project)

**Required Changes:**
- Map status strings to IDs via `client_status_master_new`
- Store project-level approval statuses
- Aggregate for day-level display

### 3.5 Rejection Reason Handling

**Current:**
- `rejectionId` stored in main table
- Single rejection per day

**New:**
- Rejection stored in `timesheet_rejection_details_new`
- Multiple rejections per day (one per project)
- Composite key: `(timesheetId, projectId, rejectionId)`

**Required Changes:**
- Store rejection per project
- Query rejections by project
- Display project-wise rejection reasons

---

## PHASE 4: DOCUMENT MANAGEMENT IMPACT

### 4.1 Document Upload Changes

**Current Flow:**
1. Receive `MultipartFile`
2. Convert to `byte[]`
3. Store in `timesheet_document_details.doc_data` (BLOB)

**New Flow:**
1. Receive `MultipartFile`
2. Upload to file storage (S3/local filesystem)
3. Get file URL
4. Store URL in `timesheet_document_details_new.file_url`
5. Map MIME type to `mime_type_id`
6. Map approval status to `client_approval_status_id`

**Affected Methods:**
- `TimesheetDocumentService.handleDocumentUpload()` - **HIGH**
- `TimesheetDocumentService.addTimesheetDocument()` - **HIGH**
- `TimesheetController.addTimesheetWithClient()` - **MEDIUM**

**Required Infrastructure:**
- File storage service (S3 client or local file system)
- File upload/download endpoints
- File cleanup on delete

### 4.2 Document Approval Changes

**Current:**
- `clientApprovalStatus`, `rmApprovalStatus`, `hrApprovalStatus` as Strings

**New:**
- Only `clientApprovalStatusId` as Integer (FK)
- RM/HR approval removed

**Impact:**
- Approval hierarchy simplified
- Existing RM/HR approval logic must be removed or migrated
- UI must be updated to remove RM/HR approval steps

### 4.3 Final Document Flow

**Current:**
- `FinalDocument` stores BLOB
- `bulkApprovedDocId` references `FinalDocument.docId`

**New:**
- `final_document_new` stores `file_url`
- `bulkApprovedDocId` still references `final_document_new.final_doc_id`

**Required Changes:**
- Upload final document to file storage
- Store URL in `final_document_new.file_url`
- Update `bulkApprovedDocId` references

### 4.4 Bulk Approval Flow

**Current:**
- Updates multiple `timesheet_document_details` records
- Links to single `FinalDocument` BLOB

**New:**
- Updates multiple `timesheet_document_details_new` records
- Links to single `final_document_new` file URL
- Must upload file once, reference URL multiple times

### 4.5 MIME Type & Client Approval Status Handling

**Current:**
- Stored as Strings: `"image/png"`, `"Approved"`, `"Pending"`

**New:**
- Stored as Integer FKs: `mime_type_id`, `client_approval_status_id`
- Must lookup from master tables

**Required Changes:**
- Create/use `doc_mime_type_master_new` repository
- Create/use `client_status_master_new` repository
- Map strings to IDs in service layer
- Cache master data for performance

---

## PHASE 5: REPORTING & DASHBOARD IMPACT

### 5.1 Reports Broken Due to `_new` Tables

**1. Employee Timesheet Report**
- **Location**: `TimesheetQueryService.getAllOrDeptWiseEmployeeTimesheetReport()`
- **Current**: Single query with `employee_timesheets`
- **Required**: JOIN `employee_timesheets_new` + `employee_timesheet_activities_mapping_new` + `project_timesheet_status_new`
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**2. Project View for Client Attendance Status**
- **Location**: `TimesheetsRepository.getProjectViewForClientAttendanceStatus()`
- **Current**: 700+ line CTE using old tables
- **Required**: Complete rewrite with new tables
- **Risk Level**: **CRITICAL**
- **Postponable**: **NO**

**3. Employee View for Client Attendance Status**
- **Location**: `TimesheetService.getEmployeeViewForClientAttendanceStatus()`
- **Current**: Uses old tables
- **Required**: Rewrite with new tables
- **Risk Level**: **HIGH**
- **Postponable**: **NO**

**4. Dashboard Counts**
- **Location**: `TimesheetDashboardService.getTimesheetDashboardCountForEmployee()`
- **Current**: Counts by day-level status
- **Required**: Aggregate project statuses, then count
- **Risk Level**: **MEDIUM**
- **Postponable**: **YES**

### 5.2 Queries Needing Aggregation from Activity Level

**All queries that:**
- Filter by project → Must JOIN `project_timesheet_status_new`
- Filter by status → Must aggregate project statuses
- Count activities → Must aggregate from `employee_timesheet_activities_mapping_new`
- Sum durations → Must sum `duration_minutes` from activities

**Example:**
```sql
-- OLD
SELECT COUNT(*) FROM employee_timesheets 
WHERE project_id = :projectId AND status = 'Approved'

-- NEW (REQUIRED)
SELECT COUNT(DISTINCT et.timesheet_id) 
FROM employee_timesheets_new et
INNER JOIN project_timesheet_status_new pts 
  ON et.timesheet_id = pts.timesheet_id
WHERE pts.project_id = :projectId 
  AND pts.status = :approvedStatusId
```

### 5.3 Performance Risks

**1. N+1 Query Problem**
- **Risk**: Loading timesheet → loading activities → loading project statuses
- **Mitigation**: Use JOIN FETCH or batch loading
- **Risk Level**: **HIGH**

**2. Status Aggregation Overhead**
- **Risk**: Calculating day-level status for every query
- **Mitigation**: Cache calculated status or use materialized view
- **Risk Level**: **MEDIUM**

**3. File URL Lookups**
- **Risk**: Multiple file storage calls for document lists
- **Mitigation**: Batch file URL retrieval or CDN
- **Risk Level**: **MEDIUM**

**4. Complex JOINs**
- **Risk**: 4-5 table JOINs for simple queries
- **Mitigation**: Create database views or materialized views
- **Risk Level**: **MEDIUM**

### 5.4 Temporary Views for Backward Compatibility

**Recommended Views:**

**1. `v_timesheet_legacy`**
```sql
CREATE VIEW v_timesheet_legacy AS
SELECT 
    et.timesheet_id,
    et.emp_id,
    et.date,
    dtm.day_type,
    et.status,
    -- Aggregate project statuses to day status
    CASE 
        WHEN EXISTS (SELECT 1 FROM project_timesheet_status_new pts 
                     WHERE pts.timesheet_id = et.timesheet_id 
                     AND pts.status = :rejectedStatusId) 
        THEN 'Rejected'
        WHEN EXISTS (SELECT 1 FROM project_timesheet_status_new pts 
                     WHERE pts.timesheet_id = et.timesheet_id 
                     AND pts.status = :pendingStatusId) 
        THEN 'Pending'
        WHEN NOT EXISTS (SELECT 1 FROM project_timesheet_status_new pts 
                         WHERE pts.timesheet_id = et.timesheet_id 
                         AND pts.status != :approvedStatusId) 
        THEN 'Approved'
        ELSE 'Pending'
    END AS calculated_status,
    -- Other fields...
FROM employee_timesheets_new et
LEFT JOIN day_type_master_new dtm ON et.day_type_id = dtm.day_type_id;
```

**2. `v_timesheet_activities_legacy`**
```sql
CREATE VIEW v_timesheet_activities_legacy AS
SELECT 
    etam.timesheet_id,
    etam.activity_id,
    etam.project_id,
    etam.description,
    etam.duration_minutes AS completion_time,
    -- Other fields...
FROM employee_timesheet_activities_mapping_new etam;
```

**Benefits:**
- Gradual migration path
- Backward compatibility
- Can be dropped after full migration

---

## DETAILED FINDINGS MATRIX

### Finding #1: Status Type Change (String → Integer)

| Aspect | Details |
|--------|---------|
| **Issue Description** | Status changed from String ("Pending", "Approved", "Rejected") to Integer (calculated) |
| **Exact Location** | All methods using `timesheet.getStatus()`, `timesheet.setStatus()`, status comparisons |
| **Why it breaks** | All String comparisons (`status.equals("Pending")`) will fail with Integer |
| **Required Change** | Create status enum/mapper, convert Integer ↔ String, update all comparisons |
| **Risk Level** | **HIGH** |
| **Postponable** | **NO** |

### Finding #2: Project Moved to Separate Table

| Aspect | Details |
|--------|---------|
| **Issue Description** | `projectId` removed from main timesheet table, moved to `project_timesheet_status_new` |
| **Exact Location** | All queries filtering/joining by `projectId`, all DTOs with single `projectId` |
| **Why it breaks** | Queries like `WHERE project_id = :projectId` no longer work on main table |
| **Required Change** | JOIN `project_timesheet_status_new`, handle multiple projects per day |
| **Risk Level** | **HIGH** |
| **Postponable** | **NO** |

### Finding #3: Activities Now Include Project Directly

| Aspect | Details |
|--------|---------|
| **Issue Description** | Activities now have direct `projectId`, no longer derived from team hierarchy |
| **Exact Location** | `TimesheetActivityMapRepository.activitiesByTimesheetId()`, activity mapping logic |
| **Why it breaks** | Queries joining through team → project no longer needed, but must include projectId |
| **Required Change** | Simplify queries, add projectId to activity DTOs |
| **Risk Level** | **MEDIUM** |
| **Postponable** | **YES** |

### Finding #4: Document Storage Changed (BLOB → File URL)

| Aspect | Details |
|--------|---------|
| **Issue Description** | Documents stored as BLOB in DB → stored as files, URL in DB |
| **Exact Location** | `TimesheetDocumentService.handleDocumentUpload()`, `TimesheetDocumentService.replaceAllTemporaryFileWithFinalFile()`, document retrieval methods |
| **Why it breaks** | `docData` byte[] no longer exists, must retrieve files from storage |
| **Required Change** | Implement file storage service, upload/download files, store URLs |
| **Risk Level** | **HIGH** |
| **Postponable** | **NO** |

### Finding #5: Status Calculation Logic Missing

| Aspect | Details |
|--------|---------|
| **Issue Description** | Day-level status marked as "calculated logically" but logic not defined |
| **Exact Location** | `EmployeeTimesheetsNew.status` field |
| **Why it breaks** | Cannot insert/update timesheets without knowing how to calculate status |
| **Required Change** | Define and implement status aggregation rules from project-level statuses |
| **Risk Level** | **CRITICAL** |
| **Postponable** | **NO** |

### Finding #6: Rejection Now Project-Level

| Aspect | Details |
|--------|---------|
| **Issue Description** | Rejection moved from day-level (`rejectionId` in main table) to project-level (`timesheet_rejection_details_new`) |
| **Exact Location** | Rejection handling in `TimesheetApprovalService`, rejection queries |
| **Why it breaks** | Single rejection per day → multiple rejections per day (one per project) |
| **Required Change** | Store rejection per project, query rejections by project, update UI |
| **Risk Level** | **MEDIUM** |
| **Postponable** | **YES** |

### Finding #7: Client Approval Status Now Integer FK

| Aspect | Details |
|--------|---------|
| **Issue Description** | `clientApprovalStatus` changed from String to Integer FK to `client_status_master_new` |
| **Exact Location** | Document approval logic, status comparisons |
| **Why it breaks** | String comparisons (`status.equals("Approved")`) fail with Integer |
| **Required Change** | Map strings to IDs via master table, update all comparisons |
| **Risk Level** | **HIGH** |
| **Postponable** | **NO** |

### Finding #8: RM/HR Approval Removed

| Aspect | Details |
|--------|---------|
| **Issue Description** | `rmApprovalStatus` and `hrApprovalStatus` removed from document table |
| **Exact Location** | Approval hierarchy logic, document approval workflows |
| **Why it breaks** | Existing RM/HR approval logic references non-existent fields |
| **Required Change** | Remove RM/HR approval logic or migrate to new approval model |
| **Risk Level** | **MEDIUM** |
| **Postponable** | **YES** (if approval hierarchy can be simplified) |

### Finding #9: Day Type Now Integer FK

| Aspect | Details |
|--------|---------|
| **Issue Description** | `dayType` changed from String to `dayTypeId` Integer FK to `day_type_master_new` |
| **Exact Location** | All day type comparisons, filters, displays |
| **Why it breaks** | String comparisons (`dayType.equals("Working")`) fail with Integer |
| **Required Change** | Map strings to IDs via master table, update all comparisons |
| **Risk Level** | **MEDIUM** |
| **Postponable** | **YES** |

### Finding #10: Complex Reporting Query Requires Complete Rewrite

| Aspect | Details |
|--------|---------|
| **Issue Description** | `getProjectViewForClientAttendanceStatus()` is 700+ line CTE using old tables |
| **Exact Location** | `TimesheetsRepository.java:4709` |
| **Why it breaks** | References old table names, old column names, old data model |
| **Required Change** | Complete rewrite with new tables, new JOINs, new aggregation logic |
| **Risk Level** | **CRITICAL** |
| **Postponable** | **NO** |

---

## INTEGRATION READINESS ASSESSMENT

### Overall Score: 35/100

**Breakdown:**
- Database Schema: 40/100 (tables exist, FKs/indexes missing)
- Entity Models: 60/100 (entities created, but incomplete)
- Repository Layer: 20/100 (200+ queries need migration)
- Service Layer: 30/100 (core logic needs rewrite)
- Controller Layer: 40/100 (APIs need request/response changes)
- DTOs: 30/100 (structure needs major changes)
- Status Logic: 0/100 (not defined)
- Document Storage: 0/100 (file storage not implemented)
- Testing: 0/100 (no tests for new model)

### Critical Blockers

1. **Status Calculation Logic Not Defined** - Cannot proceed without this
2. **File Storage Infrastructure Missing** - Documents cannot be stored
3. **200+ SQL Queries Need Migration** - Massive effort required
4. **Status Aggregation Rules Not Defined** - Day-level status unclear
5. **Master Data Not Populated** - Cannot map strings to IDs

### Safe Integration Order

**Phase 1: Foundation (Weeks 1-2)**
1. Populate master tables (`day_type_master_new`, `status_master_new`, `doc_mime_type_master_new`, `client_status_master_new`)
2. Define status calculation/aggregation rules
3. Implement status mapper service (Integer ↔ String)
4. Create database views for backward compatibility

**Phase 2: Read Operations (Weeks 3-4)**
1. Migrate read-only queries (reports, dashboards)
2. Implement status aggregation in queries
3. Update DTOs for nested structure
4. Test read operations thoroughly

**Phase 3: Document Storage (Weeks 5-6)**
1. Implement file storage service (S3/local)
2. Migrate document upload/download
3. Update document approval flows
4. Test document operations

**Phase 4: Write Operations (Weeks 7-10)**
1. Migrate `addTimesheet()` with new model
2. Migrate `updateTimesheet()` with new model
3. Migrate approval/rejection flows
4. Implement dual-write pattern (old + new tables)

**Phase 5: Full Migration (Weeks 11-12)**
1. Switch all operations to new tables
2. Remove old table dependencies
3. Drop backward compatibility views
4. Performance optimization

---

## RECOMMENDATIONS

1. **Define Status Calculation Rules First** - Critical blocker
2. **Implement File Storage Early** - Required for document operations
3. **Create Database Views** - Enables gradual migration
4. **Use Dual-Write Pattern** - Write to both old and new tables initially
5. **Implement Status Aggregation Service** - Centralize status calculation logic
6. **Create Master Data Mappers** - Map strings to IDs consistently
7. **Migrate Read Operations First** - Lower risk, validates model
8. **Test Aggressively** - Complex data model changes require thorough testing

---

**Document Version:** 1.0  
**Date:** 2025-01-30  
**Author:** Principal Software Architect Analysis

