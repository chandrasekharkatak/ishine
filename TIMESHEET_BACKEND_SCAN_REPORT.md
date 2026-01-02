# Timesheet Backend Service Layer - Scanned Analysis Report

## Report Date
Generated: Current Date

## Executive Summary

**Current State**: The timesheet system currently supports **single project per timesheet** with one set of in/out times.

**Required State**: Support for **multiple projects per timesheet**, each with:
- Different in-time and out-time
- Different sets of activities

**Impact**: **HIGH** - Requires new table, updated entities, new DTOs, and service layer refactoring.

---

## Current Architecture Scan

### Existing New Tables (with "New" prefix)

| Table Name | Entity | Repository | Current Purpose | Limitation |
|------------|--------|------------|-----------------|------------|
| `employee_timesheets_new` | `EmployeeTimesheetsNew` | `EmployeeTimesheetsNewRepository` | Main timesheet record | Single in/out time per timesheet |
| `employee_timesheet_activities_mapping_new` | `EmployeeTimesheetActivitiesMappingNew` | `TimesheetActivityMapNewRepository` | Activity mappings | No project-specific time linkage |

### Current Data Flow

```
Timesheet (1) → Activities (Many)
     ↓
Single officeInTime/officeOutTime
     ↓
Activities linked to projects via composite key (timesheetId, activityId, projectId)
```

**Problem**: Activities are linked to projects, but there's no way to store different in/out times for different projects.

---

## Required Changes Summary

### 1. Database Schema Changes

#### ✅ NEW TABLE REQUIRED
**Table**: `timesheet_project_entries_new`
**Purpose**: Store project-specific time entries
**Key Fields**:
- `project_entry_id` (PK)
- `timesheet_id` (FK)
- `project_id` (FK)
- `office_in_time`, `office_out_time`
- `client_in_time`, `client_out_time`
- `total_working_minutes`
- `client_approval_status`
- `shadow_emp_id`

**Unique Constraint**: `(timesheet_id, project_id)` - One entry per project per timesheet

#### ✅ TABLE MODIFICATION REQUIRED
**Table**: `employee_timesheet_activities_mapping_new`
**Change**: Add `project_entry_id` column (FK to `timesheet_project_entries_new`)
**Purpose**: Link activities to specific project entries

---

### 2. Entity Changes

#### New Entity Required
- ✅ `TimesheetProjectEntryNew` - Entity for project entries

#### Entity Modifications Required
- ✅ `EmployeeTimesheetActivitiesMappingNew` - Add `projectEntryId` field

---

### 3. Repository Changes

#### New Repository Required
- ✅ `TimesheetProjectEntryNewRepository` - CRUD operations for project entries

#### Repository Methods Required
```java
- findByTimesheetId(Long timesheetId)
- findByTimesheetIdAndProjectId(Long timesheetId, Integer projectId)
- deleteByTimesheetId(Long timesheetId)
- findByEmpIdAndDateRange(Long empId, LocalDate startDate, LocalDate endDate)
```

#### Updated Repository Methods Required
- ✅ `TimesheetActivityMapNewRepository` - Add methods:
  - `findByProjectEntryId(Long projectEntryId)`
  - `deleteByProjectEntryId(Long projectEntryId)`

---

### 4. DTO Changes

#### New DTOs Required
1. ✅ `TimesheetProjectEntryDTO` - Project entry data transfer
2. ✅ `ActivityDTO` - Activity data transfer
3. ✅ `CreateTimesheetRequestDTO` - Request for creating timesheet
4. ✅ `ProjectEntryRequestDTO` - Request for project entry
5. ✅ `ActivityRequestDTO` - Request for activity

#### Updated DTOs Required
- ✅ `TimesheetDTO` - Add `List<TimesheetProjectEntryDTO> projectEntries` field

---

### 5. Service Layer Changes

#### New Service Methods Required

| Method | Purpose | Complexity |
|--------|---------|------------|
| `createTimesheetWithMultipleProjects()` | Create timesheet with multiple project entries | HIGH |
| `updateTimesheetWithMultipleProjects()` | Update timesheet (add/remove/modify projects) | HIGH |
| `getTimesheetWithProjectEntries()` | Retrieve timesheet with all project entries | MEDIUM |
| `deleteTimesheetWithProjectEntries()` | Cascade delete project entries and activities | MEDIUM |
| `validateProjectEntryTimes()` | Validate in/out times per project | MEDIUM |
| `calculateProjectWorkingMinutes()` | Calculate working minutes per project | LOW |

#### Service Logic Changes

**Current Flow**:
```
1. Create Timesheet
2. Create Activities (linked to timesheet and project)
```

**New Flow**:
```
1. Create Timesheet
2. For each project:
   a. Create Project Entry (with in/out times)
   b. Create Activities (linked to project entry)
```

---

### 6. API Endpoint Changes

#### New Endpoints Required

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/timesheet/create-multi-project` | POST | Create timesheet with multiple projects |
| `/api/timesheet/update-multi-project/{id}` | PUT | Update timesheet with multiple projects |
| `/api/timesheet/{id}/with-projects` | GET | Get timesheet with project entries |
| `/api/timesheet/employee/{empId}/range` | GET | Get timesheets with project entries by date range |

#### Existing Endpoints Status
- ⚠️ **Backward Compatibility**: Existing endpoints should continue to work
- ⚠️ **Migration Path**: Consider deprecation timeline for old endpoints

---

## Data Model Comparison

### Current Model (Single Project)
```
Timesheet
├── officeInTime (single)
├── officeOutTime (single)
└── Activities[]
    ├── activityId
    ├── projectId
    └── durationMinutes
```

### New Model (Multiple Projects)
```
Timesheet
├── totalWorkingMinutes (sum of all projects)
└── ProjectEntries[]
    ├── projectId
    ├── officeInTime (project-specific)
    ├── officeOutTime (project-specific)
    ├── clientInTime (project-specific)
    ├── clientOutTime (project-specific)
    └── Activities[]
        ├── activityId
        ├── projectEntryId (NEW)
        └── durationMinutes
```

---

## Validation Requirements

### Business Rules to Implement

1. **Time Validation**:
   - ✅ Out-time must be after in-time (per project)
   - ✅ Project working hours must be positive
   - ⚠️ Time overlap validation (optional - if projects can't overlap)

2. **Activity Validation**:
   - ✅ Activity duration must be positive
   - ✅ Sum of activity durations ≤ project working hours
   - ✅ Activities must belong to the project they're assigned to

3. **Project Entry Validation**:
   - ✅ At least one project entry required for working days
   - ✅ Project entry must have valid in/out times
   - ✅ Total timesheet minutes = sum of project entry minutes

---

## Migration Impact Assessment

### Database Impact
- **New Table**: 1 table (`timesheet_project_entries_new`)
- **Table Modification**: 1 table (`employee_timesheet_activities_mapping_new`)
- **Indexes Required**: 3 new indexes
- **Data Migration**: Optional (if migrating existing data)

### Code Impact
- **New Entities**: 1 entity
- **Modified Entities**: 1 entity
- **New Repositories**: 1 repository
- **Modified Repositories**: 1 repository
- **New DTOs**: 5 DTOs
- **Modified DTOs**: 1 DTO
- **New Service Methods**: 6+ methods
- **New API Endpoints**: 4 endpoints

### Testing Impact
- **Unit Tests**: ~15-20 new test cases
- **Integration Tests**: ~10-15 new test cases
- **E2E Tests**: ~5-8 new test scenarios

---

## Risk Assessment

| Risk | Level | Mitigation |
|------|-------|------------|
| Data loss during migration | MEDIUM | Implement rollback mechanism, backup before migration |
| Performance degradation | LOW | Proper indexing, query optimization |
| Backward compatibility issues | MEDIUM | Maintain old endpoints, gradual migration |
| Validation logic errors | HIGH | Comprehensive testing, code reviews |

---

## Implementation Priority

### Phase 1: Foundation (Week 1-2)
1. ✅ Create `timesheet_project_entries_new` table
2. ✅ Create `TimesheetProjectEntryNew` entity
3. ✅ Create `TimesheetProjectEntryNewRepository`
4. ✅ Add `project_entry_id` to activity mapping table

### Phase 2: DTOs and Service Layer (Week 2-3)
1. ✅ Create all required DTOs
2. ✅ Implement `createTimesheetWithMultipleProjects()`
3. ✅ Implement `getTimesheetWithProjectEntries()`
4. ✅ Implement validation methods

### Phase 3: API Endpoints (Week 3-4)
1. ✅ Create new API endpoints
2. ✅ Implement update and delete methods
3. ✅ Add error handling and logging

### Phase 4: Testing and Migration (Week 4-5)
1. ✅ Unit tests
2. ✅ Integration tests
3. ✅ Data migration scripts (if needed)
4. ✅ Documentation

---

## Dependencies

### Required Dependencies
- ✅ Spring Data JPA (already present)
- ✅ Lombok (already present)
- ✅ Java Time API (already present)

### External Dependencies
- None - all required libraries are already in the project

---

## Recommendations

### 1. Database Design
- ✅ Use composite unique constraint on `(timesheet_id, project_id)`
- ✅ Add proper indexes for performance
- ✅ Consider adding `created_on`, `updated_on` timestamps

### 2. Service Layer
- ✅ Implement transaction management for atomic operations
- ✅ Add comprehensive logging
- ✅ Implement proper error handling
- ✅ Consider caching for frequently accessed data

### 3. API Design
- ✅ Use consistent naming conventions
- ✅ Implement proper HTTP status codes
- ✅ Add request/response validation
- ✅ Document all endpoints (Swagger/OpenAPI)

### 4. Testing
- ✅ Achieve >80% code coverage
- ✅ Test all validation scenarios
- ✅ Test edge cases (empty lists, null values, etc.)
- ✅ Performance testing for bulk operations

---

## Conclusion

The refactoring is **FEASIBLE** and **WELL-DEFINED**. The changes are:
- ✅ **Clear**: Requirements are well understood
- ✅ **Isolated**: New functionality doesn't break existing code
- ✅ **Testable**: All components can be unit tested
- ✅ **Scalable**: Design supports future enhancements

**Estimated Effort**: 4-5 weeks for complete implementation and testing.

**Recommendation**: ✅ **PROCEED** with implementation following the phased approach outlined above.

---

## Appendix: Code Locations

### Current Files to Review
- `src/main/java/com/apmosys/employeeportal/model/EmployeeTimesheetsNew.java`
- `src/main/java/com/apmosys/employeeportal/model/EmployeeTimesheetActivitiesMappingNew.java`
- `src/main/java/com/apmosys/employeeportal/repository/EmployeeTimesheetsNewRepository.java`
- `src/main/java/com/apmosys/employeeportal/repository/TimesheetActivityMapNewRepository.java`
- `src/main/java/com/apmosys/employeeportal/service/TimesheetService.java`

### New Files to Create
- `src/main/java/com/apmosys/employeeportal/model/TimesheetProjectEntryNew.java`
- `src/main/java/com/apmosys/employeeportal/repository/TimesheetProjectEntryNewRepository.java`
- `src/main/java/com/apmosys/employeeportal/dto/TimesheetProjectEntryDTO.java`
- `src/main/java/com/apmosys/employeeportal/dto/ActivityDTO.java`
- `src/main/java/com/apmosys/employeeportal/dto/CreateTimesheetRequestDTO.java`
- `src/main/java/com/apmosys/employeeportal/service/TimesheetServiceNew.java` (or extend existing)

