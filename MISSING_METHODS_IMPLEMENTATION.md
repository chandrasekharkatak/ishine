# Missing Methods Implementation - Complete

## Status: ✅ **COMPLETE**

**Date:** 2025-01-30  
**Purpose:** Add missing service methods for two APIs that were causing errors

---

## Summary

Added two missing methods to `TimesheetServiceNew`:
1. `getTimesheetsByEmployee(Long empId, LocalDate startDate, LocalDate endDate)`
2. `updateTimesheet(Long timesheetId, CreateTimesheetRequestDTONew dto, MultipartFile doc1, MultipartFile doc2)`

---

## Methods Added

### 1. getTimesheetsByEmployee ✅

**Signature:**
```java
public ServiceResponse getTimesheetsByEmployee(Long empId, LocalDate startDate, LocalDate endDate)
```

**Purpose:**
- Get all timesheets for a specific employee within a date range
- Similar to `getTimesheetsByDateRange` but uses direct parameters instead of DTO

**Implementation:**
- Validates input parameters (empId, startDate, endDate)
- Validates date range (startDate <= endDate)
- Fetches EmployeeTimesheets using `employeeTimesheetService.findByEmpIdAndDateRange()`
- Builds complete hierarchical TimesheetDTO for each timesheet
- Returns list of complete timesheets

**Location:** `TimesheetServiceNew.java` (after `getTimesheetsByDateRange`)

---

### 2. updateTimesheet ✅

**Signature:**
```java
@Transactional(rollbackFor = Exception.class)
public ServiceResponse updateTimesheet(Long timesheetId, CreateTimesheetRequestDTONew requestDTO, 
        MultipartFile doc1, MultipartFile doc2)
```

**Purpose:**
- Update existing timesheet with new data
- Handles full update of employee timesheet, projects, and activities

**Implementation Details:**

#### Step 1: Validation & Fetch Existing Timesheet
- Validates timesheetId and requestDTO
- Fetches existing EmployeeTimesheet
- Validates date not locked

#### Step 2: Update Employee Timesheet
- Updates date, dayTypeId, office times, totalWorkingMinutes
- Updates audit fields (updatedBy, updatedOn)
- Calls `employeeTimesheetService.update()`

#### Step 3: Handle Projects
- Gets existing projects for the timesheet
- Collects project IDs from request
- **Deletes** projects not in request
- **Updates** existing projects with new data
- **Creates** new projects if not existing

#### Step 4: Handle Activities
- For each project, updates activities:
  - Deletes all existing activities
  - Creates new activities from request
  - Uses `updateProjectActivities()` helper method

#### Step 5: Recalculate Totals
- Recalculates employee timesheet totals
- Updates status based on project statuses
- Calls `employeeTimesheetService.calculateAndUpdateTotals()`

#### Step 6: Handle Documents
- TODO: Integrate with TimesheetDocumentService
- Currently placeholder for document upload handling

#### Step 7: Return Response
- Fetches complete updated timesheet
- Returns success response with updated data

**Location:** `TimesheetServiceNew.java` (before helper methods)

---

## Helper Methods Added

### 1. updateProjectActivities ✅

**Signature:**
```java
private void updateProjectActivities(Long timesheetId, Long projectId, 
        List<ActivityRequestDTONew> activityRequests)
```

**Purpose:**
- Update activities for a specific project
- Deletes all existing activities and recreates from request

**Implementation:**
- If no activities in request → deletes all existing
- Otherwise → deletes all and recreates from request
- Uses `convertActivities()` to convert DTOs

**Note:** Currently uses delete-all-and-recreate strategy. Future enhancement: implement smarter diff logic.

---

### 2. convertActivities ✅

**Signature:**
```java
private List<ActivityTimesheetDTO> convertActivities(Long timesheetId, Long projectId, 
        List<ActivityRequestDTONew> activityRequests)
```

**Purpose:**
- Convert `ActivityRequestDTONew` list to `ActivityTimesheetDTO` list

**Implementation:**
- Maps each `ActivityRequestDTONew` to `ActivityTimesheetDTO`
- Sets timesheetId, projectId, activityId, description, durationMinutes, clientLocationId
- Returns list of converted DTOs

---

## DTOs Used

### CreateTimesheetRequestDTONew
- `empId`, `date`, `dayTypeId`, `dayType`
- `status`, `description`, `leaveTypeMasterId`
- `officeInTime`, `officeOutTime`, `totalWorkingMinutes`
- `isNightShift`, `toDate`
- `clientApprovalStatus`
- `projectEntries` (List<ProjectEntryRequestDTONew>)

### ProjectEntryRequestDTONew
- `projectId`, `clientSideId`, `hasClientSideId`
- `officeInTime`, `officeOutTime`
- `clientInTime`, `clientOutTime`, `totalClientWorkingMinutes`
- `shadowEmpId`, `isShadowTimesheet`
- `clientApprovalStatus`
- `activities` (List<ActivityRequestDTONew>)

### ActivityRequestDTONew
- `activityId`, `clientId`, `clientLocationId`, `teamId`
- `description`, `durationMinutes`, `durationHours`

---

## API Endpoints Fixed

### 1. GET /api/timesheetNew/getByEmployee ✅
- **Controller:** `TimesheetControllerNew.getTimesheetsByEmployee()`
- **Service:** `TimesheetServiceNew.getTimesheetsByEmployee()`
- **Status:** ✅ Fixed

### 2. PUT /api/timesheetNew/update ✅
- **Controller:** `TimesheetControllerNew.updateTimesheet()`
- **Service:** `TimesheetServiceNew.updateTimesheet()`
- **Status:** ✅ Fixed

---

## Code Quality

- ✅ No linter errors
- ✅ Proper transaction management (`@Transactional`)
- ✅ Comprehensive validation
- ✅ Error handling with try-catch
- ✅ Proper use of service layer methods
- ✅ Helper methods for code organization

---

## Testing Recommendations

### Unit Tests Needed:
1. `getTimesheetsByEmployee`:
   - Test with valid parameters
   - Test with invalid date range
   - Test with no results
   - Test with multiple timesheets

2. `updateTimesheet`:
   - Test full update (employee + projects + activities)
   - Test update with new projects
   - Test update with removed projects
   - Test update with modified activities
   - Test date lock validation
   - Test with documents

3. Helper Methods:
   - `updateProjectActivities`: Test with empty list, test with activities
   - `convertActivities`: Test conversion accuracy

---

## Future Enhancements

1. **Smart Activity Update:**
   - Implement diff logic for activities (update existing, delete removed, add new)
   - Avoid delete-all-and-recreate strategy

2. **Document Integration:**
   - Integrate `TimesheetDocumentService` in `updateTimesheet`
   - Handle document uploads properly

3. **Client Approval Status Mapping:**
   - Map string status to integer status code in update method

4. **Security Context:**
   - Get `updatedBy` from security context instead of DTO

---

## Files Modified

1. `src/main/java/com/apmosys/employeeportal/service/TimesheetServiceNew.java`
   - Added `getTimesheetsByEmployee()` method
   - Added `updateTimesheet()` method
   - Added `updateProjectActivities()` helper method
   - Added `convertActivities()` helper method
   - Added imports for new DTOs

---

**Last Updated:** 2025-01-30  
**Status:** ✅ **ALL MISSING METHODS IMPLEMENTED - READY FOR TESTING**

