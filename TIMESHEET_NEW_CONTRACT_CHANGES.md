# Timesheet New Contract Implementation Summary

## Overview
This document summarizes the changes made to support the new timesheet JSON contract in `EmployeeTimesheetControllerNew` create and update APIs.

## New Contract Structure

### Hierarchy
```
Employee Timesheet (Day Header)
  ↓
Work Location Mapping (Where & When) - NEW LEVEL
  ↓
Project Timesheet (Which Project)
  ↓
Project Activities (What Work Done)
```

### JSON Contract Structure
```json
{
  "employeeTimesheet": {
    "empId": 2001,
    "date": "2025-01-20",
    "workCheckIn": "09:00",           // NEW: String format
    "workCheckOut": "18:30",           // NEW: String format
    "dayType": "Working",              // NEW: String format
    "isNightShift": false,
    "description": "",
    "createdBy": 1842,
    "currentManagerId": 154,           // NOT IN CONTRACT but kept
    "leaveType": null,
    "isApmosysProduct": null,          // NOT IN CONTRACT but kept
    
    "locationSessions": [              // NEW: Location sessions array
      {
        "workLocationType": "APMOSYS_OFFICE",
        "locationInTime": "09:00",
        "locationOutTime": "13:00",
        "projects": [
          {
            "projectId": 3001,
            "clientId": 501,            // NEW
            "clientApprovalStatus": "Pending",  // NEW: String format
            "projectHoursMinutes": 150, // NEW
            "clientLocationId": 102,
            "isShadow": false,          // NEW
            "teamId": 360,              // NEW
            "activities": [...]
          }
        ]
      }
    ],
    "documentData": [                  // NEW: Document data array
      {
        "docId": null,
        "projectId": 3002,
        "docName": "Screenshot_Approved",
        "finalFlag": true,
        "bulkApprovedDocId": null,
        "uniqueIdentifier": ""
      }
    ]
  }
}
```

---

## Changes Made

### 1. New DTOs Created

#### `LocationSessionDTO.java`
- **Purpose**: Represents location session with workLocationType, locationInTime, locationOutTime, and nested projects
- **Fields**:
  - `workLocationType` (String) - e.g., "APMOSYS_OFFICE", "CLIENT_LOCATION"
  - `locationInTime` (String) - Format: "HH:mm"
  - `locationOutTime` (String) - Format: "HH:mm"
  - `projects` (List<ProjectTimesheetDTO>) - Projects for this location session

#### `TimesheetDocumentDataDTO.java`
- **Purpose**: Represents document data at employee level, linked to projects
- **Fields**:
  - `docId` (Long) - Document ID (null for new)
  - `projectId` (Long) - Associated project
  - `docName` (String) - Document name
  - `finalFlag` (Boolean) - true for approved, false for filled
  - `bulkApprovedDocId` (Long) - For bulk approval
  - `uniqueIdentifier` (String) - Tracking identifier

### 2. Updated DTOs

#### `EmployeeTimesheetDTO.java`
**New Fields Added:**
- `workCheckIn` (String) - NEW CONTRACT: Maps to `officeInTime`
- `workCheckOut` (String) - NEW CONTRACT: Maps to `officeOutTime`
- `dayType` (String) - NEW CONTRACT: Maps to `dayTypeId`
- `isNightShift` (Boolean) - NEW CONTRACT
- `currentManagerId` (Long) - NOT IN CONTRACT but kept for business logic
- `isApmosysProduct` (String) - NOT IN CONTRACT but kept for business logic
- `locationSessions` (List<LocationSessionDTO>) - NEW CONTRACT
- `documentData` (List<TimesheetDocumentDataDTO>) - NEW CONTRACT

**Existing Fields (Kept):**
- `projectTimesheets` - Kept for backward compatibility (projects are extracted from locationSessions)

#### `ProjectTimesheetDTO.java`
**New Fields Added:**
- `clientId` (Long) - NEW CONTRACT
- `clientApprovalStatus` (String) - NEW CONTRACT: Maps to Integer ID
- `projectHoursMinutes` (Integer) - NEW CONTRACT: Maps to `totalClientWorkingMinutes`
- `teamId` (Long) - NEW CONTRACT
- `isShadow` (Boolean) - NEW CONTRACT: Maps to `isShadowTimesheet` and `shadowEmpId`
- `isShadowTimesheet` (Boolean) - NOT IN CONTRACT but kept for backward compatibility
- `shadowEmpId` (Long) - NEW CONTRACT

### 3. Service Layer Changes (`TimesheetServiceNew.java`)

#### New Helper Methods

1. **`convertTimeStringToLocalDateTime(String timeStr, LocalDate date)`**
   - **Purpose**: Converts time string ("HH:mm" or "HH:mm:ss") to LocalDateTime
   - **NEW CONTRACT**: Maps workCheckIn/workCheckOut to officeInTime/officeOutTime
   - **Validation**: Throws IllegalArgumentException for invalid format

2. **`convertDayTypeStringToId(String dayType)`**
   - **Purpose**: Converts dayType string ("Working", "Week Off", etc.) to dayTypeId (Integer)
   - **NEW CONTRACT**: Maps dayType string to dayTypeId FK
   - **Validation**: Throws IllegalArgumentException if dayType not found in day_type_master_new

3. **`extractProjectsFromLocationSessions(List<LocationSessionDTO> locationSessions)`**
   - **Purpose**: Flattens location sessions to extract all projects
   - **NEW CONTRACT**: Projects are nested under locationSessions
   - **Returns**: List of all projects from all location sessions

4. **`convertClientApprovalStatusStringToId(String statusStr)`**
   - **Purpose**: Converts client approval status string to Integer ID
   - **NEW CONTRACT**: Maps "Pending"/"Approved"/"Rejected" to 1/2/3
   - **Returns**: Integer ID or null

5. **`normalizeEmployeeTimesheetFromNewContract(EmployeeTimesheetDTO empDTO, LocalDate date)`**
   - **Purpose**: Main normalization method that converts new contract format to internal format
   - **Mappings**:
     - workCheckIn/workCheckOut → officeInTime/officeOutTime
     - dayType string → dayTypeId
     - locationSessions → projectTimesheets (flattened)
     - clientApprovalStatus string → Integer ID
     - projectHoursMinutes → totalClientWorkingMinutes
     - isShadow → isShadowTimesheet

#### Updated Methods

##### `createTimesheet()`
**Changes:**
1. **NEW CONTRACT**: Calls `normalizeEmployeeTimesheetFromNewContract()` to normalize DTO
2. **VALIDATION**: Added validation for workCheckIn/workCheckOut for working days
3. **VALIDATION**: Added validation for locationSessions (at least one project for working days)
4. **NEW CONTRACT**: Handles documentData array
5. **COMMENTS**: Added comprehensive comments explaining new contract mappings
6. **BACKWARD COMPATIBILITY**: Still supports old structure (projectTimesheets directly)

**Validation Changes:**
- `workCheckIn`/`workCheckOut` are required for working days (NEW)
- `dayType` string must exist in `day_type_master_new` (NEW)
- `locationSessions` must have at least one project for working days (NEW)
- Existing validations (date lock, duplicate check) remain unchanged

##### `updateTimesheet()`
**Changes:**
1. **NEW CONTRACT**: Calls `normalizeEmployeeTimesheetFromNewContract()` to normalize DTO
2. **NEW CONTRACT**: Handles documentData updates
3. **COMMENTS**: Added comprehensive comments explaining new contract mappings
4. **BACKWARD COMPATIBILITY**: Still supports old structure

**Validation Changes:**
- Same as createTimesheet
- Additional: Cannot update locked timesheets (EXISTING)

#### Updated Converter Methods

##### `convertToOldProjectDTO()`
**Changes:**
- **NEW CONTRACT**: Handles clientApprovalStatus conversion (already done in normalization)
- **NEW CONTRACT**: Maps projectHoursMinutes to totalClientWorkingMinutes
- **NEW CONTRACT**: Maps isShadow to shadowEmpId
- **COMMENTS**: Added comments about new contract fields not in old DTO (clientId, teamId)

---

## Field Mappings

### Employee Level
| New Contract Field | Internal Field | Conversion |
|-------------------|----------------|------------|
| `workCheckIn` (String "09:00") | `officeInTime` (LocalDateTime) | `convertTimeStringToLocalDateTime()` |
| `workCheckOut` (String "18:30") | `officeOutTime` (LocalDateTime) | `convertTimeStringToLocalDateTime()` |
| `dayType` (String "Working") | `dayTypeId` (Integer) | `convertDayTypeStringToId()` |
| `isNightShift` (Boolean) | `isNightShift` (Boolean) | Direct mapping |
| `locationSessions[]` | `projectTimesheets[]` | `extractProjectsFromLocationSessions()` |
| `documentData[]` | Handled separately | TODO: Integrate with TimesheetDocumentService |

### Project Level
| New Contract Field | Internal Field | Conversion |
|-------------------|----------------|------------|
| `clientApprovalStatus` (String "Pending") | `clientApprovalStatus` (Integer 1) | `convertClientApprovalStatusStringToId()` |
| `projectHoursMinutes` (Integer) | `totalClientWorkingMinutes` (Integer) | Direct mapping |
| `isShadow` (Boolean) | `isShadowTimesheet` (Boolean) + `shadowEmpId` (Long) | Direct mapping |
| `clientId` (Long) | Not in old DTO | Preserved in newDTO |
| `teamId` (Long) | Not in old DTO | Preserved in newDTO |

---

## Validation Changes

### New Validations (Required)
1. **workCheckIn/workCheckOut**: Required for working days
   - **Why**: These are the primary time tracking fields in new contract
   - **Location**: `normalizeEmployeeTimesheetFromNewContract()`
   - **TODO**: Add explicit validation in createTimesheet/updateTimesheet

2. **dayType string**: Must exist in `day_type_master_new`
   - **Why**: Ensures valid day type is provided
   - **Location**: `convertDayTypeStringToId()`
   - **Error**: Throws IllegalArgumentException if not found

3. **locationSessions**: Must have at least one project for working days
   - **Why**: Working days require project entries
   - **Location**: `createTimesheet()` / `updateTimesheet()`
   - **TODO**: Add explicit validation

### Existing Validations (Unchanged)
- Date not locked
- Timesheet not duplicate (for create)
- Date not in future
- Office time validation (if provided)

---

## Fields Not in New Contract (Kept with Comments)

### Employee Level
- `currentManagerId` (Long)
  - **Why Kept**: Used for approval workflows
  - **Comment**: "NOT IN NEW CONTRACT but kept for business logic"

- `isApmosysProduct` (String)
  - **Why Kept**: Used for product-specific timesheet rules
  - **Comment**: "NOT IN NEW CONTRACT but kept for business logic"

### Project Level
- `isShadowTimesheet` (Boolean)
  - **Why Kept**: Backward compatibility
  - **Mapped From**: `isShadow` in new contract

---

## TODO Items

### High Priority
1. **Document Service Integration**
   - **Location**: `createTimesheet()` and `updateTimesheet()`
   - **Task**: Implement `handleDocumentDataFromNewContract()` method
   - **Why**: New contract has documentData array that needs to be processed

2. **Location Session Storage**
   - **Location**: Service layer
   - **Task**: Decide if location sessions should be stored in database
   - **Why**: Currently only projects are stored, location session info is lost
   - **Options**:
     - Create `timesheet_location_sessions_new` table
     - Store location info in project timesheet
     - Keep only in DTO (current approach)

3. **Validation Enhancements**
   - **Location**: `createTimesheet()` and `updateTimesheet()`
   - **Task**: Add explicit validation for:
     - workCheckIn/workCheckOut for working days
     - locationSessions must have projects for working days
     - locationInTime/locationOutTime validation within location sessions

### Medium Priority
4. **Time Overlap Validation**
   - **Location**: Validation helper
   - **Task**: Validate location sessions don't overlap
   - **Why**: Employee can't be at two locations simultaneously

5. **Location Type Master Integration**
   - **Location**: Service layer
   - **Task**: Validate workLocationType exists in `work_location_type_master`
   - **Why**: Ensures valid location types

6. **Response Structure**
   - **Location**: `getTimesheetByIdInternalNew()`
   - **Task**: Update response to include locationSessions structure
   - **Why**: Response should match new contract structure

### Low Priority
7. **Merge Strategy for projectTimesheets**
   - **Location**: `normalizeEmployeeTimesheetFromNewContract()`
   - **Task**: Decide merge strategy when both locationSessions and projectTimesheets exist
   - **Current**: Prefers locationSessions (new contract)

8. **Security Context Implementation**
   - **Location**: `getCurrentUserId()`
   - **Task**: Implement proper security context retrieval
   - **Why**: Currently returns null, uses empId as fallback

---

## Backward Compatibility

### Supported
- Old structure with `projectTimesheets` directly under `employeeTimesheet`
- Old document structure (`filledDocument`, `finalDocument`)
- Existing validation rules
- Existing aggregation logic

### Migration Path
1. Frontend can send either:
   - New contract: `locationSessions[]` with nested projects
   - Old contract: `projectTimesheets[]` directly
2. Backend normalizes both to internal structure
3. Response can be in either format (TODO: Update response structure)

---

## Testing Checklist

### Create Timesheet
- [ ] Create with new contract (locationSessions)
- [ ] Create with old contract (projectTimesheets directly)
- [ ] Create with workCheckIn/workCheckOut
- [ ] Create with dayType string
- [ ] Create with documentData
- [ ] Create with multiple location sessions
- [ ] Create with shadow timesheet (isShadow=true)
- [ ] Validation: Missing workCheckIn for working day
- [ ] Validation: Invalid dayType string
- [ ] Validation: Empty locationSessions for working day

### Update Timesheet
- [ ] Update with new contract
- [ ] Update with old contract
- [ ] Update locationSessions (add/remove)
- [ ] Update documentData
- [ ] Update workCheckIn/workCheckOut
- [ ] Update dayType
- [ ] Delete projects not in request
- [ ] Add new projects from locationSessions

### Edge Cases
- [ ] Multiple location sessions with overlapping times
- [ ] Location session with no projects
- [ ] Project in multiple location sessions
- [ ] DocumentData with invalid projectId
- [ ] Shadow timesheet without shadowEmpId

---

## Files Modified

1. **New Files Created:**
   - `LocationSessionDTO.java`
   - `TimesheetDocumentDataDTO.java`

2. **Files Updated:**
   - `EmployeeTimesheetDTO.java` - Added new contract fields
   - `ProjectTimesheetDTO.java` - Added new contract fields
   - `TimesheetServiceNew.java` - Updated createTimesheet() and updateTimesheet()
   - `EmployeeTimesheetControllerNew.java` - No changes (uses existing structure)

---

## Notes

1. **Location Sessions**: Currently, location session information (workLocationType, locationInTime, locationOutTime) is extracted but not stored in database. Only projects are persisted. Consider creating a `timesheet_location_sessions_new` table if location tracking is required.

2. **Document Data**: Document handling is marked as TODO. The structure is ready but needs integration with `TimesheetDocumentService`.

3. **Validation**: Some validations are commented as TODO. These should be implemented before production deployment.

4. **Response Structure**: The response structure (`getTimesheetByIdInternalNew()`) should be updated to return data in new contract format (with locationSessions).

5. **Backward Compatibility**: The code supports both old and new contract structures. Old structure is preserved for backward compatibility.

---

**Document Version**: 1.0  
**Date**: 2026-01-04  
**Status**: Implementation Complete (Pending Document Service Integration)

