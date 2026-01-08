package com.apmosys.employeeportal.service.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.model.EmployeeTimesheetActivitiesMappingNew;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusId;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.model.TimesheetActivityMapId;

/**
 * Mapper class for converting between DTOs and Entities.
 * Handles bidirectional mapping for timesheet hierarchy.
 * 
 * @author System
 * @version 1.0
 */
@Component
public class TimesheetMapper {

    /**
     * Convert EmployeeTimesheetDTO to EmployeeTimesheetsNew entity.
     * 
     * @param dto EmployeeTimesheetDTO
     * @return EmployeeTimesheetsNew entity
     */
    public EmployeeTimesheetsNew toEntity(EmployeeTimesheetDTO dto) {
        if (dto == null) {
            return null;
        }

        EmployeeTimesheetsNew entity = new EmployeeTimesheetsNew();
        entity.setTimesheetId(dto.getTimesheetId());
        entity.setEmpId(dto.getEmpId());
        entity.setDate(dto.getDate());
        entity.setDayTypeId(dto.getDayTypeId());
        entity.setLeaveTypeMasterId(dto.getLeaveTypeId());
        entity.setStatus(dto.getStatus());
        entity.setTotalWorkingMinutes(dto.getTotalWorkingMinutes());
        entity.setTotalActivitiesMinutes(dto.getTotalActivitiesMinutes());
        entity.setWorkCheckIn(dto.getWorkCheckIn());
        entity.setWorkCheckOut(dto.getWorkCheckOut());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedOn(dto.getCreatedOn() != null ? dto.getCreatedOn() : LocalDateTime.now());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedOn(dto.getUpdatedOn() != null ? dto.getUpdatedOn() : LocalDateTime.now());

        return entity;
    }

    /**
     * Convert EmployeeTimesheetsNew entity to EmployeeTimesheetDTO.
     * 
     * @param entity EmployeeTimesheetsNew
     * @return EmployeeTimesheetDTO
     */
    public EmployeeTimesheetDTO toDTO(EmployeeTimesheetsNew entity) {
        if (entity == null) {
            return null;
        }

        EmployeeTimesheetDTO dto = new EmployeeTimesheetDTO();
        dto.setTimesheetId(entity.getTimesheetId());
        dto.setEmpId(entity.getEmpId());
        dto.setDate(entity.getDate());
        dto.setDayTypeId(entity.getDayTypeId());
        dto.setLeaveTypeId(entity.getLeaveTypeMasterId());
        dto.setStatus(entity.getStatus());
        dto.setTotalWorkingMinutes(entity.getTotalWorkingMinutes());
        dto.setTotalActivitiesMinutes(entity.getTotalActivitiesMinutes());
        dto.setWorkCheckIn(entity.getWorkCheckIn());
        dto.setWorkCheckOut(entity.getWorkCheckOut());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());

        return dto;
    }

    /**
     * Convert ProjectTimesheetDTO to ProjectTimesheetStatusNew entity.
     * 
     * @param dto ProjectTimesheetDTO
     * @param timesheetId Parent timesheet ID
     * @return ProjectTimesheetStatusNew entity
     */
    public ProjectTimesheetStatusNew toEntity(ProjectTimesheetDTO dto, Long timesheetId) {
        if (dto == null) {
            return null;
        }

        ProjectTimesheetStatusNew entity = new ProjectTimesheetStatusNew();
        
        // Set composite key
        ProjectTimesheetStatusId id = new ProjectTimesheetStatusId();
        id.setTimesheetId(timesheetId != null ? timesheetId : dto.getTimesheetId());
        id.setProjectId(dto.getProjectId());
        entity.setId(id);

        entity.setPoNo(dto.getPoNo());
        entity.setIsNightShift(dto.getIsNightShift());
        entity.setClientApprovalStatus(dto.getClientApprovalStatus());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1); // Default to PENDING
        entity.setShadowEmpId(dto.getShadowEmpId());
        entity.setTotalClientWorkingMinutes(dto.getTotalClientWorkingMinutes());

        return entity;
    }

    /**
     * Convert ProjectTimesheetStatusNew entity to ProjectTimesheetDTO.
     * 
     * @param entity ProjectTimesheetStatusNew
     * @return ProjectTimesheetDTO
     */
    public ProjectTimesheetDTO toDTO(ProjectTimesheetStatusNew entity) {
        if (entity == null || entity.getId() == null) {
            return null;
        }

        ProjectTimesheetDTO dto = new ProjectTimesheetDTO();
        dto.setTimesheetId(entity.getId().getTimesheetId());
        dto.setProjectId(entity.getId().getProjectId());
        dto.setPoNo(entity.getPoNo());
        dto.setIsNightShift(entity.getIsNightShift());
        dto.setClientApprovalStatus(entity.getClientApprovalStatus());
        dto.setStatus(entity.getStatus());
        dto.setShadowEmpId(entity.getShadowEmpId());
        dto.setTotalClientWorkingMinutes(entity.getTotalClientWorkingMinutes());
        dto.setActivities(new ArrayList<>()); // Activities will be populated separately

        return dto;
    }

    /**
     * Convert ActivityTimesheetDTO to EmployeeTimesheetActivitiesMappingNew entity.
     * 
     * @param dto ActivityTimesheetDTO
     * @param timesheetId Parent timesheet ID
     * @param projectId Parent project ID
     * @return EmployeeTimesheetActivitiesMappingNew entity
     */
    public EmployeeTimesheetActivitiesMappingNew toEntity(ActivityTimesheetDTO dto, Long timesheetId, Integer projectId) {
        if (dto == null) {
            return null;
        }

        EmployeeTimesheetActivitiesMappingNew entity = new EmployeeTimesheetActivitiesMappingNew();
        
        // Set composite key
        TimesheetActivityMapId id = new TimesheetActivityMapId();
        id.setTimesheetId(timesheetId != null ? timesheetId : dto.getTimesheetId());
        id.setActivityId(dto.getActivityId());
        id.setProjectId(projectId != null ? projectId : dto.getProjectId());
        entity.setId(id);

        entity.setDescription(dto.getDescription());
        entity.setDurationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes().shortValue() : null);

        return entity;
    }

    /**
     * Convert EmployeeTimesheetActivitiesMappingNew entity to ActivityTimesheetDTO.
     * 
     * @param entity EmployeeTimesheetActivitiesMappingNew
     * @return ActivityTimesheetDTO
     */
    public ActivityTimesheetDTO toDTO(EmployeeTimesheetActivitiesMappingNew entity) {
        if (entity == null || entity.getId() == null) {
            return null;
        }

        ActivityTimesheetDTO dto = new ActivityTimesheetDTO();
        dto.setTimesheetId(entity.getId().getTimesheetId());
        dto.setActivityId(entity.getId().getActivityId());
        dto.setProjectId(entity.getId().getProjectId());
        dto.setDescription(entity.getDescription());
        dto.setDurationMinutes(entity.getDurationMinutes() != null ? entity.getDurationMinutes().intValue() : null);
      
        return dto;
    }
    /**
     * Convert list of activity entities to DTOs.
     * 
     * @param activities List of activity entities
     * @return List of activity DTOs
     */
    public List<ActivityTimesheetDTO> toActivityDTOList(List<EmployeeTimesheetActivitiesMappingNew> activities) {
        if (activities == null) {
            return new ArrayList<>();
        }

        return activities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of project entities to DTOs.
     * 
     * @param projects List of project entities
     * @return List of project DTOs
     */
    public List<ProjectTimesheetDTO> toProjectDTOList(List<ProjectTimesheetStatusNew> projects) {
        if (projects == null) {
            return new ArrayList<>();
        }

        return projects.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Group activities by project ID.
     * 
     * @param activities List of all activities
     * @return Map of projectId -> List of activities
     */
    public Map<Integer, List<EmployeeTimesheetActivitiesMappingNew>> groupActivitiesByProject(
            List<EmployeeTimesheetActivitiesMappingNew> activities) {
        if (activities == null) {
            return new HashMap<>();
        }

        return activities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getId().getProjectId()
                ));
    }
}

