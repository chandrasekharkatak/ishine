# Timesheet Backend Service Layer Changes Documentation

## Executive Summary

This document outlines the required changes to support the new multi-project timesheet functionality where:
1. Users can fill timesheets for **multiple projects** in a single day
2. Each project has **different in-time and out-time**
3. Each project has **different sets of activities**

## Current Architecture Analysis

### Existing New Tables

#### 1. `employee_timesheets_new`
**Entity**: `EmployeeTimesheetsNew`
**Repository**: `EmployeeTimesheetsNewRepository`
**Current Structure**:
```java
- timesheetId (PK)
- createdBy, createdOn, updatedBy, updatedOn
- date (LocalDate)
- dayTypeId (Integer) - FK to day_type_master_new
- empId (Long)
- status (Integer) - FK to status_master_new
- totalWorkingMinutes (Integer)
- officeInTime (LocalDateTime) - SINGLE in-time for entire timesheet
- officeOutTime (LocalDateTime) - SINGLE out-time for entire timesheet
- leaveTypeMasterId (Long)
```

**Limitation**: Currently stores only ONE set of in/out times per timesheet, not per project.

#### 2. `employee_timesheet_activities_mapping_new`
**Entity**: `EmployeeTimesheetActivitiesMappingNew`
**Repository**: `TimesheetActivityMapNewRepository`
**Composite Key**: `TimesheetActivityMapId` (timesheetId, activityId, projectId)
**Current Structure**:
```java
- id (TimesheetActivityMapId - composite key)
  - timesheetId (Long)
  - activityId (Long)
  - projectId (Long)
- description (String)
- durationMinutes (Short)
```

**Limitation**: Activities are linked to projects, but there's no project-specific time entry.

---

## Required Database Schema Changes

### New Table: `timesheet_project_entries_new`

**Purpose**: Store project-specific time entries (in-time, out-time) for each project in a timesheet.

**Entity**: `TimesheetProjectEntryNew`

```java
package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "timesheet_project_entries_new")
public class TimesheetProjectEntryNew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_entry_id")
    private Long projectEntryId;

    @Column(name = "timesheet_id", nullable = false)
    private Long timesheetId; // FK to employee_timesheets_new

    @Column(name = "project_id", nullable = false)
    private Integer projectId; // FK to projects

    @Column(name = "client_side_id")
    private String clientSideId; // Project-specific client side ID

    @Column(name = "office_in_time")
    private LocalDateTime officeInTime; // Project-specific ApMoSys in-time

    @Column(name = "office_out_time")
    private LocalDateTime officeOutTime; // Project-specific ApMoSys out-time

    @Column(name = "client_in_time")
    private LocalDateTime clientInTime; // Project-specific client in-time

    @Column(name = "client_out_time")
    private LocalDateTime clientOutTime; // Project-specific client out-time

    @Column(name = "total_working_minutes")
    private Integer totalWorkingMinutes; // Calculated from in/out times

    @Column(name = "total_client_working_minutes")
    private Integer totalClientWorkingMinutes; // Calculated from client in/out times

    @Column(name = "client_approval_status")
    private String clientApprovalStatus; // 'no', 'pending', 'approved'

    @Column(name = "shadow_emp_id")
    private Long shadowEmpId; // For shadow timesheet entries

    @Column(name = "is_shadow_timesheet")
    private Boolean isShadowTimesheet;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}
```

**Composite Unique Constraint**: `(timesheet_id, project_id)` - One entry per project per timesheet.

---

### Updated Table: `employee_timesheet_activities_mapping_new`

**Change Required**: Add `project_entry_id` to link activities to specific project entries.

**Updated Entity**:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_timesheet_activities_mapping_new")
public class EmployeeTimesheetActivitiesMappingNew {

    @EmbeddedId
    private TimesheetActivityMapId id;

    @Column(name = "project_entry_id", nullable = false)
    private Long projectEntryId; // NEW: FK to timesheet_project_entries_new

    @Column(name = "description")
    private String description;

    @Column(name = "duration_minutes")
    private Short durationMinutes;
}
```

**Updated Composite Key**: `TimesheetActivityMapId` remains the same (timesheetId, activityId, projectId), but activities are now also linked to project entries.

---

## Repository Changes

### New Repository: `TimesheetProjectEntryNewRepository`

```java
package com.apmosys.employeeportal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.apmosys.employeeportal.model.TimesheetProjectEntryNew;

public interface TimesheetProjectEntryNewRepository 
    extends JpaRepository<TimesheetProjectEntryNew, Long> {

    // Find all project entries for a timesheet
    List<TimesheetProjectEntryNew> findByTimesheetId(Long timesheetId);

    // Find project entry by timesheet and project
    TimesheetProjectEntryNew findByTimesheetIdAndProjectId(Long timesheetId, Integer projectId);

    // Delete all entries for a timesheet
    void deleteByTimesheetId(Long timesheetId);

    // Find entries by employee and date range
    @Query("SELECT tpe FROM TimesheetProjectEntryNew tpe " +
           "JOIN EmployeeTimesheetsNew et ON tpe.timesheetId = et.timesheetId " +
           "WHERE et.empId = :empId AND et.date BETWEEN :startDate AND :endDate")
    List<TimesheetProjectEntryNew> findByEmpIdAndDateRange(
        @Param("empId") Long empId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}
```

### Updated Repository: `TimesheetActivityMapNewRepository`

```java
public interface TimesheetActivityMapNewRepository 
    extends JpaRepository<EmployeeTimesheetActivitiesMappingNew, TimesheetActivityMapId> {

    // Find activities by project entry
    List<EmployeeTimesheetActivitiesMappingNew> findByProjectEntryId(Long projectEntryId);

    // Find activities by timesheet
    @Query("SELECT a FROM EmployeeTimesheetActivitiesMappingNew a " +
           "WHERE a.id.timesheetId = :timesheetId")
    List<EmployeeTimesheetActivitiesMappingNew> findByTimesheetId(@Param("timesheetId") Long timesheetId);

    // Delete activities by project entry
    void deleteByProjectEntryId(Long projectEntryId);

    // Delete activities by timesheet
    void deleteById_TimesheetId(Long timesheetId);
}
```

---

## DTO Changes

### New DTO: `TimesheetProjectEntryDTO`

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
    private String totalWorkingHours; // Formatted as "HH:mm"
    private Integer totalClientWorkingMinutes;
    private String totalClientWorkingHours; // Formatted as "HH:mm"
    
    private String clientApprovalStatus;
    private Long shadowEmpId;
    private Boolean isShadowTimesheet;
    
    // Activities for this project
    private List<ActivityDTO> activities;
}
```

### New DTO: `ActivityDTO`

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
public class ActivityDTO {
    
    private Long activityId;
    private String activity;
    private Integer clientId;
    private String clientName;
    private Integer clientLocationId;
    private String clientLocation;
    private Long teamId;
    private String teamName;
    private String description;
    private Short durationMinutes;
    private Float durationHours; // Calculated from minutes
}
```

### Updated DTO: `TimesheetDTO` (for new structure)

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
public class TimesheetDTO {
    
    private Long timesheetId;
    private Long empId;
    private String employeeName;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;
    
    private Integer dayTypeId;
    private String dayType;
    private Integer status;
    private String statusName;
    
    private Integer totalWorkingMinutes; // Sum of all project entries
    private String totalWorkingHours; // Formatted
    
    private Long leaveTypeMasterId;
    private String leaveType;
    
    private String description; // General description (for non-working days)
    
    // NEW: List of project entries
    private List<TimesheetProjectEntryDTO> projectEntries;
    
    // Legacy fields (for backward compatibility during migration)
    private LocalDateTime officeInTime;
    private LocalDateTime officeOutTime;
    private Integer projectId; // Primary project (if single project mode)
    
    // Metadata
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdOn;
    private Long updatedBy;
    private LocalDateTime updatedOn;
    private Long currentManagerId;
}
```

### Request DTO: `CreateTimesheetRequestDTO`

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
public class CreateTimesheetRequestDTO {
    
    private Long empId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    private Integer dayTypeId;
    private Integer status;
    private String description;
    private Long leaveTypeMasterId;
    
    // NEW: Multiple project entries
    private List<ProjectEntryRequestDTO> projectEntries;
    
    // Metadata
    private String timesheetAppliedFor; // 'self', 'asShadow', 'team'
    private Long shadowEmpId;
    private Long currentManagerId;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ProjectEntryRequestDTO {
    
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
    
    private String clientApprovalStatus;
    private Long shadowEmpId;
    private Boolean isShadowTimesheet;
    
    // Activities for this project
    private List<ActivityRequestDTO> activities;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ActivityRequestDTO {
    
    private Long activityId;
    private Integer clientId;
    private Integer clientLocationId;
    private Long teamId;
    private String description;
    private Short durationMinutes; // or Float durationHours
}
```

---

## Service Layer Changes

### New Service Methods Required

#### 1. Create Timesheet with Multiple Projects

```java
@Service
public class TimesheetServiceNew {
    
    @Autowired
    private EmployeeTimesheetsNewRepository timesheetsRepository;
    
    @Autowired
    private TimesheetProjectEntryNewRepository projectEntryRepository;
    
    @Autowired
    private TimesheetActivityMapNewRepository activityMapRepository;
    
    public ServiceResponse createTimesheetWithMultipleProjects(
            CreateTimesheetRequestDTO request, 
            Long currentUserId) {
        
        try {
            // 1. Create main timesheet record
            EmployeeTimesheetsNew timesheet = new EmployeeTimesheetsNew();
            timesheet.setEmpId(request.getEmpId());
            timesheet.setDate(request.getDate());
            timesheet.setDayTypeId(request.getDayTypeId());
            timesheet.setStatus(request.getStatus());
            timesheet.setDescription(request.getDescription());
            timesheet.setLeaveTypeMasterId(request.getLeaveTypeMasterId());
            timesheet.setCreatedBy(currentUserId);
            timesheet.setCreatedOn(LocalDateTime.now());
            
            // Calculate total working minutes from all projects
            int totalMinutes = request.getProjectEntries().stream()
                .mapToInt(pe -> calculateMinutes(pe.getOfficeInTime(), pe.getOfficeOutTime()))
                .sum();
            timesheet.setTotalWorkingMinutes(totalMinutes);
            
            timesheet = timesheetsRepository.save(timesheet);
            
            // 2. Create project entries
            List<TimesheetProjectEntryNew> projectEntries = new ArrayList<>();
            for (ProjectEntryRequestDTO entryRequest : request.getProjectEntries()) {
                TimesheetProjectEntryNew projectEntry = new TimesheetProjectEntryNew();
                projectEntry.setTimesheetId(timesheet.getTimesheetId());
                projectEntry.setProjectId(entryRequest.getProjectId());
                projectEntry.setClientSideId(entryRequest.getClientSideId());
                projectEntry.setOfficeInTime(entryRequest.getOfficeInTime());
                projectEntry.setOfficeOutTime(entryRequest.getOfficeOutTime());
                projectEntry.setClientInTime(entryRequest.getClientInTime());
                projectEntry.setClientOutTime(entryRequest.getClientOutTime());
                projectEntry.setClientApprovalStatus(entryRequest.getClientApprovalStatus());
                projectEntry.setShadowEmpId(entryRequest.getShadowEmpId());
                projectEntry.setIsShadowTimesheet(entryRequest.getIsShadowTimesheet());
                projectEntry.setCreatedBy(currentUserId);
                projectEntry.setCreatedOn(LocalDateTime.now());
                
                // Calculate project-specific working minutes
                int projectMinutes = calculateMinutes(
                    entryRequest.getOfficeInTime(), 
                    entryRequest.getOfficeOutTime());
                projectEntry.setTotalWorkingMinutes(projectMinutes);
                
                if (entryRequest.getClientInTime() != null && 
                    entryRequest.getClientOutTime() != null) {
                    int clientMinutes = calculateMinutes(
                        entryRequest.getClientInTime(), 
                        entryRequest.getClientOutTime());
                    projectEntry.setTotalClientWorkingMinutes(clientMinutes);
                }
                
                projectEntry = projectEntryRepository.save(projectEntry);
                projectEntries.add(projectEntry);
                
                // 3. Create activities for this project entry
                if (entryRequest.getActivities() != null) {
                    for (ActivityRequestDTO activityRequest : entryRequest.getActivities()) {
                        EmployeeTimesheetActivitiesMappingNew activity = 
                            new EmployeeTimesheetActivitiesMappingNew();
                        
                        TimesheetActivityMapId id = new TimesheetActivityMapId();
                        id.setTimesheetId(timesheet.getTimesheetId());
                        id.setActivityId(activityRequest.getActivityId());
                        id.setProjectId(entryRequest.getProjectId());
                        activity.setId(id);
                        
                        activity.setProjectEntryId(projectEntry.getProjectEntryId());
                        activity.setDescription(activityRequest.getDescription());
                        activity.setDurationMinutes(activityRequest.getDurationMinutes());
                        
                        activityMapRepository.save(activity);
                    }
                }
            }
            
            return ServiceResponse.success("Timesheet created successfully");
            
        } catch (Exception e) {
            return ServiceResponse.error("Failed to create timesheet: " + e.getMessage());
        }
    }
    
    private int calculateMinutes(LocalDateTime inTime, LocalDateTime outTime) {
        if (inTime == null || outTime == null) return 0;
        return (int) Duration.between(inTime, outTime).toMinutes();
    }
}
```

#### 2. Update Timesheet with Multiple Projects

```java
public ServiceResponse updateTimesheetWithMultipleProjects(
        Long timesheetId,
        CreateTimesheetRequestDTO request,
        Long currentUserId) {
    
    try {
        // 1. Get existing timesheet
        EmployeeTimesheetsNew timesheet = timesheetsRepository.findById(timesheetId)
            .orElseThrow(() -> new RuntimeException("Timesheet not found"));
        
        // 2. Update main timesheet fields
        timesheet.setDayTypeId(request.getDayTypeId());
        timesheet.setStatus(request.getStatus());
        timesheet.setDescription(request.getDescription());
        timesheet.setUpdatedBy(currentUserId);
        timesheet.setUpdatedOn(LocalDateTime.now());
        
        // 3. Delete existing project entries and activities
        List<TimesheetProjectEntryNew> existingEntries = 
            projectEntryRepository.findByTimesheetId(timesheetId);
        for (TimesheetProjectEntryNew entry : existingEntries) {
            activityMapRepository.deleteByProjectEntryId(entry.getProjectEntryId());
            projectEntryRepository.delete(entry);
        }
        
        // 4. Recreate project entries (same logic as create)
        // ... (similar to create method)
        
        return ServiceResponse.success("Timesheet updated successfully");
        
    } catch (Exception e) {
        return ServiceResponse.error("Failed to update timesheet: " + e.getMessage());
    }
}
```

#### 3. Get Timesheet with Project Entries

```java
public TimesheetDTO getTimesheetWithProjectEntries(Long timesheetId) {
    
    EmployeeTimesheetsNew timesheet = timesheetsRepository.findById(timesheetId)
        .orElseThrow(() -> new RuntimeException("Timesheet not found"));
    
    TimesheetDTO dto = new TimesheetDTO();
    dto.setTimesheetId(timesheet.getTimesheetId());
    dto.setEmpId(timesheet.getEmpId());
    dto.setDate(timesheet.getDate());
    dto.setDayTypeId(timesheet.getDayTypeId());
    dto.setStatus(timesheet.getStatus());
    dto.setTotalWorkingMinutes(timesheet.getTotalWorkingMinutes());
    
    // Get project entries
    List<TimesheetProjectEntryNew> projectEntries = 
        projectEntryRepository.findByTimesheetId(timesheetId);
    
    List<TimesheetProjectEntryDTO> projectEntryDTOs = new ArrayList<>();
    for (TimesheetProjectEntryNew entry : projectEntries) {
        TimesheetProjectEntryDTO entryDTO = new TimesheetProjectEntryDTO();
        entryDTO.setProjectEntryId(entry.getProjectEntryId());
        entryDTO.setProjectId(entry.getProjectId());
        entryDTO.setOfficeInTime(entry.getOfficeInTime());
        entryDTO.setOfficeOutTime(entry.getOfficeOutTime());
        entryDTO.setClientInTime(entry.getClientInTime());
        entryDTO.setClientOutTime(entry.getClientOutTime());
        entryDTO.setTotalWorkingMinutes(entry.getTotalWorkingMinutes());
        entryDTO.setTotalClientWorkingMinutes(entry.getTotalClientWorkingMinutes());
        
        // Get activities for this project entry
        List<EmployeeTimesheetActivitiesMappingNew> activities = 
            activityMapRepository.findByProjectEntryId(entry.getProjectEntryId());
        
        List<ActivityDTO> activityDTOs = activities.stream()
            .map(this::mapToActivityDTO)
            .collect(Collectors.toList());
        
        entryDTO.setActivities(activityDTOs);
        projectEntryDTOs.add(entryDTO);
    }
    
    dto.setProjectEntries(projectEntryDTOs);
    return dto;
}
```

---

## API Endpoint Changes

### New Endpoints Required

#### 1. Create Timesheet (Multi-Project)
```
POST /api/timesheet/create-multi-project
Request Body: CreateTimesheetRequestDTO
Response: ServiceResponse
```

#### 2. Update Timesheet (Multi-Project)
```
PUT /api/timesheet/update-multi-project/{timesheetId}
Request Body: CreateTimesheetRequestDTO
Response: ServiceResponse
```

#### 3. Get Timesheet with Project Entries
```
GET /api/timesheet/{timesheetId}/with-projects
Response: TimesheetDTO
```

#### 4. Get Timesheets by Date Range (with project entries)
```
GET /api/timesheet/employee/{empId}/range
Query Params: startDate, endDate
Response: List<TimesheetDTO>
```

---

## Migration Strategy

### Phase 1: Database Migration
1. Create `timesheet_project_entries_new` table
2. Add `project_entry_id` column to `employee_timesheet_activities_mapping_new`
3. Create indexes:
   - `idx_timesheet_project_entry_timesheet_id` on `timesheet_project_entries_new(timesheet_id)`
   - `idx_timesheet_project_entry_project_id` on `timesheet_project_entries_new(project_id)`
   - `idx_activity_map_project_entry_id` on `employee_timesheet_activities_mapping_new(project_entry_id)`

### Phase 2: Backward Compatibility
- Keep existing endpoints working
- Add new endpoints for multi-project functionality
- Support both old and new data structures during transition

### Phase 3: Data Migration (if needed)
- Migrate existing single-project timesheets to new structure
- Create project entries for existing timesheets

---

## Validation Rules

### Business Rules to Implement

1. **Project Entry Validation**:
   - Each project entry must have valid in-time and out-time
   - Out-time must be after in-time
   - Time ranges should not overlap between projects (optional business rule)
   - Total activity duration for a project should not exceed project working hours

2. **Activity Validation**:
   - Activities must belong to the project they're assigned to
   - Activity duration must be positive
   - Sum of activity durations for a project should not exceed project working hours

3. **Timesheet Validation**:
   - At least one project entry required for working days
   - Total working minutes = sum of all project entry working minutes
   - Date must be within allowed range (based on timesheet lock days)

---

## Testing Checklist

- [ ] Create timesheet with single project
- [ ] Create timesheet with multiple projects
- [ ] Create timesheet with different in/out times per project
- [ ] Create timesheet with different activities per project
- [ ] Update timesheet (add/remove projects)
- [ ] Update timesheet (modify project times)
- [ ] Update timesheet (modify activities)
- [ ] Delete timesheet (cascade delete project entries and activities)
- [ ] Get timesheet with all project entries
- [ ] Validate time overlaps (if business rule applies)
- [ ] Validate activity duration vs project working hours
- [ ] Test shadow timesheet functionality
- [ ] Test team member timesheet functionality
- [ ] Test backward compatibility with old endpoints

---

## Summary

This refactoring enables:
1. ✅ Multiple projects per timesheet
2. ✅ Project-specific in/out times
3. ✅ Project-specific activity sets
4. ✅ Maintains backward compatibility
5. ✅ Uses new table structure with "New" prefix
6. ✅ Proper data normalization and relationships

The new structure provides flexibility while maintaining data integrity through proper foreign key relationships and composite keys.

