# API Impact Analysis - New Timesheet Tables Integration

## Summary
Comprehensive analysis of all APIs impacted by the migration to new `_new` timesheet tables.

**Date:** 2025-01-30  
**Status:** 🔄 **ANALYSIS COMPLETE - READY FOR MIGRATION**

---

## Executive Summary

### Current State
- ✅ **Repository Layer:** All queries migrated to `_new` tables
- ✅ **Named Queries:** All migrated to `_new` tables
- ❌ **Service Layer:** Still using OLD repositories (`TimesheetsRepository`, `TimesheetActivityMapRepository`, `TimesheetDocumentDetailsRepository`)
- ❌ **Entity Layer:** Service methods still using OLD entities (`Timesheet`, `TimesheetActivityMap`, `TimesheetDocumentDetails`)

### Impact Scope
- **Total APIs Impacted:** 60+ endpoints
- **Controllers Affected:** 4 controllers
- **Service Methods Affected:** 80+ methods
- **Repositories to Migrate:** 3 repositories

---

## Controllers & APIs Impacted

### 1. TimesheetController (Primary Impact)
**File:** `src/main/java/com/apmosys/employeeportal/controller/TimesheetController.java`  
**Total APIs:** 60 endpoints

#### API Endpoints & Service Methods Mapping

| # | API Endpoint | HTTP Method | Service Method | Repository Used | Impact Level |
|---|--------------|-------------|----------------|-----------------|--------------|
| 1 | `/getAllProjectsByEmpId` | POST | `getAllProjectsByEmpId()` | `EmployeeTeamMapRepository` | Low |
| 2 | `/getAllActivitiesByProjectIdandEmpId` | POST | `getAllActivitiesByProjectIdandEmpId()` | `ProjectRepository` | Low |
| 3 | `/addTimesheetWithClient` | POST | `addTimesheet()` | `TimesheetsRepository` ⚠️ | **CRITICAL** |
| 4 | `/getAllMyTeamTimesheets` | POST | `getAllMyTeamTimesheets()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 5 | `/getAllMyTimesheetsByEmpId` | POST | `getAllMyTimesheetsByEmpId()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 6 | `/getAllMyActivitiesByTimesheetId` | POST | `getAllMyActivitiesByTimesheetId()` | `TimesheetActivityMapRepository` ⚠️ | **HIGH** |
| 7 | `/getMyReporteesTimesheetRequests` | POST | `getMyReporteesTimesheetRequests()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 8 | `/countMyReporteesTimesheetRequests` | POST | `countMyReporteesTimesheetRequests()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 9 | `/updateTimesheetRequestById` | POST | `updateTimesheetRequestById()` | `TimesheetsRepository` ⚠️ | **CRITICAL** |
| 10 | `/updateTimesheet` | POST | `updateTimesheet()` | `TimesheetsRepository` ⚠️ | **CRITICAL** |
| 11 | `/getMyReporteesApprovedTimesheets` | POST | `getMyReporteesApprovedTimesheets()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 12 | `/getMyReporteesApprovedTimesheets2` | POST | `getMyReporteesApprovedTimesheets2()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 13 | `/getMyReportees` | POST | `getMyReportees()` | `EmployeeRepository` | Low |
| 14 | `/getLast7DaysTimesheetsByEmpId` | POST | `getLast7DaysTimesheetsByEmpId()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 15 | `/getAllLeaveTimesheetsWithoutLeaveApplication` | POST | `getAllLeaveTimesheetsWithoutLeaveApplication()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 16 | `/addClientAndProjectByList` | POST | `addClientAndProjectByList()` | `ProjectRepository` | Low |
| 17 | `/getTimesheetsForHomePageByEmpId` | POST | `getTimesheetsForHomePageByEmpId()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 18 | `/revokeApprovedTimesheet` | POST | `revokeApprovedTimesheet()` | `TimesheetsRepository` ⚠️ | **CRITICAL** |
| 19 | `/bulkApproveTimesheetRequest` | POST | `bulkApproveTimesheetRequest()` | `TimesheetsRepository` ⚠️ | **CRITICAL** |
| 20 | `/bulkRejectTimesheetRequest` | POST | `bulkRejectTimesheetRequest()` | `TimesheetsRepository` ⚠️ | **CRITICAL** |
| 21 | `/getAllOrDeptWiseEmployeeTimesheetReport` | POST | `getAllOrDeptWiseEmployeeTimesheetReport()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 22 | `/getLastFilledTimesheetByEmpId` | POST | `getLastFilledTimesheetByEmpId()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 23 | `/getActiveProjectsByEmpId` | POST | `getActiveProjectsByEmpId()` | `ProjectRepository` | Low |
| 24 | `/getClientSideIdByProjectId` | POST | `getClientSideIdByProjectId()` | `ProjectRepository` | Low |
| 25 | `/fetchEmploymentIdByEmpId` | POST | `fetchEmploymentIdByEmpId()` | `EmployeeRepository` | Low |
| 26 | `/updateClientSideIdMapping` | POST | `updateClientSideIdMapping()` | `EmployeeClientSideIdMappingRepository` | Low |
| 27 | `/getActiveProjectsAndClientSideIdByEmpId` | POST | `getActiveProjectsAndClientSideIdByEmpId()` | `ProjectRepository` | Low |
| 28 | `/getEmployeeListByProjectId` | GET | `getEmployeeListByProjectId()` | `ProjectRepository` | Low |
| 29 | `/getClientSideIdByProjectIdAndEmpId` | POST | `getClientSideIdByProjectIdAndEmpId()` | `ProjectRepository` | Low |
| 30 | `/getDocumentDataByDocId` | GET | `getDocumentDataByDocId()` | `TimesheetDocumentDetailsRepository` ⚠️ | **HIGH** |
| 31 | `/getFinalDocumentDataByDocId` | GET | `getFinalDocumentDataByDocId()` | `FinalDocumentRepository` | Low |
| 32 | `/getOneMonthTimesheetReport` | POST | `getTimesheetForEmployee()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 33 | `/checkIfProjectRequiresClientId` | GET | `checkIfProjectRequiresClientId()` | `ProjectRepository` | Low |
| 34 | `/totalVmsFilledCount` | POST | `totalVmsFilledCount()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 35 | `/totalIshineFilledCount` | POST | `totalIshineFilledCount()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 36 | `/totalvmsNotFilled` | POST | `totalvmsNotFilled()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 37 | `/totalIshineNotFilledCount` | POST | `totalIshineNotFilledCount()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 38 | `/getVmsDocumentApprovalStatusWiseCount` | GET | `getVmsDocumentApprovalStatusWiseCount()` | `TimesheetDocumentDetailsRepository` ⚠️ | **HIGH** |
| 39 | `/bulkFinalDocumentUpload` | POST | `replaceAllTemporaryFileWithFinalFile()` | `TimesheetDocumentDetailsRepository` ⚠️ | **CRITICAL** |
| 40 | `/getAllDisabledDateListForBulkDocSubmit` | GET | `getAllDisabledDateListForBulkDocSubmit()` | `ProjectRepository` | Low |
| 41 | `/getEmployeeViewForClientAttendanceStatus` | POST | `getEmployeeViewForClientAttendanceStatus()` | `EmployeeRepository` | Medium |
| 42 | `/getEmployeeTimesheetsByProject` | POST | `getEmployeeTimesheetsByProject()` | `ProjectRepository` | Medium |
| 43 | `/getRejectionReason` | GET | `getRejectionReason()` | `TimesheetRejectionReasonsMasterRepository` | Low |
| 44 | `/approveOrRejectDocument` | POST | `approveOrRejectDocument()` | `TimesheetDocumentDetailsRepository` ⚠️ | **CRITICAL** |
| 45 | `/setTimesheetRejectReason` | POST | `setTimesheetRejectReason()` | `TimesheetRejectionReasonsMasterRepository` | Low |
| 46 | `/getRejectionReasonById` | GET | `getRejectionReasonById()` | `TimesheetRejectionReasonsMasterRepository` | Low |
| 47 | `/getProjectViewForClientAttendanceStatus` | POST | `getProjectViewForClientAttendanceStatus()` | `ProjectRepository` | Medium |
| 48 | `/updateActiveByRejectIdId` | POST | `updateActiveByRejectIdId()` | `TimesheetRejectionReasonsMasterRepository` | Low |
| 49 | `/getAllEmployeeDSROfRM` | POST | `getAllEmployeeDSROfRM()` | `ProjectRepository` | Medium |
| 50 | `/getEmployeeTimesheetAsCalender` | POST | `getEmployeeTimesheetAsCalender()` | `EmployeeRepository` | Medium |
| 51 | `/approveTimesheetRequest` | POST | `approveTimesheetRequest()` | `TimesheetsRepository` ⚠️ | **CRITICAL** |
| 52 | `/getEmployeeTimesheetAsCalenderByProjectId` | POST | `getEmployeeTimesheetAsCalenderByProjectId()` | `ProjectRepository` | Medium |
| 53 | `/getTimesheetDashboardCountForEmployee` | POST | `getTimesheetDashboardCountForEmployee()` | `EmployeeRepository` | Medium |
| 54 | `/getTimesheetDashboardCountForProject` | GET | `getTimesheetDashboardCountForProject()` | `ProjectRepository` | Medium |
| 55 | `/getLastFilledTimesheetByEmp` | POST | `getLastFilledTimesheetByEmp()` | `TimesheetsRepository` ⚠️ | **HIGH** |
| 56 | `/getEmployeeByNameAndEmpidForTimesheet` | POST | `getEmployeeByNameAndEmpidForTimesheet()` | `EmployeeRepository` | Low |
| 57 | `/getDocumentsByEmpAndDate` | POST | `getDocumentsByEmpAndDate()` | `TimesheetDocumentDetailsRepository` ⚠️ | **HIGH** |
| 58 | `/getEmployeeSummaryOnExport` | POST | `getEmployeeSummaryOnExportAccordingToStatus()` | `EmployeeRepository` | Medium |
| 59 | `/isInTNMProject` | POST | `isEmployeeInTNMProject()` | `ProjectRepository` | Low |
| 60 | `/isClientIdMandetory` | POST | `isClientMandetory()` | `ProjectRepository` | Low |
| 61 | `/getProjectByMonthRangeAndEmpId` | POST | `getProjectByMonthRangeAndEmpId()` | `ProjectRepository` | Medium |

**Legend:**
- ⚠️ = Uses OLD repository (needs migration)
- **CRITICAL** = Write operations (CREATE/UPDATE/DELETE)
- **HIGH** = Read operations with high usage
- **MEDIUM** = Read operations with moderate usage
- **LOW** = Read operations with low usage or no timesheet dependency

---

### 2. Employee360Controller
**File:** `src/main/java/com/apmosys/employeeportal/controller/Employee360Controller.java`  
**Total APIs:** 1 endpoint

| # | API Endpoint | HTTP Method | Service Method | Repository Used | Impact Level |
|---|--------------|-------------|----------------|-----------------|--------------|
| 1 | `/get360TimesheetDetails` | GET | `get360TimesheetDetails()` | `EmployeeRepository` | Medium |

---

### 3. ReportController
**File:** `src/main/java/com/apmosys/employeeportal/controller/ReportController.java`  
**Total APIs:** 1 endpoint

| # | API Endpoint | HTTP Method | Service Method | Repository Used | Impact Level |
|---|--------------|-------------|----------------|-----------------|--------------|
| 1 | `/timesheetReport` | GET | `timesheetReport()` | `ReportService` → `TimesheetsRepository` ⚠️ | **HIGH** |

---

### 4. ResourceManagementController
**File:** `src/main/java/com/apmosys/employeeportal/controller/ResourceManagementController.java`  
**Total APIs:** 3 endpoints

| # | API Endpoint | HTTP Method | Service Method | Repository Used | Impact Level |
|---|--------------|-------------|----------------|-----------------|--------------|
| 1 | `/sendTimesheetDetailsToShankh` | POST | `sendTimesheetDetailsToShankh()` | `ResourceManagementService` → `TimesheetsRepository` ⚠️ | **HIGH** |
| 2 | `/getDocumentDataByDocIdForPO` | POST | `getDocumentDataByDocId()` | `ResourceManagementService` → `TimesheetDocumentDetailsRepository` ⚠️ | **HIGH** |
| 3 | `/getAllApprovedPoWithTimesheet` | GET | `getAllApprovedPoWithTimesheet()` | `ProjectRepository` | Medium |

---

## Service Layer Analysis

### TimesheetService - Repository Dependencies

#### OLD Repositories (Need Migration) ⚠️
1. **`TimesheetsRepository`** → Migrate to **`EmployeeTimesheetsNewRepository`**
   - Used in: 40+ service methods
   - Entity: `Timesheet` → `EmployeeTimesheetsNew`
   
2. **`TimesheetActivityMapRepository`** → Migrate to **`TimesheetActivityMapNewRepository`**
   - Used in: 10+ service methods
   - Entity: `TimesheetActivityMap` → `EmployeeTimesheetActivitiesMappingNew`
   
3. **`TimesheetDocumentDetailsRepository`** → Migrate to **`TimesheetDocumentDetailsNewRepository`**
   - Used in: 8+ service methods
   - Entity: `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`

#### Already Using New Repositories ✅
- `EmployeeRepository` (queries already migrated)
- `ProjectRepository` (queries already migrated)
- `FinalDocumentRepository` (new entity, no migration needed)

---

## Critical Service Methods Requiring Migration

### Write Operations (CRITICAL - Must Migrate First)

1. **`addTimesheet()`** - Line ~645
   - **Repository:** `TimesheetsRepository` → `EmployeeTimesheetsNewRepository`
   - **Entity:** `Timesheet` → `EmployeeTimesheetsNew`
   - **Impact:** Creates new timesheet entries
   - **Dependencies:** `TimesheetActivityMapRepository` → `TimesheetActivityMapNewRepository`

2. **`updateTimesheet()`** - Line ~644
   - **Repository:** `TimesheetsRepository` → `EmployeeTimesheetsNewRepository`
   - **Entity:** `Timesheet` → `EmployeeTimesheetsNew`
   - **Impact:** Updates existing timesheet entries

3. **`updateTimesheetRequestById()`**
   - **Repository:** `TimesheetsRepository` → `EmployeeTimesheetsNewRepository`
   - **Impact:** Updates timesheet status/approval

4. **`approveTimesheetRequest()`**
   - **Repository:** `TimesheetsRepository` → `EmployeeTimesheetsNewRepository`
   - **Impact:** Approves timesheet requests

5. **`revokeApprovedTimesheet()`**
   - **Repository:** `TimesheetsRepository` → `EmployeeTimesheetsNewRepository`
   - **Impact:** Revokes approved timesheets

6. **`bulkApproveTimesheetRequest()`**
   - **Repository:** `TimesheetsRepository` → `EmployeeTimesheetsNewRepository`
   - **Impact:** Bulk approval operations

7. **`bulkRejectTimesheetRequest()`**
   - **Repository:** `TimesheetsRepository` → `EmployeeTimesheetsNewRepository`
   - **Impact:** Bulk rejection operations

8. **`approveOrRejectDocument()`**
   - **Repository:** `TimesheetDocumentDetailsRepository` → `TimesheetDocumentDetailsNewRepository`
   - **Impact:** Document approval/rejection

9. **`replaceAllTemporaryFileWithFinalFile()`**
   - **Repository:** `TimesheetDocumentDetailsRepository` → `TimesheetDocumentDetailsNewRepository`
   - **Impact:** Final document upload and mapping

---

## Migration Plan - Next Steps

### Phase 1: Repository Injection Migration (Foundation)
**Priority:** 🔴 **CRITICAL**  
**Estimated Effort:** 2-3 days

#### Tasks:
1. **Update TimesheetService Repository Injections**
   - Replace `TimesheetsRepository` with `EmployeeTimesheetsNewRepository`
   - Replace `TimesheetActivityMapRepository` with `TimesheetActivityMapNewRepository`
   - Replace `TimesheetDocumentDetailsRepository` with `TimesheetDocumentDetailsNewRepository`
   - Update all import statements

2. **Update Entity References**
   - Replace `Timesheet` entity with `EmployeeTimesheetsNew`
   - Replace `TimesheetActivityMap` entity with `EmployeeTimesheetActivitiesMappingNew`
   - Replace `TimesheetDocumentDetails` entity with `TimesheetDocumentDetailsNew`
   - Update all field mappings (see schema differences)

3. **Update Composite Key Handling**
   - `TimesheetActivityMapId` → `TimesheetActivityMapId` (same structure, verify)
   - Update all composite key access patterns

---

### Phase 2: Entity Field Mapping Migration
**Priority:** 🔴 **CRITICAL**  
**Estimated Effort:** 3-4 days

#### Schema Differences to Address:

##### EmployeeTimesheetsNew vs Timesheet
- ✅ `total_time` → `total_activities_minutes` (calculate from activities)
- ✅ `total_working_hours` → `total_working_minutes` (convert format)
- ✅ `day_type` → `day_type_id` (FK to `day_type_master_new`)
- ✅ `status` → `status_id` (FK to `status_master_new`)
- ✅ `description` → Removed (now in activity mapping)
- ✅ `project_id` → Removed (now in activity mapping)
- ✅ `client_approval_status` → Removed (now in `project_timesheet_status_new`)
- ✅ `rm_approval_status` → Removed (now in `project_timesheet_status_new`)
- ✅ `hr_approval_status` → Removed (now in `project_timesheet_status_new`)
- ✅ `is_night_shift` → Removed (now in `project_timesheet_status_new`)

##### EmployeeTimesheetActivitiesMappingNew vs TimesheetActivityMap
- ✅ `completion_time` → `duration_minutes` (convert hours to minutes)
- ✅ `timesheet_activity_map_id` → Composite key (`timesheet_id`, `activity_id`, `project_id`)
- ✅ `client_location_id` → Removed (verify if needed)

##### TimesheetDocumentDetailsNew vs TimesheetDocumentDetails
- ✅ `emp_id` → Removed (get from `EmployeeTimesheetsNew`)
- ✅ `rm_approval_status` → Removed
- ✅ `hr_approval_status` → Removed
- ✅ `client_approval_status` → `client_approval_status_id` (FK to `client_status_master_new`)

---

### Phase 3: Service Method Migration (Write Operations First)
**Priority:** 🔴 **CRITICAL**  
**Estimated Effort:** 5-7 days

#### Migration Order:
1. **`addTimesheet()`** - Most critical, creates new records
2. **`updateTimesheet()`** - Updates existing records
3. **`updateTimesheetRequestById()`** - Status updates
4. **`approveTimesheetRequest()`** - Approval workflow
5. **`revokeApprovedTimesheet()`** - Revocation workflow
6. **`bulkApproveTimesheetRequest()`** - Bulk operations
7. **`bulkRejectTimesheetRequest()`** - Bulk operations
8. **Document-related methods** - Document approval/upload

---

### Phase 4: Service Method Migration (Read Operations)
**Priority:** 🟡 **HIGH**  
**Estimated Effort:** 4-5 days

#### Migration Order:
1. High-usage read methods (dashboard, reports)
2. Medium-usage read methods (employee views)
3. Low-usage read methods (utility queries)

---

### Phase 5: DTO Mapping Updates
**Priority:** 🟡 **HIGH**  
**Estimated Effort:** 2-3 days

#### Tasks:
1. Update `TimesheetDTO` mapping logic
2. Update response DTOs to match new schema
3. Update field access patterns in service methods
4. Update aggregation logic (total_time, total_working_hours)

---

### Phase 6: Testing & Validation
**Priority:** 🔴 **CRITICAL**  
**Estimated Effort:** 5-7 days

#### Test Coverage:
1. **Unit Tests:** All service methods
2. **Integration Tests:** All API endpoints
3. **Data Validation:** Verify data integrity
4. **Performance Tests:** Query performance
5. **Regression Tests:** Existing functionality

---

## Risk Assessment

### High-Risk Areas
1. **Write Operations** - Data corruption risk if migration incomplete
2. **Composite Keys** - Complex JOIN logic may break
3. **Aggregation Logic** - Total time calculations may be incorrect
4. **Document Management** - File upload/download may fail
5. **Approval Workflows** - Status transitions may break

### Mitigation Strategies
1. **Feature Flags** - Toggle between old/new repositories
2. **Dual Write** - Write to both old and new tables during transition
3. **Data Validation** - Compare old vs new table data
4. **Rollback Plan** - Quick revert to old repositories if issues
5. **Staged Rollout** - Migrate one API at a time

---

## Dependencies & Prerequisites

### Completed ✅
- ✅ Repository query migration (all queries updated)
- ✅ Named queries migration (all queries updated)
- ✅ New entity classes created
- ✅ New repository interfaces created

### Pending ❌
- ❌ Service layer repository injection
- ❌ Entity field mapping updates
- ❌ DTO mapping logic updates
- ❌ Composite key handling
- ❌ Master table JOIN logic
- ❌ Aggregation logic updates

---

## Estimated Timeline

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| Phase 1: Repository Injection | 2-3 days | None |
| Phase 2: Entity Field Mapping | 3-4 days | Phase 1 |
| Phase 3: Write Operations | 5-7 days | Phase 1, 2 |
| Phase 4: Read Operations | 4-5 days | Phase 1, 2 |
| Phase 5: DTO Mapping | 2-3 days | Phase 3, 4 |
| Phase 6: Testing | 5-7 days | Phase 5 |
| **Total** | **21-29 days** | |

---

## Success Criteria

1. ✅ All APIs functional with new tables
2. ✅ No data loss during migration
3. ✅ Performance maintained or improved
4. ✅ All tests passing
5. ✅ Zero production incidents
6. ✅ Complete rollback capability

---

**Last Updated:** 2025-01-30  
**Next Action:** Begin Phase 1 - Repository Injection Migration

