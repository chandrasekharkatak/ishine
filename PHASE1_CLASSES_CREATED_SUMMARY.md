# Phase 1: Foundation & Core CRUD - Classes Created Summary

## Overview
All classes for Phase 1 have been created and are ready for review.

**Date:** 2025-01-30  
**Status:** ✅ **CLASSES CREATED - READY FOR REVIEW**

---

## Phase 1 APIs List

### CREATE Operations (2 APIs)
1. **`POST /api/createTimesheet`** - Create new timesheet (NEW)
2. **`POST /api/addTimesheetWithClient`** - Create timesheet with client (MODIFY)

### READ Operations (3 APIs)
3. **`GET /api/getTimesheetById`** - Get timesheet by ID (NEW)
4. **`POST /api/getTimesheetByDate`** - Get timesheet by date (NEW)
5. **`POST /api/getTimesheetsByDateRange`** - Get timesheets by date range (NEW)

### UPDATE Operations (2 APIs)
6. **`POST /api/updateTimesheet`** - Update timesheet (MODIFY)
7. **`POST /api/updateTimesheetStatus`** - Update timesheet status (NEW)

### DELETE Operations (3 APIs)
8. **`DELETE /api/deleteTimesheet`** - Delete timesheet (NEW)
9. **`DELETE /api/deleteProjectFromTimesheet`** - Delete project from timesheet (NEW)
10. **`DELETE /api/deleteActivityFromTimesheet`** - Delete activity from timesheet (NEW)

**Total: 10 APIs**

---

## Classes Created

### 1. DTOs (Data Transfer Objects) ✅

#### 1.1 EmployeeTimesheetDTO
**File:** `src/main/java/com/apmosys/employeeportal/dto/EmployeeTimesheetDTO.java`  
**Status:** ✅ Created  
**Fields:**
- `timesheetId` (Long)
- `empId` (Long) - Required
- `date` (LocalDate) - Required
- `dayTypeId` (Integer) - Required
- `leaveTypeId` (Long) - Optional
- `status` (Integer) - Calculated
- `totalWorkingMinutes` (Integer) - Calculated
- `totalActivitiesMinutes` (Integer) - Calculated
- `officeInTime` (LocalDateTime) - Optional
- `officeOutTime` (LocalDateTime) - Optional
- `createdBy`, `createdOn`, `updatedBy`, `updatedOn` - Audit fields

**Validation:**
- `@NotNull` on required fields
- `@PastOrPresent` on date
- Jackson annotations for JSON mapping

---

#### 1.2 ProjectTimesheetDTO
**File:** `src/main/java/com/apmosys/employeeportal/dto/ProjectTimesheetDTO.java`  
**Status:** ✅ Created  
**Fields:**
- `timesheetId` (Long) - Required
- `projectId` (Long) - Required
- `poNo` (String) - Optional
- `poId` (Long) - Optional
- `clientInTime` (LocalDateTime) - Optional
- `clientOutTime` (LocalDateTime) - Optional
- `isNightShift` (Boolean) - Optional
- `clientApprovalStatus` (Integer) - Optional
- `status` (Integer) - Required
- `shadowEmpId` (Long) - Optional
- `totalClientWorkingMinutes` (Integer) - Calculated
- `activities` (List<ActivityTimesheetDTO>) - Required for working days

**Validation:**
- `@NotNull` on required fields
- `@Valid` on activities list

---

#### 1.3 ActivityTimesheetDTO
**File:** `src/main/java/com/apmosys/employeeportal/dto/ActivityTimesheetDTO.java`  
**Status:** ✅ Created  
**Fields:**
- `timesheetId` (Long) - Required
- `activityId` (Long) - Required
- `projectId` (Long) - Required
- `description` (String) - Optional
- `durationMinutes` (Integer) - Required, min 1
- `clientLocationId` (Long) - Optional

**Validation:**
- `@NotNull` on required fields
- `@Min(1)` on durationMinutes

---

#### 1.4 TimesheetDTO (Updated)
**File:** `src/main/java/com/apmosys/employeeportal/dto/TimesheetDTO.java`  
**Status:** ✅ Updated  
**New Fields:**
- `employeeTimesheet` (EmployeeTimesheetDTO) - NEW
- `projectTimesheets` (List<ProjectTimesheetDTO>) - NEW

**Backward Compatibility:**
- All existing fields maintained
- All existing constructors maintained
- New constructor added for hierarchical structure

---

### 2. Helper Classes ✅

#### 2.1 TimesheetAggregationHelper
**File:** `src/main/java/com/apmosys/employeeportal/service/helper/TimesheetAggregationHelper.java`  
**Status:** ✅ Created  
**Methods:**
- `calculateTotalActivitiesMinutes(List<ProjectTimesheetDTO>)` - Sum all activity durations
- `calculateTotalWorkingMinutes(LocalDateTime, LocalDateTime)` - Calculate from office times
- `calculateTotalClientWorkingMinutes(List<ActivityTimesheetDTO>)` - Sum project activity durations
- `calculateTotalClientWorkingMinutesFromTimes(LocalDateTime, LocalDateTime)` - Calculate from client times
- `calculateEmployeeTimesheetStatus(List<ProjectTimesheetDTO>)` - Calculate status from projects
- `calculateAndSetEmployeeTimesheetTotals(EmployeeTimesheetDTO, List<ProjectTimesheetDTO>)` - Calculate and set all totals
- `calculateAndSetProjectTimesheetTotals(ProjectTimesheetDTO)` - Calculate project totals
- `calculateAndSetAllProjectTimesheetTotals(List<ProjectTimesheetDTO>)` - Calculate all project totals

**Status Constants:**
- `STATUS_PENDING = 1`
- `STATUS_APPROVED = 2`
- `STATUS_REJECTED = 3`
- `STATUS_PARTIAL = 4`

---

#### 2.2 TimesheetMapper
**File:** `src/main/java/com/apmosys/employeeportal/service/mapper/TimesheetMapper.java`  
**Status:** ✅ Created  
**Methods:**
- `toEntity(EmployeeTimesheetDTO)` → `EmployeeTimesheetsNew`
- `toDTO(EmployeeTimesheetsNew)` → `EmployeeTimesheetDTO`
- `toEntity(ProjectTimesheetDTO, Long)` → `ProjectTimesheetStatusNew`
- `toDTO(ProjectTimesheetStatusNew)` → `ProjectTimesheetDTO`
- `toEntity(ActivityTimesheetDTO, Long, Long)` → `EmployeeTimesheetActivitiesMappingNew`
- `toDTO(EmployeeTimesheetActivitiesMappingNew)` → `ActivityTimesheetDTO`
- `toTimesheetDTO(EmployeeTimesheetsNew, List<ProjectTimesheetStatusNew>, Map<Long, List<EmployeeTimesheetActivitiesMappingNew>>)` → `TimesheetDTO`
- `toActivityDTOList(List<EmployeeTimesheetActivitiesMappingNew>)` → `List<ActivityTimesheetDTO>`
- `toProjectDTOList(List<ProjectTimesheetStatusNew>)` → `List<ProjectTimesheetDTO>`
- `groupActivitiesByProject(List<EmployeeTimesheetActivitiesMappingNew>)` → `Map<Long, List<EmployeeTimesheetActivitiesMappingNew>>`

---

#### 2.3 TimesheetValidationHelper
**File:** `src/main/java/com/apmosys/employeeportal/service/validator/TimesheetValidationHelper.java`  
**Status:** ✅ Created  
**Methods:**
- `validateEmployeeTimesheet(EmployeeTimesheetDTO)` - Validate employee timesheet
- `validateProjectTimesheet(ProjectTimesheetDTO, Long)` - Validate project timesheet
- `validateActivityTimesheet(ActivityTimesheetDTO, Long)` - Validate activity timesheet
- `validateTimesheetStructure(TimesheetDTO)` - Validate complete structure
- `validateDateNotInFuture(LocalDate)` - Validate date
- `isDateLocked(Long, LocalDate, Integer)` - Check if date is locked
- `validateOfficeTimes(LocalDateTime, LocalDateTime)` - Validate office times
- `validateClientTimes(LocalDateTime, LocalDateTime)` - Validate client times
- `validateActivityDuration(Integer)` - Validate activity duration
- `validateProjectAssignment(Long, Long)` - Validate employee assigned to project
- `validateDateNotLocked(Long, LocalDate, Integer)` - Validate date not locked

**Dependencies:**
- `EmployeeRepository`
- `ProjectRepository`
- `EmployeeTeamMapRepository`
- `ActivitiesRepository`

---

### 3. Service Classes ✅

#### 3.1 EmployeeTimesheetService
**File:** `src/main/java/com/apmosys/employeeportal/service/EmployeeTimesheetService.java`  
**Status:** ✅ Created  
**Methods:**
- `create(EmployeeTimesheetDTO)` → `EmployeeTimesheetsNew`
- `findById(Long)` → `EmployeeTimesheetDTO`
- `findByEmpIdAndDate(Long, LocalDate)` → `EmployeeTimesheetDTO`
- `findByEmpIdAndDateRange(Long, LocalDate, LocalDate)` → `List<EmployeeTimesheetDTO>`
- `update(EmployeeTimesheetDTO)` → `EmployeeTimesheetsNew`
- `delete(Long)` → void
- `calculateAndUpdateStatus(Long, List<ProjectTimesheetDTO>)` → `Integer`
- `calculateAndUpdateTotals(Long, EmployeeTimesheetDTO, List<ProjectTimesheetDTO>)` → void
- `existsByEmpIdAndDate(Long, LocalDate)` → `boolean`

**Dependencies:**
- `EmployeeTimesheetsNewRepository`
- `TimesheetMapper`
- `TimesheetAggregationHelper`

---

#### 3.2 ProjectTimesheetService
**File:** `src/main/java/com/apmosys/employeeportal/service/ProjectTimesheetService.java`  
**Status:** ✅ Created  
**Methods:**
- `create(Long, ProjectTimesheetDTO)` → `ProjectTimesheetStatusNew`
- `findByTimesheetId(Long)` → `List<ProjectTimesheetDTO>`
- `findByTimesheetIdAndProjectId(Long, Long)` → `ProjectTimesheetDTO`
- `update(ProjectTimesheetDTO)` → `ProjectTimesheetStatusNew`
- `delete(Long, Long)` → void
- `deleteByTimesheetId(Long)` → void
- `calculateProjectTotals(ProjectTimesheetDTO)` → `ProjectTimesheetDTO`
- `exists(Long, Long)` → `boolean`

**Dependencies:**
- `ProjectTimesheetStatusNewRepository`
- `TimesheetMapper`
- `TimesheetAggregationHelper`

---

#### 3.3 ActivityTimesheetService
**File:** `src/main/java/com/apmosys/employeeportal/service/ActivityTimesheetService.java`  
**Status:** ✅ Created  
**Methods:**
- `create(Long, Long, ActivityTimesheetDTO)` → `EmployeeTimesheetActivitiesMappingNew`
- `createAll(Long, Long, List<ActivityTimesheetDTO>)` → `List<EmployeeTimesheetActivitiesMappingNew>`
- `findByTimesheetId(Long)` → `List<ActivityTimesheetDTO>`
- `findByTimesheetIdAndProjectId(Long, Long)` → `List<ActivityTimesheetDTO>`
- `findByCompositeKey(Long, Long, Long)` → `ActivityTimesheetDTO`
- `update(ActivityTimesheetDTO)` → `EmployeeTimesheetActivitiesMappingNew`
- `delete(Long, Long, Long)` → void
- `deleteByTimesheetId(Long)` → void
- `deleteByTimesheetIdAndProjectId(Long, Long)` → void
- `calculateActivityTotals(List<ActivityTimesheetDTO>)` → `Integer`
- `exists(Long, Long, Long)` → `boolean`

**Dependencies:**
- `TimesheetActivityMapNewRepository`
- `TimesheetMapper`

---

### 4. Repository Updates ✅

#### 4.1 EmployeeTimesheetsNewRepository
**File:** `src/main/java/com/apmosys/employeeportal/repository/EmployeeTimesheetsNewRepository.java`  
**Status:** ✅ Updated  
**New Methods Added:**
- `findByEmpIdAndDateNew(Long, LocalDate)` → `Optional<EmployeeTimesheetsNew>`
- `findAllByEmpIdAndDateBetweenOrderByDateDescNew(Long, LocalDate, LocalDate)` → `List<EmployeeTimesheetsNew>`

**Note:** Old methods maintained for backward compatibility.

---

#### 4.2 TimesheetActivityMapNewRepository
**File:** `src/main/java/com/apmosys/employeeportal/repository/TimesheetActivityMapNewRepository.java`  
**Status:** ✅ Updated  
**New Methods Added:**
- `findByTimesheetId(Long)` → `List<EmployeeTimesheetActivitiesMappingNew>` (default method)

**Note:** Uses existing `findByIdTimesheetId` method.

---

## Class Dependencies Diagram

```
TimesheetController
    └── TimesheetService (to be updated)
            ├── EmployeeTimesheetService
            │       ├── EmployeeTimesheetsNewRepository
            │       ├── TimesheetMapper
            │       └── TimesheetAggregationHelper
            ├── ProjectTimesheetService
            │       ├── ProjectTimesheetStatusNewRepository
            │       ├── TimesheetMapper
            │       └── TimesheetAggregationHelper
            ├── ActivityTimesheetService
            │       ├── TimesheetActivityMapNewRepository
            │       └── TimesheetMapper
            ├── TimesheetAggregationHelper
            ├── TimesheetMapper
            └── TimesheetValidationHelper
                    ├── EmployeeRepository
                    ├── ProjectRepository
                    ├── EmployeeTeamMapRepository
                    └── ActivitiesRepository
```

---

## Design Decisions

### 1. DTO Structure
- **Hierarchical DTOs:** Separate DTOs for each level (Employee, Project, Activity)
- **Wrapper DTO:** TimesheetDTO wraps all levels for complete request/response
- **Backward Compatibility:** All old TimesheetDTO fields maintained

### 2. Service Layer
- **Separation of Concerns:** Three separate services for each level
- **Transaction Management:** `@Transactional` on write operations
- **Validation:** Centralized in TimesheetValidationHelper

### 3. Aggregation Logic
- **Centralized Calculations:** All aggregation logic in TimesheetAggregationHelper
- **Status Calculation:** Derived from project statuses
- **Total Calculations:** From activities or time differences

### 4. Mapping
- **Bidirectional Mapping:** DTO ↔ Entity conversion
- **Composite Key Handling:** Proper mapping for composite keys
- **Grouping:** Activities grouped by project

---

## Review Checklist

### DTOs
- [ ] Review EmployeeTimesheetDTO field types and validations
- [ ] Review ProjectTimesheetDTO field types and validations
- [ ] Review ActivityTimesheetDTO field types and validations
- [ ] Review TimesheetDTO backward compatibility
- [ ] Verify JSON serialization/deserialization

### Helper Classes
- [ ] Review TimesheetAggregationHelper calculation logic
- [ ] Review TimesheetMapper mapping logic
- [ ] Review TimesheetValidationHelper validation rules
- [ ] Verify composite key handling

### Service Classes
- [ ] Review EmployeeTimesheetService CRUD operations
- [ ] Review ProjectTimesheetService CRUD operations
- [ ] Review ActivityTimesheetService CRUD operations
- [ ] Verify transaction boundaries
- [ ] Verify error handling

### Repository Methods
- [ ] Verify EmployeeTimesheetsNewRepository new methods
- [ ] Verify TimesheetActivityMapNewRepository methods
- [ ] Verify ProjectTimesheetStatusNewRepository methods

---

## Next Steps

1. **Review all classes** for low-level design
2. **Suggest any changes** needed
3. **Approve design** before implementation
4. **Begin implementation** of TimesheetService methods
5. **Create controller endpoints**

---

**Last Updated:** 2025-01-30  
**Status:** ✅ **ALL CLASSES CREATED - READY FOR REVIEW**

