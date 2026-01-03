# Timesheet API Analysis & Implementation Plan
## Complete Backend Migration Plan for New Hierarchical Timesheet Structure

**Date:** 2025-01-30  
**Status:** 📋 **PLANNING COMPLETE - READY FOR IMPLEMENTATION**

---

## Executive Summary

### New Architecture Overview
The timesheet system is migrating from a **flat structure** to a **hierarchical structure**:

**OLD Structure:**
```
Timesheet (single record per day)
  ├── Activities (separate table)
  └── Documents (separate table)
```

**NEW Structure:**
```
EmployeeTimesheet (one per day per employee)
  └── ProjectTimesheets (multiple per day - one per project)
      └── Activities (multiple per project)
```

### Key Changes
1. **One EmployeeTimesheet per day** (replaces single Timesheet)
2. **Multiple ProjectTimesheets per day** (employee can work on multiple projects)
3. **Activities nested under projects** (activity-to-project relationship)
4. **Project-level approval status** (separate approval per project)
5. **Aggregated totals** (calculated from activities)

---

## New DTO Structure

### 1. EmployeeTimesheetDTO
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTimesheetDTO {
    private Long timesheetId;
    private Long empId;
    private LocalDate date;
    private Integer dayTypeId;        // FK to day_type_master_new
    private Integer leaveTypeId;      // FK to leave_type_master (nullable)
    private Integer status;           // Calculated from project statuses
    private Integer totalWorkingMinutes;
    private Integer totalActivitiesMinutes;
    private LocalDateTime officeInTime;
    private LocalDateTime officeOutTime;
    private Long createdBy;
    private LocalDateTime createdOn;
    private Long updatedBy;
    private LocalDateTime updatedOn;
}
```

**Maps to:** `employee_timesheets_new` table

---

### 2. ProjectTimesheetDTO
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTimesheetDTO {
    private Long timesheetId;         // FK to employee_timesheets_new
    private Long projectId;
    private String poNo;
    private Long poId;
    private LocalDateTime clientInTime;
    private LocalDateTime clientOutTime;
    private Boolean isNightShift;
    private Integer clientApprovalStatus;  // FK to client_status_master_new
    private Integer status;                // FK to status_master_new
    private Long shadowEmpId;
    private Integer totalClientWorkingMinutes;
    private List<ActivityTimesheetDTO> activities;
}
```

**Maps to:** `project_timesheet_status_new` table (composite key: timesheet_id + project_id)

---

### 3. ActivityTimesheetDTO
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTimesheetDTO {
    private Long timesheetId;         // FK to employee_timesheets_new
    private Long activityId;
    private Long projectId;           // FK to projects
    private String description;
    private Integer durationMinutes;
    private Long clientLocationId;    // FK to client_locations
}
```

**Maps to:** `employee_timesheet_activities_mapping_new` table (composite key: timesheet_id + activity_id + project_id)

---

### 4. TimesheetDTO (Wrapper)
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDTO {
    private EmployeeTimesheetDTO employeeTimesheet;
    private List<ProjectTimesheetDTO> projectTimesheets;
}
```

**Purpose:** Request/Response wrapper for complete timesheet data

---

## API Categorization & Migration Strategy

### Category A: CRUD Operations (New Implementation Required)
These APIs need complete rewrite for new structure.

### Category B: Query Operations (Modify Existing)
These APIs need modification to work with new structure.

### Category C: Utility Operations (Minimal Changes)
These APIs need minor adjustments.

### Category D: Deprecated Operations (Remove/Replace)
These APIs are no longer valid with new structure.

---

## Phase 1: Foundation & Core CRUD Operations
**Priority:** 🔴 **CRITICAL**  
**Duration:** 5-7 days  
**Dependencies:** None

### 1.1 Create New DTOs
**Files to Create:**
- `src/main/java/com/apmosys/employeeportal/dto/EmployeeTimesheetDTO.java`
- `src/main/java/com/apmosys/employeeportal/dto/ProjectTimesheetDTO.java`
- `src/main/java/com/apmosys/employeeportal/dto/ActivityTimesheetDTO.java`
- Update `src/main/java/com/apmosys/employeeportal/dto/TimesheetDTO.java`

**Tasks:**
- [ ] Create EmployeeTimesheetDTO with all fields
- [ ] Create ProjectTimesheetDTO with all fields
- [ ] Create ActivityTimesheetDTO with all fields
- [ ] Update TimesheetDTO to use new structure
- [ ] Add validation annotations
- [ ] Add Jackson annotations for JSON mapping

---

### 1.2 Create New Service Layer
**Files to Create:**
- `src/main/java/com/apmosys/employeeportal/service/EmployeeTimesheetService.java`
- `src/main/java/com/apmosys/employeeportal/service/ProjectTimesheetService.java`
- `src/main/java/com/apmosys/employeeportal/service/ActivityTimesheetService.java`

**Tasks:**
- [ ] Create EmployeeTimesheetService with CRUD operations
- [ ] Create ProjectTimesheetService with CRUD operations
- [ ] Create ActivityTimesheetService with CRUD operations
- [ ] Implement transaction management
- [ ] Implement validation logic
- [ ] Implement aggregation logic (total minutes calculation)

---

### 1.3 Core CRUD APIs - CREATE

#### API 1.1: Create Timesheet (New)
**Endpoint:** `POST /api/createTimesheet`  
**Controller:** `TimesheetController.createTimesheet()`  
**Service:** `TimesheetService.createTimesheet()`  
**Priority:** 🔴 **CRITICAL**

**Request:**
```json
{
  "employeeTimesheet": {
    "empId": 123,
    "date": "2025-01-30",
    "dayTypeId": 1,
    "officeInTime": "2025-01-30T09:00:00",
    "officeOutTime": "2025-01-30T18:00:00"
  },
  "projectTimesheets": [
    {
      "projectId": 456,
      "clientInTime": "2025-01-30T09:30:00",
      "clientOutTime": "2025-01-30T17:30:00",
      "isNightShift": false,
      "activities": [
        {
          "activityId": 789,
          "description": "Development work",
          "durationMinutes": 480,
          "clientLocationId": 10
        }
      ]
    }
  ]
}
```

**Implementation Steps:**
1. Validate employee authorization
2. Validate timesheet date (not future, not locked)
3. Check if timesheet already exists for date
4. Create EmployeeTimesheet record
5. For each ProjectTimesheet:
   - Create ProjectTimesheetStatus record
   - For each Activity:
     - Create ActivityMapping record
6. Calculate and update totals
7. Handle document uploads (if any)
8. Return created timesheet

**Service Method:**
```java
@Transactional(rollbackFor = Exception.class)
public ServiceResponse createTimesheet(TimesheetDTO timesheetDTO, 
                                       MultipartFile doc1, 
                                       MultipartFile doc2)
```

**Dependencies:**
- EmployeeTimesheetService.create()
- ProjectTimesheetService.create()
- ActivityTimesheetService.create()
- TimesheetDocumentService.handleDocumentUpload()

---

#### API 1.2: Create Timesheet (Replace Existing)
**Endpoint:** `POST /api/addTimesheetWithClient` (MODIFY)  
**Controller:** `TimesheetController.addTimesheetWithClient()`  
**Service:** `TimesheetService.addTimesheet()` → **REPLACE**  
**Priority:** 🔴 **CRITICAL**

**Changes Required:**
- Replace old `addTimesheet()` with new `createTimesheet()`
- Update request/response mapping
- Maintain backward compatibility during transition

---

### 1.4 Core CRUD APIs - READ

#### API 1.3: Get Timesheet by ID (New)
**Endpoint:** `GET /api/getTimesheetById`  
**Controller:** `TimesheetController.getTimesheetById()`  
**Service:** `TimesheetService.getTimesheetById()`  
**Priority:** 🟡 **HIGH**

**Request:** `?timesheetId=123`

**Response:**
```json
{
  "employeeTimesheet": { ... },
  "projectTimesheets": [ ... ]
}
```

**Implementation:**
- Fetch EmployeeTimesheet by ID
- Fetch all ProjectTimesheets for timesheet
- Fetch all Activities for each project
- Aggregate totals
- Return complete TimesheetDTO

---

#### API 1.4: Get Timesheet by Date (New)
**Endpoint:** `POST /api/getTimesheetByDate`  
**Controller:** `TimesheetController.getTimesheetByDate()`  
**Service:** `TimesheetService.getTimesheetByDate()`  
**Priority:** 🟡 **HIGH**

**Request:**
```json
{
  "empId": 123,
  "date": "2025-01-30"
}
```

---

#### API 1.5: Get Timesheets by Date Range (New)
**Endpoint:** `POST /api/getTimesheetsByDateRange`  
**Controller:** `TimesheetController.getTimesheetsByDateRange()`  
**Service:** `TimesheetService.getTimesheetsByDateRange()`  
**Priority:** 🟡 **HIGH**

**Request:**
```json
{
  "empId": 123,
  "startDate": "2025-01-01",
  "endDate": "2025-01-31"
}
```

**Response:** `List<TimesheetDTO>`

---

### 1.5 Core CRUD APIs - UPDATE

#### API 1.6: Update Timesheet (New)
**Endpoint:** `POST /api/updateTimesheet` (MODIFY)  
**Controller:** `TimesheetController.updateTimesheet()`  
**Service:** `TimesheetService.updateTimesheet()` → **REPLACE**  
**Priority:** 🔴 **CRITICAL**

**Request:** Same as Create

**Implementation:**
1. Fetch existing EmployeeTimesheet
2. Update EmployeeTimesheet fields
3. Handle ProjectTimesheet changes:
   - Add new projects
   - Update existing projects
   - Remove deleted projects
4. Handle Activity changes:
   - Add new activities
   - Update existing activities
   - Remove deleted activities
5. Recalculate totals
6. Update documents if changed

---

#### API 1.7: Update Timesheet Status (New)
**Endpoint:** `POST /api/updateTimesheetStatus`  
**Controller:** `TimesheetController.updateTimesheetStatus()`  
**Service:** `TimesheetService.updateTimesheetStatus()`  
**Priority:** 🟡 **HIGH**

**Request:**
```json
{
  "timesheetId": 123,
  "projectId": 456,
  "status": 2,
  "updatedBy": 789
}
```

**Purpose:** Update status for specific project within timesheet

---

### 1.6 Core CRUD APIs - DELETE

#### API 1.8: Delete Timesheet (New)
**Endpoint:** `DELETE /api/deleteTimesheet`  
**Controller:** `TimesheetController.deleteTimesheet()`  
**Service:** `TimesheetService.deleteTimesheet()`  
**Priority:** 🟡 **MEDIUM**

**Request:** `?timesheetId=123`

**Implementation:**
- Delete all Activities (cascade)
- Delete all ProjectTimesheets (cascade)
- Delete EmployeeTimesheet
- Delete associated documents

---

#### API 1.9: Delete Project from Timesheet (New)
**Endpoint:** `DELETE /api/deleteProjectFromTimesheet`  
**Controller:** `TimesheetController.deleteProjectFromTimesheet()`  
**Service:** `TimesheetService.deleteProjectFromTimesheet()`  
**Priority:** 🟡 **MEDIUM**

**Request:**
```json
{
  "timesheetId": 123,
  "projectId": 456
}
```

---

#### API 1.10: Delete Activity from Timesheet (New)
**Endpoint:** `DELETE /api/deleteActivityFromTimesheet`  
**Controller:** `TimesheetController.deleteActivityFromTimesheet()`  
**Service:** `TimesheetService.deleteActivityFromTimesheet()`  
**Priority:** 🟡 **MEDIUM**

**Request:**
```json
{
  "timesheetId": 123,
  "activityId": 789,
  "projectId": 456
}
```

---

## Phase 2: Approval & Status Management
**Priority:** 🔴 **CRITICAL**  
**Duration:** 4-5 days  
**Dependencies:** Phase 1

### 2.1 Approval APIs

#### API 2.1: Approve Timesheet (Modify)
**Endpoint:** `POST /api/approveTimesheetRequest` (MODIFY)  
**Controller:** `TimesheetController.approveTimesheetRequest()`  
**Service:** `TimesheetService.approveTimesheetRequest()` → **MODIFY**  
**Priority:** 🔴 **CRITICAL**

**Changes Required:**
- Old: Approve entire timesheet
- New: Approve specific project(s) within timesheet

**Request:**
```json
{
  "timesheetId": 123,
  "projectIds": [456, 789],  // Optional: if empty, approve all
  "approvedBy": 999,
  "approvalType": "MANAGER" // MANAGER, HR, CLIENT
}
```

**Implementation:**
- Update status for each ProjectTimesheet
- Calculate overall EmployeeTimesheet status
- Send notifications
- Log approval

---

#### API 2.2: Reject Timesheet (Modify)
**Endpoint:** `POST /api/rejectTimesheetRequest` (NEW - was bulkReject)  
**Controller:** `TimesheetController.rejectTimesheetRequest()`  
**Service:** `TimesheetService.rejectTimesheetRequest()`  
**Priority:** 🔴 **CRITICAL**

**Request:**
```json
{
  "timesheetId": 123,
  "projectId": 456,
  "rejectionReasonId": 10,
  "rejectedBy": 999,
  "comments": "Incorrect hours"
}
```

---

#### API 2.3: Bulk Approve Timesheets (Modify)
**Endpoint:** `POST /api/bulkApproveTimesheetRequest` (MODIFY)  
**Controller:** `TimesheetController.bulkApproveTimesheetRequest()`  
**Service:** `TimesheetService.bulkApproveTimesheetRequest()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Request:**
```json
{
  "timesheetIds": [123, 124, 125],
  "projectIds": [456],  // Optional: approve specific projects
  "approvedBy": 999
}
```

---

#### API 2.4: Bulk Reject Timesheets (Modify)
**Endpoint:** `POST /api/bulkRejectTimesheetRequest` (MODIFY)  
**Controller:** `TimesheetController.bulkRejectTimesheetRequest()`  
**Service:** `TimesheetService.bulkRejectTimesheetRequest()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

#### API 2.5: Revoke Approved Timesheet (Modify)
**Endpoint:** `POST /api/revokeApprovedTimesheet` (MODIFY)  
**Controller:** `TimesheetController.revokeApprovedTimesheet()`  
**Service:** `TimesheetService.revokeApprovedTimesheet()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Changes:**
- Revoke specific project approval
- Recalculate overall status

---

### 2.2 Status Query APIs

#### API 2.6: Get Pending Timesheets (Modify)
**Endpoint:** `POST /api/getMyReporteesTimesheetRequests` (MODIFY)  
**Controller:** `TimesheetController.getMyReporteesTimesheetRequests()`  
**Service:** `TimesheetService.getMyReporteesTimesheetRequests()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Changes:**
- Filter by project status
- Group by project
- Show project-wise pending count

---

#### API 2.7: Get Approved Timesheets (Modify)
**Endpoint:** `POST /api/getMyReporteesApprovedTimesheets` (MODIFY)  
**Controller:** `TimesheetController.getMyReporteesApprovedTimesheets()`  
**Service:** `TimesheetService.getMyReporteesApprovedTimesheets()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

## Phase 3: Query & Reporting APIs
**Priority:** 🟡 **HIGH**  
**Duration:** 5-6 days  
**Dependencies:** Phase 1, 2

### 3.1 Employee Timesheet Queries

#### API 3.1: Get Employee Timesheets (Modify)
**Endpoint:** `POST /api/getAllMyTimesheetsByEmpId` (MODIFY)  
**Controller:** `TimesheetController.getAllMyTimesheetsByEmpId()`  
**Service:** `TimesheetService.getAllMyTimesheetsByEmpId()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Changes:**
- Return hierarchical structure
- Include all projects per day
- Aggregate totals

---

#### API 3.2: Get Team Timesheets (Modify)
**Endpoint:** `POST /api/getAllMyTeamTimesheets` (MODIFY)  
**Controller:** `TimesheetController.getAllMyTeamTimesheets()`  
**Service:** `TimesheetService.getAllMyTeamTimesheets()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

#### API 3.3: Get Timesheets for Home Page (Modify)
**Endpoint:** `POST /api/getTimesheetsForHomePageByEmpId` (MODIFY)  
**Controller:** `TimesheetController.getTimesheetsForHomePageByEmpId()`  
**Service:** `TimesheetService.getTimesheetsForHomePageByEmpId()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Changes:**
- Aggregate activities per project
- Show project-wise totals
- Calculate daily totals

---

#### API 3.4: Get Last 7 Days Timesheets (Modify)
**Endpoint:** `POST /api/getLast7DaysTimesheetsByEmpId` (MODIFY)  
**Controller:** `TimesheetController.getLast7DaysTimesheetsByEmpId()`  
**Service:** `TimesheetService.getLast7DaysTimesheetsByEmpId()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

#### API 3.5: Get Last Filled Timesheet (Modify)
**Endpoint:** `POST /api/getLastFilledTimesheetByEmp` (MODIFY)  
**Controller:** `TimesheetController.getLastFilledTimesheetByEmp()`  
**Service:** `TimesheetService.getLastFilledTimesheetByEmp()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

### 3.2 Calendar & View APIs

#### API 3.6: Get Employee Timesheet Calendar (Modify)
**Endpoint:** `POST /api/getEmployeeTimesheetAsCalender` (MODIFY)  
**Controller:** `TimesheetController.getEmployeeTimesheetAsCalender()`  
**Service:** `TimesheetService.getEmployeeTimesheetAsCalender()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Changes:**
- Group by date
- Show multiple projects per day
- Aggregate totals per day

---

#### API 3.7: Get Project Timesheet Calendar (Modify)
**Endpoint:** `POST /api/getEmployeeTimesheetAsCalenderByProjectId` (MODIFY)  
**Controller:** `TimesheetController.getEmployeeTimesheetAsCalenderByProjectId()`  
**Service:** `TimesheetService.getEmployeeTimesheetAsCalenderByProjectId()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

### 3.3 Reporting APIs

#### API 3.8: Get Employee Summary Report (Modify)
**Endpoint:** `POST /api/getEmployeeSummaryOnExport` (MODIFY)  
**Controller:** `TimesheetController.getEmployeeSummaryOnExport()`  
**Service:** `TimesheetService.getEmployeeSummaryOnExportAccordingToStatus()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Changes:**
- Project-wise breakdown
- Activity-wise details
- Aggregated totals

---

#### API 3.9: Get Employee View for Client Attendance (Modify)
**Endpoint:** `POST /api/getEmployeeViewForClientAttendanceStatus` (MODIFY)  
**Controller:** `TimesheetController.getEmployeeViewForClientAttendanceStatus()`  
**Service:** `TimesheetService.getEmployeeViewForClientAttendanceStatus()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

#### API 3.10: Get Project View for Client Attendance (Modify)
**Endpoint:** `POST /api/getProjectViewForClientAttendanceStatus` (MODIFY)  
**Controller:** `TimesheetController.getProjectViewForClientAttendanceStatus()`  
**Service:** `TimesheetService.getProjectViewForClientAttendanceStatus()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

#### API 3.11: Get All Employee DSR of RM (Modify)
**Endpoint:** `POST /api/getAllEmployeeDSROfRM` (MODIFY)  
**Controller:** `TimesheetController.getAllEmployeeDSROfRM()`  
**Service:** `TimesheetService.getAllEmployeeDSROfRM()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

#### API 3.12: Get Employee Timesheets by Project (Modify)
**Endpoint:** `POST /api/getEmployeeTimesheetsByProject` (MODIFY)  
**Controller:** `TimesheetController.getEmployeeTimesheetsByProject()`  
**Service:** `TimesheetService.getEmployeeTimesheetsByProject()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

#### API 3.13: Get One Month Timesheet Report (Modify)
**Endpoint:** `POST /api/getOneMonthTimesheetReport` (MODIFY)  
**Controller:** `TimesheetController.getOneMonthTimesheetReport()`  
**Service:** `TimesheetService.getTimesheetForEmployee()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

#### API 3.14: Get All or Dept Wise Employee Timesheet Report (Modify)
**Endpoint:** `POST /api/getAllOrDeptWiseEmployeeTimesheetReport` (MODIFY)  
**Controller:** `TimesheetController.getAllOrDeptWiseEmployeeTimesheetReport()`  
**Service:** `TimesheetService.getAllOrDeptWiseEmployeeTimesheetReport()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

## Phase 4: Dashboard & Analytics APIs
**Priority:** 🟡 **HIGH**  
**Duration:** 3-4 days  
**Dependencies:** Phase 1, 2, 3

### 4.1 Dashboard APIs

#### API 4.1: Get Timesheet Dashboard Count for Employee (Modify)
**Endpoint:** `POST /api/getTimesheetDashboardCountForEmployee` (MODIFY)  
**Controller:** `TimesheetController.getTimesheetDashboardCountForEmployee()`  
**Service:** `TimesheetService.getTimesheetDashboardCountForEmployee()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Changes:**
- Project-wise counts
- Status breakdown per project
- Aggregated totals

---

#### API 4.2: Get Timesheet Dashboard Count for Project (Modify)
**Endpoint:** `GET /api/getTimesheetDashboardCountForProject` (MODIFY)  
**Controller:** `TimesheetController.getTimesheetDashboardCountForProject()`  
**Service:** `TimesheetService.getTimesheetDashboardCountForProject()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

### 4.2 Count APIs

#### API 4.3: Count Pending Timesheet Requests (Modify)
**Endpoint:** `POST /api/countMyReporteesTimesheetRequests` (MODIFY)  
**Controller:** `TimesheetController.countMyReporteesTimesheetRequests()`  
**Service:** `TimesheetService.countMyReporteesTimesheetRequests()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

**Changes:**
- Count by project
- Total pending count

---

#### API 4.4: Total VMS Filled Count (Modify)
**Endpoint:** `POST /api/totalVmsFilledCount` (MODIFY)  
**Controller:** `TimesheetController.totalVmsFilledCount()`  
**Service:** `TimesheetService.totalVmsFilledCount()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

#### API 4.5: Total Ishine Filled Count (Modify)
**Endpoint:** `POST /api/totalIshineFilledCount` (MODIFY)  
**Controller:** `TimesheetController.totalIshineFilledCount()`  
**Service:** `TimesheetService.totalIshineFilledCount()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

#### API 4.6: Total VMS Not Filled (Modify)
**Endpoint:** `POST /api/totalvmsNotFilled` (MODIFY)  
**Controller:** `TimesheetController.totalvmsNotFilled()`  
**Service:** `TimesheetService.totalvmsNotFilled()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

#### API 4.7: Total Ishine Not Filled Count (Modify)
**Endpoint:** `POST /api/totalIshineNotFilledCount` (MODIFY)  
**Controller:** `TimesheetController.totalIshineNotFilledCount()`  
**Service:** `TimesheetService.totalIshineNotFilledCount()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

## Phase 5: Document Management APIs
**Priority:** 🟡 **HIGH**  
**Duration:** 3-4 days  
**Dependencies:** Phase 1

### 5.1 Document CRUD

#### API 5.1: Get Document by ID (Modify)
**Endpoint:** `GET /api/getDocumentDataByDocId` (MODIFY)  
**Controller:** `TimesheetController.getDocumentDataByDocId()`  
**Service:** `TimesheetService.getDocumentDataByDocId()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Changes:**
- Link to project instead of timesheet only
- Support project-specific documents

---

#### API 5.2: Get Final Document by ID (Modify)
**Endpoint:** `GET /api/getFinalDocumentDataByDocId` (MODIFY)  
**Controller:** `TimesheetController.getFinalDocumentDataByDocId()`  
**Service:** `TimesheetService.getFinalDocumentDataByDocId()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

---

#### API 5.3: Get Documents by Employee and Date (Modify)
**Endpoint:** `POST /api/getDocumentsByEmpAndDate` (MODIFY)  
**Controller:** `TimesheetController.getDocumentsByEmpAndDate()`  
**Service:** `TimesheetService.getDocumentsByEmpAndDate()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

**Changes:**
- Return project-wise documents
- Group by project

---

#### API 5.4: Approve or Reject Document (Modify)
**Endpoint:** `POST /api/approveOrRejectDocument` (MODIFY)  
**Controller:** `TimesheetController.approveOrRejectDocument()`  
**Service:** `TimesheetService.approveOrRejectDocument()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Changes:**
- Link approval to project
- Update project status on approval

---

#### API 5.5: Bulk Final Document Upload (Modify)
**Endpoint:** `POST /api/bulkFinalDocumentUpload` (MODIFY)  
**Controller:** `TimesheetController.bulkFinalDocumentUpload()`  
**Service:** `TimesheetService.replaceAllTemporaryFileWithFinalFile()` → **MODIFY**  
**Priority:** 🟡 **HIGH**

**Changes:**
- Handle multiple projects
- Update project-wise documents

---

#### API 5.6: Get VMS Document Approval Status Wise Count (Modify)
**Endpoint:** `GET /api/getVmsDocumentApprovalStatusWiseCount` (MODIFY)  
**Controller:** `TimesheetController.getVmsDocumentApprovalStatusWiseCount()`  
**Service:** `TimesheetService.getVmsDocumentApprovalStatusWiseCount()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

## Phase 6: Utility & Configuration APIs
**Priority:** 🟢 **LOW**  
**Duration:** 2-3 days  
**Dependencies:** Phase 1

### 6.1 Project & Activity Utilities

#### API 6.1: Get All Projects by Employee ID (Keep)
**Endpoint:** `POST /api/getAllProjectsByEmpId` (KEEP)  
**Controller:** `TimesheetController.getAllProjectsByEmpId()`  
**Service:** `TimesheetService.getAllProjectsByEmpId()`  
**Priority:** 🟢 **LOW**

**Status:** No changes needed

---

#### API 6.2: Get All Activities by Project and Employee (Keep)
**Endpoint:** `POST /api/getAllActivitiesByProjectIdandEmpId` (KEEP)  
**Controller:** `TimesheetController.getAllActivitiesByProjectIdandEmpId()`  
**Service:** `TimesheetService.getAllActivitiesByProjectIdandEmpId()`  
**Priority:** 🟢 **LOW**

**Status:** No changes needed

---

#### API 6.3: Get All My Activities by Timesheet ID (Modify)
**Endpoint:** `POST /api/getAllMyActivitiesByTimesheetId` (MODIFY)  
**Controller:** `TimesheetController.getAllMyActivitiesByTimesheetId()`  
**Service:** `TimesheetService.getAllMyActivitiesByTimesheetId()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

**Changes:**
- Return activities grouped by project
- Include project details

---

#### API 6.4: Get Active Projects by Employee ID (Keep)
**Endpoint:** `POST /api/getActiveProjectsByEmpId` (KEEP)  
**Controller:** `TimesheetController.getActiveProjectsByEmpId()`  
**Service:** `TimesheetService.getActiveProjectsByEmpId()`  
**Priority:** 🟢 **LOW**

---

#### API 6.5: Get Project by Month Range and Employee ID (Modify)
**Endpoint:** `POST /api/getProjectByMonthRangeAndEmpId` (MODIFY)  
**Controller:** `TimesheetController.getProjectByMonthRangeAndEmpId()`  
**Service:** `TimesheetService.getProjectByMonthRangeAndEmpId()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

### 6.2 Client Side ID Management

#### API 6.6: Get Client Side ID by Project ID (Keep)
**Endpoint:** `POST /api/getClientSideIdByProjectId` (KEEP)  
**Controller:** `TimesheetController.getClientSideIdByProjectId()`  
**Service:** `TimesheetService.getClientSideIdByProjectId()`  
**Priority:** 🟢 **LOW**

---

#### API 6.7: Get Client Side ID by Project and Employee (Keep)
**Endpoint:** `POST /api/getClientSideIdByProjectIdAndEmpId` (KEEP)  
**Controller:** `TimesheetController.getClientSideIdByProjectIdAndEmpId()`  
**Service:** `TimesheetService.getClientSideIdByProjectIdAndEmpId()`  
**Priority:** 🟢 **LOW**

---

#### API 6.8: Get Active Projects and Client Side ID (Keep)
**Endpoint:** `POST /api/getActiveProjectsAndClientSideIdByEmpId` (KEEP)  
**Controller:** `TimesheetController.getActiveProjectsAndClientSideIdByEmpId()`  
**Service:** `TimesheetService.getActiveProjectsAndClientSideIdByEmpId()`  
**Priority:** 🟢 **LOW**

---

#### API 6.9: Update Client Side ID Mapping (Keep)
**Endpoint:** `POST /api/updateClientSideIdMapping` (KEEP)  
**Controller:** `TimesheetController.updateClientSideIdMapping()`  
**Service:** `TimesheetService.updateClientSideIdMapping()`  
**Priority:** 🟢 **LOW**

---

#### API 6.10: Check if Project Requires Client ID (Keep)
**Endpoint:** `GET /api/checkIfProjectRequiresClientId` (KEEP)  
**Controller:** `TimesheetController.checkIfProjectRequiresClientId()`  
**Service:** `TimesheetService.checkIfProjectRequiresClientId()`  
**Priority:** 🟢 **LOW**

---

#### API 6.11: Is Client ID Mandatory (Keep)
**Endpoint:** `POST /api/isClientIdMandetory` (KEEP)  
**Controller:** `TimesheetController.isClientMandetory()`  
**Service:** `TimesheetService.isClientMandetory()`  
**Priority:** 🟢 **LOW**

---

### 6.3 Employee Utilities

#### API 6.12: Get Employee by Name and Emp ID for Timesheet (Keep)
**Endpoint:** `POST /api/getEmployeeByNameAndEmpidForTimesheet` (KEEP)  
**Controller:** `TimesheetController.getEmployeeByNameAndEmpidForTimesheet()`  
**Service:** `TimesheetService.getEmployeeByNameAndEmpidForTimesheet()`  
**Priority:** 🟢 **LOW**

---

#### API 6.13: Fetch Employment ID by Employee ID (Keep)
**Endpoint:** `POST /api/fetchEmploymentIdByEmpId` (KEEP)  
**Controller:** `TimesheetController.fetchEmploymentIdByEmpId()`  
**Service:** `TimesheetService.fetchEmploymentIdByEmpId()`  
**Priority:** 🟢 **LOW**

---

#### API 6.14: Get My Reportees (Keep)
**Endpoint:** `POST /api/getMyReportees` (KEEP)  
**Controller:** `TimesheetController.getMyReportees()`  
**Service:** `TimesheetService.getMyReportees()`  
**Priority:** 🟢 **LOW**

---

### 6.4 Leave & Holiday Integration

#### API 6.15: Get All Leave Timesheets Without Leave Application (Modify)
**Endpoint:** `POST /api/getAllLeaveTimesheetsWithoutLeaveApplication` (MODIFY)  
**Controller:** `TimesheetController.getAllLeaveTimesheetsWithoutLeaveApplication()`  
**Service:** `TimesheetService.getAllLeaveTimesheetsWithoutLeaveApplication()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

**Changes:**
- Check project-wise leave entries
- Aggregate leave days per project

---

### 6.5 Rejection Reason Management

#### API 6.16: Get Rejection Reason (Keep)
**Endpoint:** `GET /api/getRejectionReason` (KEEP)  
**Controller:** `TimesheetController.getRejectionReason()`  
**Service:** `TimesheetService.getRejectionReason()`  
**Priority:** 🟢 **LOW**

---

#### API 6.17: Set Timesheet Reject Reason (Keep)
**Endpoint:** `POST /api/setTimesheetRejectReason` (KEEP)  
**Controller:** `TimesheetController.setTimesheetRejectReason()`  
**Service:** `TimesheetService.setTimesheetRejectReason()`  
**Priority:** 🟢 **LOW**

---

#### API 6.18: Get Rejection Reason by ID (Keep)
**Endpoint:** `GET /api/getRejectionReasonById` (KEEP)  
**Controller:** `TimesheetController.getRejectionReasonById()`  
**Service:** `TimesheetService.getRejectionReasonById()`  
**Priority:** 🟢 **LOW**

---

#### API 6.19: Update Active by Reject ID (Keep)
**Endpoint:** `POST /api/updateActiveByRejectIdId` (KEEP)  
**Controller:** `TimesheetController.updateActiveByRejectIdId()`  
**Service:** `TimesheetService.updateActiveByRejectIdId()`  
**Priority:** 🟢 **LOW**

---

### 6.6 Other Utilities

#### API 6.20: Get All Disabled Date List for Bulk Doc Submit (Keep)
**Endpoint:** `GET /api/getAllDisabledDateListForBulkDocSubmit` (KEEP)  
**Controller:** `TimesheetController.getAllDisabledDateListForBulkDocSubmit()`  
**Service:** `TimesheetService.getAllDisabledDateListForBulkDocSubmit()`  
**Priority:** 🟢 **LOW**

---

#### API 6.21: Is Employee in TNM Project (Keep)
**Endpoint:** `POST /api/isInTNMProject` (KEEP)  
**Controller:** `TimesheetController.employeeInTNMProject()`  
**Service:** `TimesheetService.isEmployeeInTNMProject()`  
**Priority:** 🟢 **LOW**

---

#### API 6.22: Get Employee List by Project ID (Keep)
**Endpoint:** `GET /api/getEmployeeListByProjectId` (KEEP)  
**Controller:** `TimesheetController.getEmployeeListByProjectId()`  
**Service:** `TimesheetService.getEmployeeListByProjectId()`  
**Priority:** 🟢 **LOW**

---

#### API 6.23: Add Client and Project by List (Keep)
**Endpoint:** `POST /api/addClientAndProjectByList` (KEEP)  
**Controller:** `TimesheetController.addClientAndProjectByList()`  
**Service:** `TimesheetService.addClientAndProjectByList()`  
**Priority:** 🟢 **LOW**

---

## Phase 7: Integration & External APIs
**Priority:** 🟡 **MEDIUM**  
**Duration:** 2-3 days  
**Dependencies:** Phase 1, 2, 3

### 7.1 Employee360 Integration

#### API 7.1: Get 360 Timesheet Details (Modify)
**Endpoint:** `GET /api/get360TimesheetDetails` (MODIFY)  
**Controller:** `Employee360Controller.get360TimesheetDetails()`  
**Service:** `Employee360Service.get360TimesheetDetails()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

**Changes:**
- Return hierarchical structure
- Project-wise breakdown

---

### 7.2 Report Service Integration

#### API 7.2: Timesheet Report (Modify)
**Endpoint:** `GET /api/timesheetReport` (MODIFY)  
**Controller:** `ReportController.timesheetReport()`  
**Service:** `ReportService.timesheetReport()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

### 7.3 Resource Management Integration

#### API 7.3: Send Timesheet Details to Shankh (Modify)
**Endpoint:** `POST /api/sendTimesheetDetailsToShankh` (MODIFY)  
**Controller:** `ResourceManagementController.sendTimesheetDetailsToShankh()`  
**Service:** `ResourceManagementService.sendTimesheetDetailsToShankh()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

**Changes:**
- Include project-wise data
- Aggregate totals

---

#### API 7.4: Get Document Data by Doc ID for PO (Modify)
**Endpoint:** `POST /api/getDocumentDataByDocIdForPO` (MODIFY)  
**Controller:** `ResourceManagementController.getDocumentDataByDocId()`  
**Service:** `ResourceManagementService.getDocumentDataByDocId()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

#### API 7.5: Get All Approved PO with Timesheet (Modify)
**Endpoint:** `GET /api/getAllApprovedPoWithTimesheet` (MODIFY)  
**Controller:** `ResourceManagementController.getAllApprovedPoWithTimesheet()`  
**Service:** `ResourceManagementService.getAllApprovedPoWithTimesheet()` → **MODIFY**  
**Priority:** 🟡 **MEDIUM**

---

## Implementation Summary

### API Count by Category

| Category | Count | Status |
|----------|-------|--------|
| **New APIs** | 10 | To be created |
| **Modify APIs** | 45 | To be updated |
| **Keep APIs** | 23 | No changes |
| **Total** | **78** | |

### API Count by Phase

| Phase | APIs | Priority | Duration |
|-------|------|----------|----------|
| Phase 1: Foundation & CRUD | 10 | 🔴 CRITICAL | 5-7 days |
| Phase 2: Approval & Status | 7 | 🔴 CRITICAL | 4-5 days |
| Phase 3: Query & Reporting | 14 | 🟡 HIGH | 5-6 days |
| Phase 4: Dashboard & Analytics | 7 | 🟡 HIGH | 3-4 days |
| Phase 5: Document Management | 6 | 🟡 HIGH | 3-4 days |
| Phase 6: Utility & Configuration | 23 | 🟢 LOW | 2-3 days |
| Phase 7: Integration & External | 5 | 🟡 MEDIUM | 2-3 days |
| **Total** | **76** | | **24-32 days** |

---

## Critical Implementation Details

### 1. Status Calculation Logic

**EmployeeTimesheet Status:**
- If ALL projects approved → Status = APPROVED
- If ANY project pending → Status = PENDING
- If ANY project rejected → Status = REJECTED
- If MIXED (some approved, some pending) → Status = PARTIAL

**Implementation:**
```java
private Integer calculateEmployeeTimesheetStatus(List<ProjectTimesheetDTO> projects) {
    boolean allApproved = projects.stream().allMatch(p -> p.getStatus() == APPROVED);
    boolean anyRejected = projects.stream().anyMatch(p -> p.getStatus() == REJECTED);
    boolean anyPending = projects.stream().anyMatch(p -> p.getStatus() == PENDING);
    
    if (allApproved) return APPROVED;
    if (anyRejected) return REJECTED;
    if (anyPending) return PENDING;
    return PARTIAL;
}
```

---

### 2. Total Minutes Calculation

**Total Activities Minutes:**
- Sum of all `durationMinutes` from all activities across all projects

**Total Working Minutes:**
- Office hours: `officeOutTime - officeInTime` (in minutes)
- Or sum of activity durations if office times not provided

**Total Client Working Minutes (per project):**
- Sum of activity durations for that project
- Or `clientOutTime - clientInTime` if provided

**Implementation:**
```java
private void calculateTotals(TimesheetDTO timesheetDTO) {
    EmployeeTimesheetDTO empTS = timesheetDTO.getEmployeeTimesheet();
    List<ProjectTimesheetDTO> projects = timesheetDTO.getProjectTimesheets();
    
    // Calculate total activities minutes
    int totalActivitiesMinutes = projects.stream()
        .flatMap(p -> p.getActivities().stream())
        .mapToInt(ActivityTimesheetDTO::getDurationMinutes)
        .sum();
    empTS.setTotalActivitiesMinutes(totalActivitiesMinutes);
    
    // Calculate total working minutes
    if (empTS.getOfficeInTime() != null && empTS.getOfficeOutTime() != null) {
        long minutes = ChronoUnit.MINUTES.between(
            empTS.getOfficeInTime(), 
            empTS.getOfficeOutTime()
        );
        empTS.setTotalWorkingMinutes((int) minutes);
    } else {
        empTS.setTotalWorkingMinutes(totalActivitiesMinutes);
    }
    
    // Calculate per-project client working minutes
    projects.forEach(project -> {
        int projectMinutes = project.getActivities().stream()
            .mapToInt(ActivityTimesheetDTO::getDurationMinutes)
            .sum();
        project.setTotalClientWorkingMinutes(projectMinutes);
    });
}
```

---

### 3. Transaction Management

**Create/Update Operations:**
- Use `@Transactional` at service method level
- Rollback on any exception
- Ensure atomicity across EmployeeTimesheet, ProjectTimesheets, and Activities

**Implementation:**
```java
@Transactional(rollbackFor = Exception.class)
public ServiceResponse createTimesheet(TimesheetDTO timesheetDTO, 
                                       MultipartFile doc1, 
                                       MultipartFile doc2) {
    try {
        // 1. Create EmployeeTimesheet
        EmployeeTimesheetsNew empTS = createEmployeeTimesheet(timesheetDTO.getEmployeeTimesheet());
        
        // 2. Create ProjectTimesheets
        for (ProjectTimesheetDTO projectDTO : timesheetDTO.getProjectTimesheets()) {
            ProjectTimesheetStatusNew projectTS = createProjectTimesheet(empTS, projectDTO);
            
            // 3. Create Activities
            for (ActivityTimesheetDTO activityDTO : projectDTO.getActivities()) {
                createActivity(empTS, projectTS, activityDTO);
            }
        }
        
        // 4. Calculate and update totals
        calculateAndUpdateTotals(empTS);
        
        // 5. Handle documents
        if (doc1 != null || doc2 != null) {
            handleDocumentUpload(timesheetDTO, empTS, doc1, doc2);
        }
        
        return successResponse(empTS);
    } catch (Exception e) {
        // Transaction will rollback automatically
        return errorResponse(e);
    }
}
```

---

### 4. Validation Rules

**EmployeeTimesheet Validation:**
- Employee must exist and be active
- Date cannot be in future
- Date cannot be within lock period
- Day type must be valid
- Office times must be valid (if provided)

**ProjectTimesheet Validation:**
- Project must exist and be active
- Employee must be assigned to project
- Client times must be valid (if provided)
- At least one activity required for working days

**Activity Validation:**
- Activity must exist and be active
- Activity must belong to project's team
- Duration must be > 0
- Total duration per project must not exceed working hours

---

### 5. Migration Strategy

**Dual Write Approach (Recommended):**
1. Write to both old and new tables during transition
2. Read from new tables
3. Validate data consistency
4. Once stable, stop writing to old tables
5. Eventually deprecate old tables

**Feature Flag Approach:**
1. Use feature flag to toggle between old/new implementation
2. Test new implementation with subset of users
3. Gradually roll out to all users
4. Monitor for issues
5. Remove old implementation once stable

---

## Testing Strategy

### Unit Tests
- [ ] DTO mapping tests
- [ ] Service method tests
- [ ] Validation logic tests
- [ ] Calculation logic tests
- [ ] Transaction rollback tests

### Integration Tests
- [ ] API endpoint tests
- [ ] Database integration tests
- [ ] Document upload tests
- [ ] Approval workflow tests

### Performance Tests
- [ ] Bulk create performance
- [ ] Query performance
- [ ] Aggregation performance
- [ ] Concurrent access tests

### Regression Tests
- [ ] All existing functionality
- [ ] Data integrity checks
- [ ] Backward compatibility (if maintained)

---

## Risk Mitigation

### High-Risk Areas
1. **Data Migration** - Risk of data loss
   - **Mitigation:** Dual write, data validation, rollback plan

2. **Status Calculation** - Complex logic may have bugs
   - **Mitigation:** Comprehensive unit tests, code review

3. **Transaction Management** - Partial updates may occur
   - **Mitigation:** Proper transaction boundaries, rollback testing

4. **Performance** - Aggregation may be slow
   - **Mitigation:** Database indexing, query optimization, caching

5. **Backward Compatibility** - Breaking changes
   - **Mitigation:** Feature flags, gradual rollout, API versioning

---

## Success Criteria

1. ✅ All 76 APIs functional with new structure
2. ✅ No data loss during migration
3. ✅ Performance maintained or improved
4. ✅ All tests passing
5. ✅ Zero production incidents
6. ✅ Complete rollback capability
7. ✅ Documentation updated

---

## Timeline Summary

| Phase | Duration | Cumulative |
|-------|----------|------------|
| Phase 1: Foundation & CRUD | 5-7 days | 5-7 days |
| Phase 2: Approval & Status | 4-5 days | 9-12 days |
| Phase 3: Query & Reporting | 5-6 days | 14-18 days |
| Phase 4: Dashboard & Analytics | 3-4 days | 17-22 days |
| Phase 5: Document Management | 3-4 days | 20-26 days |
| Phase 6: Utility & Configuration | 2-3 days | 22-29 days |
| Phase 7: Integration & External | 2-3 days | 24-32 days |
| **Testing & Bug Fixes** | **5-7 days** | **29-39 days** |
| **Total** | | **29-39 days** |

---

**Last Updated:** 2025-01-30  
**Next Action:** Begin Phase 1 - Foundation & Core CRUD Operations
