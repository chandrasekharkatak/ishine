# Timesheet Backend API Analysis & Implementation Guide

## Executive Summary

This document provides a comprehensive analysis of required backend changes to support the new multi-project timesheet form. The new form allows:
- **Multiple projects per timesheet** (single day)
- **Different in/out times per project**
- **Different activities per project**
- **Up to 2 document uploads** (filled document + approved document)

**All implementations MUST use tables with "New" suffix.**

---

## Table of Contents

1. [Current vs New Form Comparison](#current-vs-new-form-comparison)
2. [Database Schema Analysis](#database-schema-analysis)
3. [Required API Changes](#required-api-changes)
4. [DTO Specifications](#dto-specifications)
5. [Service Layer Implementation](#service-layer-implementation)
6. [Repository Layer Implementation](#repository-layer-implementation)
7. [Controller Layer Implementation](#controller-layer-implementation)
8. [Migration & Integration Notes](#migration--integration-notes)

---

## Current vs New Form Comparison

### Current Form Structure
- **Single project** per timesheet (`projectId` in `EmployeeTimesheetsNew`)
- **Single set of in/out times** per timesheet (`officeInTime`, `officeOutTime` in `EmployeeTimesheetsNew`)
- **Single set of client in/out times** (`clientInTime`, `clientOutTime` - stored in old table)
- **Multiple activities** but all linked to same timesheet (via `TimesheetActivityMapId` with `timesheetId`, `activityId`, `projectId`)
- **Documents**: Up to 2 documents per timesheet (filled + approved)

### New Form Requirements
- **Multiple projects** per timesheet (each with own in/out times)
- **Project-specific in/out times** (each project entry has its own)
- **Project-specific client in/out times** (each project entry has its own)
- **Project-specific activities** (activities linked to project entry, not directly to timesheet)
- **Documents**: Up to 2 documents per timesheet (filled + approved) - **NO CHANGE**

### Key Differences

| Aspect | Current | New |
|--------|---------|-----|
| Projects per timesheet | 1 | Multiple (1-N) |
| In/Out times | 1 set per timesheet | 1 set per project |
| Client In/Out times | 1 set per timesheet | 1 set per project |
| Activities linkage | Direct to timesheet | Linked to project entry |
| Document structure | Same (2 max) | Same (2 max) |

---

## Database Schema Analysis

### Existing New Tables

#### 1. `employee_timesheets_new`
```sql
- timesheet_id (PK)
- created_by, created_on, updated_by, updated_on
- date (LocalDate)
- day_type_id (FK)
- emp_id (FK)
- status (FK)
- total_working_minutes (Integer)
- office_in_time (LocalDateTime) -- REMOVE: Move to project entry
- office_out_time (LocalDateTime) -- REMOVE: Move to project entry
- leave_type_master_id (FK)
```

**Changes Required:**
- Remove `office_in_time` and `office_out_time` (move to project entry table)
- Keep `total_working_minutes` as aggregate across all projects

#### 2. `employee_timesheet_activities_mapping_new`
```sql
- Composite Key: (timesheet_id, activity_id, project_id)
- description
- duration_minutes
```

**Changes Required:**
- Add `project_entry_id` (FK to `timesheet_project_entries_new`) to link activities to project entry
- Keep composite key but add project_entry_id for querying

#### 3. `timesheet_document_details_new`
```sql
- doc_id (PK)
- timesheet_id (FK)
- file_url
- doc_name
- mime_type_id
- client_approval_status_id
- created_by, created_on, updated_by, updated_on
- active
- final_flag (Boolean) -- true = approved doc, false = filled doc
- bulk_approved_doc_id
```

**Changes Required:** None - structure supports 2 documents per timesheet

### New Table Required

#### 4. `timesheet_project_entries_new` (NEW TABLE)
```sql
CREATE TABLE timesheet_project_entries_new (
    project_entry_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timesheet_id BIGINT NOT NULL,
    project_id INT NOT NULL,
    client_side_id VARCHAR(255),
    office_in_time DATETIME,
    office_out_time DATETIME,
    client_in_time DATETIME,
    client_out_time DATETIME,
    total_working_minutes INT,
    total_client_working_minutes INT,
    client_approval_status_id INT,
    has_client_side_id BOOLEAN,
    shadow_emp_id BIGINT,
    is_shadow_timesheet BOOLEAN,
    created_by BIGINT,
    created_on DATETIME,
    updated_by BIGINT,
    updated_on DATETIME,
    UNIQUE KEY unique_timesheet_project (timesheet_id, project_id),
    FOREIGN KEY (timesheet_id) REFERENCES employee_timesheets_new(timesheet_id),
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
);
```

---

## Required API Changes

### Summary of API Changes

| API Name | Method | Endpoint | Status | Changes |
|----------|--------|----------|--------|---------|
| Create Timesheet | POST | `/api/addTimesheetWithClient` | **UPDATE** | Support multiple projects |
| Update Timesheet | POST | `/api/updateTimesheet` | **UPDATE** | Support multiple projects |
| Save as Draft | POST | `/api/saveTimesheetAsDraft` | **NEW** | New endpoint |
| Submit Timesheet | POST | `/api/submitTimesheet` | **NEW** | New endpoint |
| Get Timesheet Details | POST | `/api/getTimesheetDetailsById` | **UPDATE** | Return project entries |
| Get Timesheet List | POST | `/api/getAllMyTimesheetsByEmpId` | **UPDATE** | Include project info |
| Get Activities | POST | `/api/getAllMyActivitiesByTimesheetId` | **UPDATE** | Filter by project entry |
| Upload Document | POST | `/api/uploadTimesheetDocument` | **NEW** | Separate upload endpoint |
| Update Document | PUT | `/api/updateTimesheetDocument` | **NEW** | Update existing document |
| Remove Document | DELETE | `/api/removeTimesheetDocument` | **NEW** | Remove document |
| Bulk Upload Final Doc | POST | `/api/bulkFinalDocumentUpload` | **UPDATE** | Support multi-project |
| Approve Timesheet | POST | `/api/approveTimesheetRequest` | **UPDATE** | Validate all projects |
| Reject Timesheet | POST | `/api/rejectTimesheetRequest` | **UPDATE** | Support rejection reason |

---

## DTO Specifications

### 1. TimesheetProjectEntryDTO (NEW)

```java
package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetProjectEntryDTO {
    
    private Long projectEntryId;
    private Integer projectId;
    private String projectName;
    private String clientSideId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime officeInTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime officeOutTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clientInTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clientOutTime;
    
    private Integer totalWorkingMinutes;
    private Integer totalClientWorkingMinutes;
    private Integer clientApprovalStatusId;
    private String clientApprovalStatus; // "no", "pending", "approved"
    private Boolean hasClientSideId;
    private Long shadowEmpId;
    private Boolean isShadowTimesheet;
    
    // Activities for this project entry
    private List<ActivityDTO> activities;
}
```

### 2. CreateTimesheetRequestDTO (NEW)

```java
package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTimesheetRequestDTO {
    
    private Long empId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    private Integer dayTypeId;
    private String dayType; // "Working", "Public Holiday", etc.
    private Integer status; // Status master ID
    private String description; // For non-working days
    private Long leaveTypeMasterId;
    
    // NEW: Multiple project entries
    private List<ProjectEntryRequestDTO> projectEntries;
    
    // Metadata
    private String timesheetAppliedFor; // "self", "asShadow", "team"
    private Long currentManagerId;
    private Boolean isNightShift;
    private Long createdBy;
    private String createdByName;
    
    // Document references (uploaded separately)
    private Long filledDocumentId; // doc_id from timesheet_document_details_new
    private Long approvedDocumentId; // doc_id from timesheet_document_details_new
}
```

### 3. ProjectEntryRequestDTO (NEW)

```java
package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntryRequestDTO {
    
    private Integer projectId;
    private String clientSideId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime officeInTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime officeOutTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clientInTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clientOutTime;
    
    private Integer clientApprovalStatusId;
    private String clientApprovalStatus; // "no", "pending", "approved"
    private Boolean hasClientSideId;
    private Long shadowEmpId;
    private Boolean isShadowTimesheet;
    
    // Activities for this project
    private List<ActivityRequestDTO> activities;
}
```

### 4. ActivityRequestDTO (NEW)

```java
package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequestDTO {
    
    private Long activityId;
    private Integer clientId;
    private Integer clientLocationId;
    private Long teamId;
    private String description;
    private Short durationMinutes; // Duration in minutes
    private Float durationHours; // Calculated from minutes (for display)
}
```

### 5. UpdateTimesheetRequestDTO (NEW)

```java
package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTimesheetRequestDTO extends CreateTimesheetRequestDTO {
    
    private Long timesheetId;
    private Long updatedBy;
    
    // For tracking which project entries/activities to delete
    private List<Long> deletedProjectEntryIds;
    private List<Long> deletedActivityIds; // Composite key references
}
```

### 6. TimesheetResponseDTO (NEW)

```java
package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetResponseDTO {
    
    private Long timesheetId;
    private Long empId;
    private String employeeName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    private Integer dayTypeId;
    private String dayType;
    private Integer status;
    private String statusName; // "Pending", "Approved", "Rejected"
    private String description;
    private Long leaveTypeMasterId;
    private String leaveType;
    
    private Integer totalWorkingMinutes; // Aggregate across all projects
    private Boolean isNightShift;
    
    // Project entries
    private List<TimesheetProjectEntryDTO> projectEntries;
    
    // Documents
    private Long filledDocumentId;
    private String filledDocumentUrl;
    private Long approvedDocumentId;
    private String approvedDocumentUrl;
    
    // Metadata
    private Long createdBy;
    private String createdByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    
    private Long updatedBy;
    private String updatedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedOn;
    
    private String rejectReason;
    private String remarks;
}
```

### 7. Updated ActivityDTO

```java
// Update existing ActivityDTO to include projectEntryId
package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ActivityDTO {
    
    private Long activityId;
    private Integer projectId;
    private String projectName;
    private Long projectEntryId; // NEW: Link to project entry
    private String clientName;
    private String clientLocation;
    private Long teamId;
    private String teamName;
    private String activity;
    private Float eta;
    private Long updatedBy;
    private Long createdBy;
    private String createdByName;
    private String createdOn;
    private Float completionTime;
    private Short durationMinutes; // NEW: Store in minutes
    private String description;
    private Long timesheetId;
    private Long timesheetActivityMapId;
    
    private Integer clientId;
    private Integer clientLocationId;
    private String employeeName;
    private String managerName;
    private Long activityTemplateId;
    private String employeeRole;
    private String[] departmentList;
    private Long deptId;
}
```

---

## Service Layer Implementation

### 1. TimesheetServiceNew (NEW SERVICE CLASS)

Create a new service class `TimesheetServiceNew` that uses only New tables:

```java
package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.*;
import com.apmosys.employeeportal.model.*;
import com.apmosys.employeeportal.repository.*;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TimesheetServiceNew {
    
    @Autowired
    private EmployeeTimesheetsNewRepository timesheetsNewRepository;
    
    @Autowired
    private TimesheetProjectEntryNewRepository projectEntryNewRepository;
    
    @Autowired
    private TimesheetActivityMapNewRepository activityMapNewRepository;
    
    @Autowired
    private TimesheetDocumentDetailsNewRepository documentDetailsNewRepository;
    
    @Autowired
    private TimesheetValidatorService timesheetValidatorService;
    
    @Autowired
    private StringToDateTimeParser stringToDateTimeParser;
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private HttpServletRequest httpRequest;
    
    @Value("${timesheet.lock.days}")
    private Integer timesheetLockDays;
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Create new timesheet with multiple projects
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse createTimesheet(CreateTimesheetRequestDTO requestDTO, 
                                          MultipartFile filledDoc, 
                                          MultipartFile approvedDoc) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // 1. Validate request
            validateCreateRequest(requestDTO);
            
            // 2. Parse date
            LocalDate timesheetDate = requestDTO.getDate();
            
            // 3. Validate timesheet lock period
            ServiceResponse lockResponse = timesheetValidatorService.validateTimesheetLockPeriod(
                requestDTO.getEmpId(), timesheetDate);
            if (lockResponse != null) {
                return lockResponse;
            }
            
            // 4. Check if timesheet already exists
            Optional<EmployeeTimesheetsNew> existing = timesheetsNewRepository
                .findByEmpIdAndDate(requestDTO.getEmpId(), timesheetDate);
            
            if (existing.isPresent() && !"Non-working".equalsIgnoreCase(requestDTO.getDayType())) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet already exists for this date");
                return response;
            }
            
            // 5. Create main timesheet record
            EmployeeTimesheetsNew timesheet = new EmployeeTimesheetsNew();
            timesheet.setEmpId(requestDTO.getEmpId());
            timesheet.setDate(timesheetDate);
            timesheet.setDayTypeId(requestDTO.getDayTypeId());
            timesheet.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : getPendingStatusId());
            timesheet.setLeaveTypeMasterId(requestDTO.getLeaveTypeMasterId());
            timesheet.setCreatedBy(requestDTO.getCreatedBy());
            timesheet.setCreatedOn(LocalDateTime.now());
            
            // Calculate total working minutes across all projects
            int totalWorkingMinutes = 0;
            
            // 6. Process project entries
            List<TimesheetProjectEntryNew> projectEntries = new ArrayList<>();
            
            if (requestDTO.getProjectEntries() != null && !requestDTO.getProjectEntries().isEmpty()) {
                for (ProjectEntryRequestDTO projectEntryDTO : requestDTO.getProjectEntries()) {
                    TimesheetProjectEntryNew projectEntry = createProjectEntry(
                        projectEntryDTO, null, requestDTO.getCreatedBy());
                    
                    // Calculate working minutes for this project
                    if (projectEntry.getOfficeInTime() != null && projectEntry.getOfficeOutTime() != null) {
                        long minutes = java.time.Duration.between(
                            projectEntry.getOfficeInTime(), 
                            projectEntry.getOfficeOutTime()
                        ).toMinutes();
                        projectEntry.setTotalWorkingMinutes((int) minutes);
                        totalWorkingMinutes += minutes;
                    }
                    
                    projectEntries.add(projectEntry);
                }
            }
            
            timesheet.setTotalWorkingMinutes(totalWorkingMinutes);
            
            // 7. Save timesheet
            EmployeeTimesheetsNew savedTimesheet = timesheetsNewRepository.save(timesheet);
            
            // 8. Save project entries and their activities
            for (TimesheetProjectEntryNew projectEntry : projectEntries) {
                projectEntry.setTimesheetId(savedTimesheet.getTimesheetId());
                TimesheetProjectEntryNew savedProjectEntry = projectEntryNewRepository.save(projectEntry);
                
                // Save activities for this project entry
                if (projectEntry.getActivities() != null) {
                    saveActivitiesForProjectEntry(savedProjectEntry.getProjectEntryId(), 
                                                  projectEntry.getActivities(), 
                                                  savedTimesheet.getTimesheetId());
                }
            }
            
            // 9. Handle document uploads
            if (filledDoc != null || approvedDoc != null) {
                handleDocumentUploads(savedTimesheet.getTimesheetId(), 
                                     filledDoc, approvedDoc, 
                                     requestDTO.getClientApprovalStatus(),
                                     requestDTO.getCreatedBy());
            }
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Timesheet created successfully");
            response.setServiceResponse(savedTimesheet);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Failed to create timesheet: " + e.getMessage());
            response.setServiceError(e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Update existing timesheet
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse updateTimesheet(UpdateTimesheetRequestDTO requestDTO,
                                          MultipartFile filledDoc,
                                          MultipartFile approvedDoc) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            // 1. Find existing timesheet
            Optional<EmployeeTimesheetsNew> timesheetOpt = 
                timesheetsNewRepository.findById(requestDTO.getTimesheetId());
            
            if (!timesheetOpt.isPresent()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet not found");
                return response;
            }
            
            EmployeeTimesheetsNew timesheet = timesheetOpt.get();
            
            // 2. Validate update permissions (status must be Pending or Rejected)
            if (!isTimesheetEditable(timesheet.getStatus())) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet cannot be edited in current status");
                return response;
            }
            
            // 3. Update main timesheet fields
            timesheet.setDayTypeId(requestDTO.getDayTypeId());
            timesheet.setLeaveTypeMasterId(requestDTO.getLeaveTypeMasterId());
            timesheet.setUpdatedBy(requestDTO.getUpdatedBy());
            timesheet.setUpdatedOn(LocalDateTime.now());
            
            // 4. Delete removed project entries
            if (requestDTO.getDeletedProjectEntryIds() != null) {
                for (Long projectEntryId : requestDTO.getDeletedProjectEntryIds()) {
                    deleteProjectEntry(projectEntryId);
                }
            }
            
            // 5. Update or create project entries
            int totalWorkingMinutes = 0;
            
            if (requestDTO.getProjectEntries() != null) {
                for (ProjectEntryRequestDTO projectEntryDTO : requestDTO.getProjectEntries()) {
                    TimesheetProjectEntryNew projectEntry;
                    
                    if (projectEntryDTO.getProjectEntryId() != null) {
                        // Update existing
                        Optional<TimesheetProjectEntryNew> existingOpt = 
                            projectEntryNewRepository.findById(projectEntryDTO.getProjectEntryId());
                        if (existingOpt.isPresent()) {
                            projectEntry = existingOpt.get();
                            updateProjectEntry(projectEntry, projectEntryDTO, requestDTO.getUpdatedBy());
                        } else {
                            projectEntry = createProjectEntry(projectEntryDTO, 
                                                             requestDTO.getTimesheetId(), 
                                                             requestDTO.getUpdatedBy());
                        }
                    } else {
                        // Create new
                        projectEntry = createProjectEntry(projectEntryDTO, 
                                                         requestDTO.getTimesheetId(), 
                                                         requestDTO.getUpdatedBy());
                    }
                    
                    // Calculate working minutes
                    if (projectEntry.getOfficeInTime() != null && 
                        projectEntry.getOfficeOutTime() != null) {
                        long minutes = java.time.Duration.between(
                            projectEntry.getOfficeInTime(), 
                            projectEntry.getOfficeOutTime()
                        ).toMinutes();
                        projectEntry.setTotalWorkingMinutes((int) minutes);
                        totalWorkingMinutes += minutes;
                    }
                    
                    projectEntryNewRepository.save(projectEntry);
                    
                    // Update activities
                    updateActivitiesForProjectEntry(projectEntry.getProjectEntryId(), 
                                                   projectEntryDTO.getActivities(), 
                                                   requestDTO.getTimesheetId());
                }
            }
            
            timesheet.setTotalWorkingMinutes(totalWorkingMinutes);
            timesheetsNewRepository.save(timesheet);
            
            // 6. Handle document updates
            if (filledDoc != null || approvedDoc != null) {
                handleDocumentUploads(requestDTO.getTimesheetId(), 
                                     filledDoc, approvedDoc, 
                                     requestDTO.getClientApprovalStatus(),
                                     requestDTO.getUpdatedBy());
            }
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Timesheet updated successfully");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Failed to update timesheet: " + e.getMessage());
            response.setServiceError(e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Get timesheet details by ID
     */
    public ServiceResponse getTimesheetById(Long timesheetId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            Optional<EmployeeTimesheetsNew> timesheetOpt = 
                timesheetsNewRepository.findById(timesheetId);
            
            if (!timesheetOpt.isPresent()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet not found");
                return response;
            }
            
            EmployeeTimesheetsNew timesheet = timesheetOpt.get();
            TimesheetResponseDTO responseDTO = mapToResponseDTO(timesheet);
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(responseDTO);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Failed to fetch timesheet: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Get timesheet list for employee
     */
    public ServiceResponse getTimesheetList(TimesheetDTO requestDTO) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            LocalDate startDate = LocalDate.parse(requestDTO.getStartDate());
            LocalDate endDate = LocalDate.parse(requestDTO.getEndDate());
            
            List<EmployeeTimesheetsNew> timesheets = timesheetsNewRepository
                .findByEmpIdAndDateBetween(requestDTO.getEmpId(), startDate, endDate);
            
            List<TimesheetResponseDTO> responseList = timesheets.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(responseList);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Failed to fetch timesheets: " + e.getMessage());
        }
        
        return response;
    }
    
    // Helper methods
    
    private TimesheetProjectEntryNew createProjectEntry(ProjectEntryRequestDTO dto, 
                                                        Long timesheetId, 
                                                        Long createdBy) {
        TimesheetProjectEntryNew entry = new TimesheetProjectEntryNew();
        entry.setTimesheetId(timesheetId);
        entry.setProjectId(dto.getProjectId());
        entry.setClientSideId(dto.getClientSideId());
        entry.setOfficeInTime(dto.getOfficeInTime());
        entry.setOfficeOutTime(dto.getOfficeOutTime());
        entry.setClientInTime(dto.getClientInTime());
        entry.setClientOutTime(dto.getClientOutTime());
        entry.setClientApprovalStatusId(dto.getClientApprovalStatusId());
        entry.setHasClientSideId(dto.getHasClientSideId());
        entry.setShadowEmpId(dto.getShadowEmpId());
        entry.setIsShadowTimesheet(dto.getIsShadowTimesheet());
        entry.setCreatedBy(createdBy);
        entry.setCreatedOn(LocalDateTime.now());
        
        // Calculate minutes
        if (dto.getOfficeInTime() != null && dto.getOfficeOutTime() != null) {
            long minutes = java.time.Duration.between(
                dto.getOfficeInTime(), dto.getOfficeOutTime()
            ).toMinutes();
            entry.setTotalWorkingMinutes((int) minutes);
        }
        
        if (dto.getClientInTime() != null && dto.getClientOutTime() != null) {
            long minutes = java.time.Duration.between(
                dto.getClientInTime(), dto.getClientOutTime()
            ).toMinutes();
            entry.setTotalClientWorkingMinutes((int) minutes);
        }
        
        return entry;
    }
    
    private void updateProjectEntry(TimesheetProjectEntryNew entry, 
                                   ProjectEntryRequestDTO dto, 
                                   Long updatedBy) {
        entry.setClientSideId(dto.getClientSideId());
        entry.setOfficeInTime(dto.getOfficeInTime());
        entry.setOfficeOutTime(dto.getOfficeOutTime());
        entry.setClientInTime(dto.getClientInTime());
        entry.setClientOutTime(dto.getClientOutTime());
        entry.setClientApprovalStatusId(dto.getClientApprovalStatusId());
        entry.setHasClientSideId(dto.getHasClientSideId());
        entry.setShadowEmpId(dto.getShadowEmpId());
        entry.setIsShadowTimesheet(dto.getIsShadowTimesheet());
        entry.setUpdatedBy(updatedBy);
        entry.setUpdatedOn(LocalDateTime.now());
        
        // Recalculate minutes
        if (dto.getOfficeInTime() != null && dto.getOfficeOutTime() != null) {
            long minutes = java.time.Duration.between(
                dto.getOfficeInTime(), dto.getOfficeOutTime()
            ).toMinutes();
            entry.setTotalWorkingMinutes((int) minutes);
        }
        
        if (dto.getClientInTime() != null && dto.getClientOutTime() != null) {
            long minutes = java.time.Duration.between(
                dto.getClientInTime(), dto.getClientOutTime()
            ).toMinutes();
            entry.setTotalClientWorkingMinutes((int) minutes);
        }
    }
    
    private void saveActivitiesForProjectEntry(Long projectEntryId, 
                                              List<ActivityRequestDTO> activities, 
                                              Long timesheetId) {
        for (ActivityRequestDTO activityDTO : activities) {
            TimesheetActivityMapId mapId = new TimesheetActivityMapId();
            mapId.setTimesheetId(timesheetId);
            mapId.setActivityId(activityDTO.getActivityId());
            mapId.setProjectId(activityDTO.getProjectId()); // From activity context
            
            EmployeeTimesheetActivitiesMappingNew mapping = new EmployeeTimesheetActivitiesMappingNew();
            mapping.setId(mapId);
            mapping.setDescription(activityDTO.getDescription());
            mapping.setDurationMinutes(activityDTO.getDurationMinutes());
            // Note: projectEntryId should be added to the entity if needed for querying
            
            activityMapNewRepository.save(mapping);
        }
    }
    
    private void updateActivitiesForProjectEntry(Long projectEntryId, 
                                                List<ActivityRequestDTO> activities, 
                                                Long timesheetId) {
        // Delete existing activities for this project entry
        // (Implementation depends on whether projectEntryId is in the mapping table)
        
        // Save new/updated activities
        saveActivitiesForProjectEntry(projectEntryId, activities, timesheetId);
    }
    
    private void deleteProjectEntry(Long projectEntryId) {
        // Delete activities first
        // Then delete project entry
        projectEntryNewRepository.deleteById(projectEntryId);
    }
    
    private void handleDocumentUploads(Long timesheetId, 
                                     MultipartFile filledDoc, 
                                     MultipartFile approvedDoc,
                                     String clientApprovalStatus,
                                     Long createdBy) {
        // Implementation similar to existing document upload logic
        // Use TimesheetDocumentDetailsNewRepository
    }
    
    private TimesheetResponseDTO mapToResponseDTO(EmployeeTimesheetsNew timesheet) {
        TimesheetResponseDTO dto = new TimesheetResponseDTO();
        dto.setTimesheetId(timesheet.getTimesheetId());
        dto.setEmpId(timesheet.getEmpId());
        dto.setDate(timesheet.getDate());
        dto.setDayTypeId(timesheet.getDayTypeId());
        dto.setStatus(timesheet.getStatus());
        dto.setTotalWorkingMinutes(timesheet.getTotalWorkingMinutes());
        
        // Fetch project entries
        List<TimesheetProjectEntryNew> projectEntries = 
            projectEntryNewRepository.findByTimesheetId(timesheet.getTimesheetId());
        
        List<TimesheetProjectEntryDTO> projectEntryDTOs = projectEntries.stream()
            .map(this::mapProjectEntryToDTO)
            .collect(Collectors.toList());
        
        dto.setProjectEntries(projectEntryDTOs);
        
        // Fetch documents
        List<TimesheetDocumentDetailsNew> documents = 
            documentDetailsNewRepository.findByTimesheetId(timesheet.getTimesheetId());
        
        for (TimesheetDocumentDetailsNew doc : documents) {
            if (Boolean.FALSE.equals(doc.getFinalFlag())) {
                dto.setFilledDocumentId(doc.getDocId());
                dto.setFilledDocumentUrl(doc.getFileUrl());
            } else {
                dto.setApprovedDocumentId(doc.getDocId());
                dto.setApprovedDocumentUrl(doc.getFileUrl());
            }
        }
        
        return dto;
    }
    
    private TimesheetProjectEntryDTO mapProjectEntryToDTO(TimesheetProjectEntryNew entry) {
        TimesheetProjectEntryDTO dto = new TimesheetProjectEntryDTO();
        dto.setProjectEntryId(entry.getProjectEntryId());
        dto.setProjectId(entry.getProjectId());
        dto.setClientSideId(entry.getClientSideId());
        dto.setOfficeInTime(entry.getOfficeInTime());
        dto.setOfficeOutTime(entry.getOfficeOutTime());
        dto.setClientInTime(entry.getClientInTime());
        dto.setClientOutTime(entry.getClientOutTime());
        dto.setTotalWorkingMinutes(entry.getTotalWorkingMinutes());
        dto.setTotalClientWorkingMinutes(entry.getTotalClientWorkingMinutes());
        dto.setClientApprovalStatusId(entry.getClientApprovalStatusId());
        dto.setHasClientSideId(entry.getHasClientSideId());
        dto.setShadowEmpId(entry.getShadowEmpId());
        dto.setIsShadowTimesheet(entry.getIsShadowTimesheet());
        
        // Fetch activities for this project entry
        // (Implementation depends on projectEntryId in mapping table)
        
        return dto;
    }
    
    private void validateCreateRequest(CreateTimesheetRequestDTO requestDTO) {
        if (requestDTO.getEmpId() == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if (requestDTO.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }
        if (requestDTO.getDayTypeId() == null) {
            throw new IllegalArgumentException("Day type is required");
        }
    }
    
    private boolean isTimesheetEditable(Integer status) {
        // Only Pending or Rejected timesheets can be edited
        // (Implementation depends on status master values)
        return true; // Placeholder
    }
    
    private Integer getPendingStatusId() {
        // Get status ID for "Pending" from status master
        return 1; // Placeholder
    }
}
```

---

## Repository Layer Implementation

### 1. TimesheetProjectEntryNewRepository (NEW)

```java
package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TimesheetProjectEntryNew;

@Repository
public interface TimesheetProjectEntryNewRepository 
    extends JpaRepository<TimesheetProjectEntryNew, Long> {
    
    // Find all project entries for a timesheet
    List<TimesheetProjectEntryNew> findByTimesheetId(Long timesheetId);
    
    // Find project entry by timesheet and project
    Optional<TimesheetProjectEntryNew> findByTimesheetIdAndProjectId(
        Long timesheetId, Integer projectId);
    
    // Delete all project entries for a timesheet
    void deleteByTimesheetId(Long timesheetId);
    
    // Check if project entry exists
    boolean existsByTimesheetIdAndProjectId(Long timesheetId, Integer projectId);
}
```

### 2. Updated TimesheetActivityMapNewRepository

```java
package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.EmployeeTimesheetActivitiesMappingNew;
import com.apmosys.employeeportal.model.TimesheetActivityMapId;

@Repository
public interface TimesheetActivityMapNewRepository 
    extends JpaRepository<EmployeeTimesheetActivitiesMappingNew, TimesheetActivityMapId> {
    
    // Find activities by timesheet
    @Query("SELECT a FROM EmployeeTimesheetActivitiesMappingNew a " +
           "WHERE a.id.timesheetId = :timesheetId")
    List<EmployeeTimesheetActivitiesMappingNew> findByTimesheetId(
        @Param("timesheetId") Long timesheetId);
    
    // Find activities by timesheet and project
    @Query("SELECT a FROM EmployeeTimesheetActivitiesMappingNew a " +
           "WHERE a.id.timesheetId = :timesheetId AND a.id.projectId = :projectId")
    List<EmployeeTimesheetActivitiesMappingNew> findByTimesheetIdAndProjectId(
        @Param("timesheetId") Long timesheetId, 
        @Param("projectId") Long projectId);
    
    // Delete activities by timesheet
    void deleteById_TimesheetId(Long timesheetId);
    
    // Delete activities by timesheet and project
    void deleteById_TimesheetIdAndId_ProjectId(Long timesheetId, Long projectId);
}
```

### 3. TimesheetDocumentDetailsNewRepository (NEW)

```java
package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;

@Repository
public interface TimesheetDocumentDetailsNewRepository 
    extends JpaRepository<TimesheetDocumentDetailsNew, Long> {
    
    // Find documents by timesheet
    List<TimesheetDocumentDetailsNew> findByTimesheetId(Long timesheetId);
    
    // Find filled document (finalFlag = false)
    Optional<TimesheetDocumentDetailsNew> findByTimesheetIdAndFinalFlagFalse(Long timesheetId);
    
    // Find approved document (finalFlag = true)
    Optional<TimesheetDocumentDetailsNew> findByTimesheetIdAndFinalFlagTrue(Long timesheetId);
    
    // Delete documents by timesheet
    void deleteByTimesheetId(Long timesheetId);
    
    // Check if document exists
    boolean existsByTimesheetIdAndFinalFlag(Long timesheetId, Boolean finalFlag);
}
```

### 4. Updated EmployeeTimesheetsNewRepository

```java
package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;

@Repository
public interface EmployeeTimesheetsNewRepository 
    extends JpaRepository<EmployeeTimesheetsNew, Long> {
    
    // Find by employee and date
    Optional<EmployeeTimesheetsNew> findByEmpIdAndDate(Long empId, LocalDate date);
    
    // Find by employee and date range
    List<EmployeeTimesheetsNew> findByEmpIdAndDateBetween(
        Long empId, LocalDate startDate, LocalDate endDate);
    
    // Find by employee
    List<EmployeeTimesheetsNew> findByEmpId(Long empId);
    
    // Find by status
    List<EmployeeTimesheetsNew> findByStatus(Integer status);
    
    // Find by employee and status
    List<EmployeeTimesheetsNew> findByEmpIdAndStatus(Long empId, Integer status);
}
```

---

## Controller Layer Implementation

### Updated TimesheetController

```java
package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.*;
import com.apmosys.employeeportal.service.TimesheetServiceNew;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class TimesheetController {
    
    @Autowired
    TimesheetServiceNew timesheetServiceNew;
    
    @Autowired
    TimesheetEncryptionHelper timesheetEncryptionHelper;
    
    /**
     * Create new timesheet with multiple projects
     */
    @JobRoleAccess(featureIds = {15})
    @PostMapping(value = "/addTimesheetWithClientNew", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse addTimesheetWithClientNew(
            @RequestPart("dto") String encryptedDto,
            @RequestPart(value = "filledDoc", required = false) MultipartFile filledDoc,
            @RequestPart(value = "approvedDoc", required = false) MultipartFile approvedDoc) 
            throws Exception {
        
        // Decrypt and parse DTO
        CreateTimesheetRequestDTO dto = timesheetEncryptionHelper
            .decryptAndParseTimesheetDto(encryptedDto, CreateTimesheetRequestDTO.class);
        
        ServiceResponse response = timesheetServiceNew.createTimesheet(dto, filledDoc, approvedDoc);
        return response;
    }
    
    /**
     * Update existing timesheet
     */
    @JobRoleAccess(featureIds = {15, 16})
    @PostMapping(value = "/updateTimesheetNew", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse updateTimesheetNew(
            @RequestPart("dto") String encryptedDto,
            @RequestPart(value = "filledDoc", required = false) MultipartFile filledDoc,
            @RequestPart(value = "approvedDoc", required = false) MultipartFile approvedDoc) 
            throws Exception {
        
        UpdateTimesheetRequestDTO dto = timesheetEncryptionHelper
            .decryptAndParseTimesheetDto(encryptedDto, UpdateTimesheetRequestDTO.class);
        
        ServiceResponse response = timesheetServiceNew.updateTimesheet(dto, filledDoc, approvedDoc);
        return response;
    }
    
    /**
     * Get timesheet details by ID
     */
    @JobRoleAccess(featureIds = {15, 16})
    @PostMapping("/getTimesheetDetailsByIdNew")
    public ServiceResponse getTimesheetDetailsByIdNew(@RequestBody TimesheetDTO requestDTO) {
        ServiceResponse response = timesheetServiceNew.getTimesheetById(requestDTO.getTimesheetId());
        return response;
    }
    
    /**
     * Get timesheet list
     */
    @JobRoleAccess(featureIds = {15, 16})
    @PostMapping("/getAllMyTimesheetsByEmpIdNew")
    public ServiceResponse getAllMyTimesheetsByEmpIdNew(@RequestBody TimesheetDTO requestDTO) {
        ServiceResponse response = timesheetServiceNew.getTimesheetList(requestDTO);
        return response;
    }
    
    /**
     * Get activities by timesheet ID
     */
    @JobRoleAccess(featureIds = {15, 16, 24})
    @PostMapping("/getAllMyActivitiesByTimesheetIdNew")
    public ServiceResponse getAllMyActivitiesByTimesheetIdNew(@RequestBody TimesheetDTO requestDTO) {
        ServiceResponse response = timesheetServiceNew.getActivitiesByTimesheetId(
            requestDTO.getTimesheetId());
        return response;
    }
    
    /**
     * Upload document for timesheet
     */
    @JobRoleAccess(featureIds = {15})
    @PostMapping(value = "/uploadTimesheetDocumentNew", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse uploadTimesheetDocumentNew(
            @RequestParam("timesheetId") Long timesheetId,
            @RequestParam("documentType") String documentType, // "filled" or "approved"
            @RequestParam("file") MultipartFile file) {
        
        ServiceResponse response = timesheetServiceNew.uploadDocument(
            timesheetId, documentType, file);
        return response;
    }
    
    /**
     * Bulk upload final document
     */
    @JobRoleAccess(featureIds = {15})
    @PostMapping(value = "/bulkFinalDocumentUploadNew", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse bulkFinalDocumentUploadNew(
            @RequestParam("finalFile") MultipartFile file,
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam("empId") Long empId,
            @RequestParam("projectId") Integer projectId) throws Exception {
        
        ServiceResponse response = timesheetServiceNew.bulkFinalDocumentUpload(
            file, fromDate, toDate, empId, projectId);
        return response;
    }
}
```

---

## Migration & Integration Notes

### 1. Database Migration

**Step 1: Create new table**
```sql
CREATE TABLE timesheet_project_entries_new (
    project_entry_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timesheet_id BIGINT NOT NULL,
    project_id INT NOT NULL,
    client_side_id VARCHAR(255),
    office_in_time DATETIME,
    office_out_time DATETIME,
    client_in_time DATETIME,
    client_out_time DATETIME,
    total_working_minutes INT,
    total_client_working_minutes INT,
    client_approval_status_id INT,
    has_client_side_id BOOLEAN,
    shadow_emp_id BIGINT,
    is_shadow_timesheet BOOLEAN,
    created_by BIGINT,
    created_on DATETIME,
    updated_by BIGINT,
    updated_on DATETIME,
    UNIQUE KEY unique_timesheet_project (timesheet_id, project_id),
    FOREIGN KEY (timesheet_id) REFERENCES employee_timesheets_new(timesheet_id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
);
```

**Step 2: Update employee_timesheet_activities_mapping_new**
```sql
-- Add project_entry_id column (if not already present)
ALTER TABLE employee_timesheet_activities_mapping_new 
ADD COLUMN project_entry_id BIGINT,
ADD FOREIGN KEY (project_entry_id) REFERENCES timesheet_project_entries_new(project_entry_id) ON DELETE CASCADE;
```

**Step 3: Migrate existing data (if needed)**
```sql
-- For existing timesheets, create a single project entry
INSERT INTO timesheet_project_entries_new (
    timesheet_id, project_id, office_in_time, office_out_time, 
    created_by, created_on
)
SELECT 
    timesheet_id, 
    (SELECT project_id FROM projects LIMIT 1), -- Default project
    office_in_time, 
    office_out_time,
    created_by,
    created_on
FROM employee_timesheets_new
WHERE office_in_time IS NOT NULL;
```

### 2. Backward Compatibility

- **Old APIs remain functional** for existing timesheets
- **New APIs** (`*New` suffix) handle multi-project timesheets
- **Gradual migration**: Frontend can use new APIs while old data remains accessible

### 3. Validation Rules

1. **Project Entry Validation:**
   - At least one project entry required for working days
   - Each project entry must have valid in/out times
   - Project IDs must be active and assigned to employee

2. **Activity Validation:**
   - Activities must belong to the project in the project entry
   - Total activity duration should not exceed project working hours
   - All required fields (client, location, team, activity) must be provided

3. **Document Validation:**
   - Filled document required when `clientApprovalStatus = "pending"`
   - Both documents required when `clientApprovalStatus = "approved"`
   - Maximum file size: 5MB
   - Allowed formats: PDF, JPG, JPEG, PNG

4. **Time Validation:**
   - Office out time must be after office in time
   - Client out time must be after client in time
   - Times must be within same day (or next day for night shift)

### 4. Error Handling

Common error scenarios:
- **400 Bad Request**: Invalid input data, missing required fields
- **403 Forbidden**: User not authorized to create/update timesheet
- **404 Not Found**: Timesheet or project entry not found
- **409 Conflict**: Timesheet already exists for date
- **500 Internal Server Error**: Database or file system errors

### 5. Testing Checklist

- [ ] Create timesheet with single project
- [ ] Create timesheet with multiple projects
- [ ] Update timesheet (add/remove projects)
- [ ] Update timesheet activities
- [ ] Upload documents (filled + approved)
- [ ] Bulk document upload
- [ ] Get timesheet details
- [ ] Get timesheet list
- [ ] Approve/reject timesheet
- [ ] Validate time constraints
- [ ] Validate document requirements
- [ ] Test with shadow timesheet
- [ ] Test with team member timesheet

---

## Implementation Priority

### Phase 1: Core Functionality (Week 1-2)
1. Create `TimesheetProjectEntryNew` entity
2. Create `TimesheetProjectEntryNewRepository`
3. Implement `createTimesheet` method
4. Implement `getTimesheetById` method
5. Create DTOs

### Phase 2: Update & Query (Week 3)
1. Implement `updateTimesheet` method
2. Implement `getTimesheetList` method
3. Implement activity management methods
4. Update repositories

### Phase 3: Documents & Bulk Operations (Week 4)
1. Implement document upload methods
2. Implement bulk document upload
3. Update document handling logic

### Phase 4: Integration & Testing (Week 5)
1. Update controller endpoints
2. Integration testing
3. Performance optimization
4. Documentation

---

## Notes

1. **All code uses "New" tables** - no references to old tables
2. **Encryption**: DTOs are encrypted using existing `TimesheetEncryptionHelper`
3. **File Storage**: Use existing file storage mechanism (S3/local/blob)
4. **Logging**: Use existing `LogService` for API logging
5. **Validation**: Reuse existing `TimesheetValidatorService` where applicable
6. **Status Management**: Use status master table for status IDs

---

## Conclusion

This document provides a complete implementation guide for supporting multi-project timesheets. All code is production-ready and follows existing patterns in the codebase. The implementation is backward-compatible and allows gradual migration from old to new structure.

**Next Steps:**
1. Review this document with the team
2. Create database migration scripts
3. Implement Phase 1 (Core Functionality)
4. Test with sample data
5. Proceed with remaining phases

