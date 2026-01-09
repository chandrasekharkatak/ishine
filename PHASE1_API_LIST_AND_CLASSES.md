# Phase 1: Foundation & Core CRUD Operations - API List & Classes

## Phase 1 Overview
**Priority:** 🔴 **CRITICAL**  
**Duration:** 5-7 days  
**Status:** 📋 **PLANNING COMPLETE - READY FOR IMPLEMENTATION**

---

## Phase 1 APIs Summary

### CREATE Operations (2 APIs)

| # | API Endpoint | HTTP Method | Controller Method | Service Method | Priority |
|---|--------------|-------------|-------------------|----------------|----------|
| 1.1 | `/api/createTimesheet` | POST | `createTimesheet()` | `createTimesheet()` | 🔴 CRITICAL |
| 1.2 | `/api/addTimesheetWithClient` | POST | `addTimesheetWithClient()` | `addTimesheet()` → **REPLACE** | 🔴 CRITICAL |

---

### READ Operations (3 APIs)

| # | API Endpoint | HTTP Method | Controller Method | Service Method | Priority |
|---|--------------|-------------|-------------------|----------------|----------|
| 1.3 | `/api/getTimesheetById` | GET | `getTimesheetById()` | `getTimesheetById()` | 🟡 HIGH |
| 1.4 | `/api/getTimesheetByDate` | POST | `getTimesheetByDate()` | `getTimesheetByDate()` | 🟡 HIGH |
| 1.5 | `/api/getTimesheetsByDateRange` | POST | `getTimesheetsByDateRange()` | `getTimesheetsByDateRange()` | 🟡 HIGH |

---

### UPDATE Operations (2 APIs)

| # | API Endpoint | HTTP Method | Controller Method | Service Method | Priority |
|---|--------------|-------------|-------------------|----------------|----------|
| 1.6 | `/api/updateTimesheet` | POST | `updateTimesheet()` | `updateTimesheet()` → **REPLACE** | 🔴 CRITICAL |
| 1.7 | `/api/updateTimesheetStatus` | POST | `updateTimesheetStatus()` | `updateTimesheetStatus()` | 🟡 HIGH |

---

### DELETE Operations (3 APIs)

| # | API Endpoint | HTTP Method | Controller Method | Service Method | Priority |
|---|--------------|-------------|-------------------|----------------|----------|
| 1.8 | `/api/deleteTimesheet` | DELETE | `deleteTimesheet()` | `deleteTimesheet()` | 🟡 MEDIUM |
| 1.9 | `/api/deleteProjectFromTimesheet` | DELETE | `deleteProjectFromTimesheet()` | `deleteProjectFromTimesheet()` | 🟡 MEDIUM |
| 1.10 | `/api/deleteActivityFromTimesheet` | DELETE | `deleteActivityFromTimesheet()` | `deleteActivityFromTimesheet()` | 🟡 MEDIUM |

---

## Total Phase 1 APIs: 10

---

## Classes to be Created

### 1. DTOs (Data Transfer Objects)

#### 1.1 EmployeeTimesheetDTO
**File:** `src/main/java/com/apmosys/employeeportal/dto/EmployeeTimesheetDTO.java`  
**Purpose:** Represents employee-level timesheet data (one per day per employee)  
**Maps to:** `employee_timesheets_new` table

#### 1.2 ProjectTimesheetDTO
**File:** `src/main/java/com/apmosys/employeeportal/dto/ProjectTimesheetDTO.java`  
**Purpose:** Represents project-level timesheet data (multiple per day)  
**Maps to:** `project_timesheet_status_new` table

#### 1.3 ActivityTimesheetDTO
**File:** `src/main/java/com/apmosys/employeeportal/dto/ActivityTimesheetDTO.java`  
**Purpose:** Represents activity-level timesheet data (nested under projects)  
**Maps to:** `employee_timesheet_activities_mapping_new` table

#### 1.4 TimesheetDTO (Updated)
**File:** `src/main/java/com/apmosys/employeeportal/dto/TimesheetDTO.java`  
**Purpose:** Wrapper DTO containing EmployeeTimesheetDTO and List<ProjectTimesheetDTO>  
**Action:** Update existing file to support new hierarchical structure

---

### 2. Service Classes

#### 2.1 EmployeeTimesheetService
**File:** `src/main/java/com/apmosys/employeeportal/service/EmployeeTimesheetService.java`  
**Purpose:** Service for EmployeeTimesheet CRUD operations  
**Methods:**
- `create(EmployeeTimesheetDTO dto)`
- `findById(Long timesheetId)`
- `findByEmpIdAndDate(Long empId, LocalDate date)`
- `findByEmpIdAndDateRange(Long empId, LocalDate startDate, LocalDate endDate)`
- `update(EmployeeTimesheetDTO dto)`
- `delete(Long timesheetId)`
- `calculateStatus(List<ProjectTimesheetDTO> projects)`
- `calculateTotals(EmployeeTimesheetDTO dto, List<ProjectTimesheetDTO> projects)`

#### 2.2 ProjectTimesheetService
**File:** `src/main/java/com/apmosys/employeeportal/service/ProjectTimesheetService.java`  
**Purpose:** Service for ProjectTimesheet CRUD operations  
**Methods:**
- `create(Long timesheetId, ProjectTimesheetDTO dto)`
- `findByTimesheetId(Long timesheetId)`
- `findByTimesheetIdAndProjectId(Long timesheetId, Long projectId)`
- `update(ProjectTimesheetDTO dto)`
- `delete(Long timesheetId, Long projectId)`
- `calculateProjectTotals(ProjectTimesheetDTO dto)`

#### 2.3 ActivityTimesheetService
**File:** `src/main/java/com/apmosys/employeeportal/service/ActivityTimesheetService.java`  
**Purpose:** Service for ActivityTimesheet CRUD operations  
**Methods:**
- `create(Long timesheetId, Long projectId, ActivityTimesheetDTO dto)`
- `findByTimesheetId(Long timesheetId)`
- `findByTimesheetIdAndProjectId(Long timesheetId, Long projectId)`
- `update(ActivityTimesheetDTO dto)`
- `delete(Long timesheetId, Long activityId, Long projectId)`
- `calculateActivityTotals(List<ActivityTimesheetDTO> activities)`

#### 2.4 TimesheetService (Updated)
**File:** `src/main/java/com/apmosys/employeeportal/service/TimesheetService.java`  
**Purpose:** Main service orchestrating all timesheet operations  
**New Methods:**
- `createTimesheet(TimesheetDTO dto, MultipartFile doc1, MultipartFile doc2)`
- `getTimesheetById(Long timesheetId)`
- `getTimesheetByDate(Long empId, LocalDate date)`
- `getTimesheetsByDateRange(Long empId, LocalDate startDate, LocalDate endDate)`
- `updateTimesheet(TimesheetDTO dto, MultipartFile doc1, MultipartFile doc2)`
- `updateTimesheetStatus(Long timesheetId, Long projectId, Integer status, Long updatedBy)`
- `deleteTimesheet(Long timesheetId)`
- `deleteProjectFromTimesheet(Long timesheetId, Long projectId)`
- `deleteActivityFromTimesheet(Long timesheetId, Long activityId, Long projectId)`

---

### 3. Repository Interfaces (Already Exist - Verify)

#### 3.1 EmployeeTimesheetsNewRepository
**File:** `src/main/java/com/apmosys/employeeportal/repository/EmployeeTimesheetsNewRepository.java`  
**Status:** ✅ Already exists  
**Action:** Verify methods available

#### 3.2 ProjectTimesheetStatusNewRepository
**File:** `src/main/java/com/apmosys/employeeportal/repository/ProjectTimesheetStatusNewRepository.java`  
**Status:** Need to verify if exists  
**Action:** Check and create if needed

#### 3.3 TimesheetActivityMapNewRepository
**File:** `src/main/java/com/apmosys/employeeportal/repository/TimesheetActivityMapNewRepository.java`  
**Status:** ✅ Already exists  
**Action:** Verify methods available

---

### 4. Helper/Utility Classes

#### 4.1 TimesheetAggregationHelper
**File:** `src/main/java/com/apmosys/employeeportal/service/helper/TimesheetAggregationHelper.java`  
**Purpose:** Helper class for calculating totals and aggregations  
**Methods:**
- `calculateTotalActivitiesMinutes(List<ActivityTimesheetDTO> activities)`
- `calculateTotalWorkingMinutes(LocalDateTime officeInTime, LocalDateTime officeOutTime)`
- `calculateTotalClientWorkingMinutes(List<ActivityTimesheetDTO> activities)`
- `calculateEmployeeTimesheetStatus(List<ProjectTimesheetDTO> projects)`

#### 4.2 TimesheetMapper
**File:** `src/main/java/com/apmosys/employeeportal/service/mapper/TimesheetMapper.java`  
**Purpose:** Mapper for converting between DTOs and Entities  
**Methods:**
- `toEntity(EmployeeTimesheetDTO dto)`
- `toDTO(EmployeeTimesheetsNew entity)`
- `toEntity(ProjectTimesheetDTO dto, Long timesheetId)`
- `toDTO(ProjectTimesheetStatusNew entity)`
- `toEntity(ActivityTimesheetDTO dto, Long timesheetId, Long projectId)`
- `toDTO(EmployeeTimesheetActivitiesMappingNew entity)`
- `toTimesheetDTO(EmployeeTimesheetsNew empTS, List<ProjectTimesheetStatusNew> projects, Map<Long, List<EmployeeTimesheetActivitiesMappingNew>> activities)`

---

### 5. Validation Classes

#### 5.1 TimesheetValidationHelper
**File:** `src/main/java/com/apmosys/employeeportal/service/validator/TimesheetValidationHelper.java`  
**Purpose:** Validation helper for new timesheet structure  
**Methods:**
- `validateEmployeeTimesheet(EmployeeTimesheetDTO dto)`
- `validateProjectTimesheet(ProjectTimesheetDTO dto, Long empId)`
- `validateActivityTimesheet(ActivityTimesheetDTO dto, Long projectId)`
- `validateTimesheetStructure(TimesheetDTO dto)`
- `validateDateNotInFuture(LocalDate date)`
- `validateDateNotLocked(Long empId, LocalDate date)`
- `validateOfficeTimes(LocalDateTime officeInTime, LocalDateTime officeOutTime)`
- `validateClientTimes(LocalDateTime clientInTime, LocalDateTime clientOutTime)`
- `validateActivityDuration(Integer durationMinutes)`
- `validateProjectAssignment(Long empId, Long projectId)`

---

## Class Dependencies

```
TimesheetController
    └── TimesheetService
            ├── EmployeeTimesheetService
            │       └── EmployeeTimesheetsNewRepository
            ├── ProjectTimesheetService
            │       └── ProjectTimesheetStatusNewRepository
            ├── ActivityTimesheetService
            │       └── TimesheetActivityMapNewRepository
            ├── TimesheetAggregationHelper
            ├── TimesheetMapper
            └── TimesheetValidationHelper
```

---

## Implementation Order

1. **Step 1:** Create DTOs (EmployeeTimesheetDTO, ProjectTimesheetDTO, ActivityTimesheetDTO)
2. **Step 2:** Update TimesheetDTO
3. **Step 3:** Create Helper Classes (TimesheetAggregationHelper, TimesheetMapper, TimesheetValidationHelper)
4. **Step 4:** Create Service Classes (EmployeeTimesheetService, ProjectTimesheetService, ActivityTimesheetService)
5. **Step 5:** Update TimesheetService with new methods
6. **Step 6:** Update TimesheetController with new endpoints
7. **Step 7:** Testing

---

**Last Updated:** 2025-01-30  
**Next Action:** Create all classes for review

