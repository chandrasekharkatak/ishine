package com.apmosys.employeeportal.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.ProjectTimesheetStatusNewRepository;
import com.apmosys.employeeportal.service.helper.TimesheetAggregationHelper;
import com.apmosys.employeeportal.service.mapper.TimesheetMapper;

/**
 * Service for ProjectTimesheet CRUD operations.
 * Handles project-level timesheet data (multiple per day).
 * 
 * @author System
 * @version 1.0
 */
@Service
public class ProjectTimesheetService {

    @Autowired
    private ProjectTimesheetStatusNewRepository projectTimesheetStatusNewRepository;

    @Autowired
    private TimesheetMapper timesheetMapper;

    @Autowired
    private TimesheetAggregationHelper aggregationHelper;
    
    @Autowired
    ActivitiesRepository activitiesRepository;

    /**
     * Create a new ProjectTimesheet.
     * 
     * @param timesheetId Parent timesheet ID
     * @param dto ProjectTimesheetDTO
     * @return Created ProjectTimesheetStatusNew entity
     */
    @Transactional
    public ProjectTimesheetStatusNew create(Long timesheetId, ProjectTimesheetDTO dto, Long createdBy) {
        if (timesheetId == null) {
            throw new IllegalArgumentException("Timesheet ID is required");
        }
        if (dto == null) {
            throw new IllegalArgumentException("ProjectTimesheetDTO cannot be null");
        }
        if (dto.getProjectId() == null) {
            throw new IllegalArgumentException("Project is required");
        }

        // Calculate project totals
        aggregationHelper.calculateAndSetProjectTimesheetTotals(dto);

        // Set default status if not provided
        if (dto.getStatus() == null) {
            dto.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
        }

        ProjectTimesheetStatusNew entity = timesheetMapper.toEntity(dto, timesheetId);
		String description = "";

        // Non-fillable day types have no activities; use DTO description if provided
        if (dto.getActivities() == null || dto.getActivities().isEmpty()) {
			description = (dto.getDescription() != null && !dto.getDescription().trim().isEmpty())
					? dto.getDescription() : "No activity available in project timesheet";
		} else {
			//activityId3
			for (ActivityTimesheetDTO activity : dto.getActivities()) {
				Activity activityMaster=activitiesRepository.findById(activity.getActivityId()).orElseThrow(() -> new IllegalArgumentException("Activity not found"));
				description += activityMaster.getActivity()+ "<br>";
			}
		}
        entity.setDescription(description);
        entity.setCreatedBy(createdBy);
        
        
        
        return projectTimesheetStatusNewRepository.save(entity);
    }

    /**
     * Find all ProjectTimesheets for a timesheet.
     * 
     * @param timesheetId Timesheet ID
     * @return List of ProjectTimesheetDTO
     */
    public List<ProjectTimesheetDTO> findByTimesheetId(Long timesheetId) {
        if (timesheetId == null) {
            return List.of();
        }

        List<ProjectTimesheetStatusNew> entities = projectTimesheetStatusNewRepository
                .findByTimesheetId(timesheetId);
        return entities.stream()
                .map(timesheetMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find ProjectTimesheet by timesheet ID and project ID.
     * 
     * @param timesheetId Timesheet ID
     * @param projectId Project ID
     * @return ProjectTimesheetDTO or null if not found
     */
    public ProjectTimesheetDTO findByTimesheetIdAndProjectId(Long timesheetId, Integer projectId) {
        if (timesheetId == null || projectId == null) {
            return null;
        }

        Optional<ProjectTimesheetStatusNew> entity = projectTimesheetStatusNewRepository
                .findByTimesheetIdAndProjectId(timesheetId, projectId);
        return entity.map(timesheetMapper::toDTO).orElse(null);
    }

    /**
     * Update ProjectTimesheet.
     * 
     * @param dto ProjectTimesheetDTO with updated data
     * @return Updated ProjectTimesheetStatusNew entity
     */
    @Transactional
    public ProjectTimesheetStatusNew update(ProjectTimesheetDTO dto) {
        if (dto == null || dto.getTimesheetId() == null || dto.getProjectId() == null) {
            throw new IllegalArgumentException("ProjectTimesheetDTO, timesheetId, and projectId are required");
        }

        ProjectTimesheetStatusNew entity = projectTimesheetStatusNewRepository
                .findByTimesheetIdAndProjectId(dto.getTimesheetId(), dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ProjectTimesheet not found: timesheetId=" + dto.getTimesheetId() + ", projectId=" + dto.getProjectId()));

        // Calculate project totals
        aggregationHelper.calculateAndSetProjectTimesheetTotals(dto);
        entity.setStatus(TimesheetAggregationHelper.STATUS_PENDING);      // Update fields
        entity.setPoNo(dto.getPoNo());
        entity.setClientApprovalStatus(dto.getClientApprovalStatus());
        entity.setShadowEmpId(dto.getShadowEmpId());
        entity.setTotalClientWorkingMinutes(dto.getTotalClientWorkingMinutes());
        
		String description = "";
        // Non-fillable: use DTO description when activities are empty
        if (dto.getActivities() == null || dto.getActivities().isEmpty()) {
			description = (dto.getDescription() != null && !dto.getDescription().trim().isEmpty())
					? dto.getDescription() : "No activity available in project timesheet";
		} else {
			//activityId
			for (ActivityTimesheetDTO activity : dto.getActivities()) {
				Activity activityMaster=activitiesRepository.findById(activity.getActivityId()).get();
				description += activityMaster.getActivity() + "<br>";
			}
		}
        entity.setDescription(description);
        
        // Update locationMappingId if provided (NEW CONTRACT: Projects are linked to locations)
        if (dto.getLocationMappingId() != null) {
            entity.getId().setLocationMappingId(dto.getLocationMappingId());
        }
        
        return projectTimesheetStatusNewRepository.save(entity);
    }

    /**
     * Delete ProjectTimesheet by timesheet ID and project ID.
     * Note: This will cascade delete related Activities.
     * 
     * @param timesheetId Timesheet ID
     * @param projectId Project ID
     */
    @Transactional
    public void delete(Long timesheetId, Integer projectId) {
        if (timesheetId == null || projectId == null) {
            throw new IllegalArgumentException("Timesheet ID and Project ID are required");
        }

        ProjectTimesheetStatusNew entity = projectTimesheetStatusNewRepository
                .findByTimesheetIdAndProjectId(timesheetId, projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ProjectTimesheet not found: timesheetId=" + timesheetId + ", projectId=" + projectId));

        projectTimesheetStatusNewRepository.delete(entity);
    }

    /**
     * Delete all ProjectTimesheets for a timesheet.
     * 
     * @param timesheetId Timesheet ID
     */
    @Transactional
    public void deleteByTimesheetId(Long timesheetId) {
        if (timesheetId == null) {
            throw new IllegalArgumentException("Timesheet ID is required");
        }

        projectTimesheetStatusNewRepository.deleteByTimesheetId(timesheetId);
    }

    /**
     * Calculate and update totals for a project.
     * 
     * @param dto ProjectTimesheetDTO
     * @return Updated ProjectTimesheetDTO with calculated totals
     */
    public ProjectTimesheetDTO calculateProjectTotals(ProjectTimesheetDTO dto) {
        if (dto == null) {
            return null;
        }

        aggregationHelper.calculateAndSetProjectTimesheetTotals(dto);
        return dto;
    }

    /**
     * Check if ProjectTimesheet exists.
     * 
     * @param timesheetId Timesheet ID
     * @param projectId Project ID
     * @return true if exists, false otherwise
     */
    public boolean exists(Long timesheetId, Integer projectId) {
        if (timesheetId == null || projectId == null) {
            return false;
        }

        return projectTimesheetStatusNewRepository
                .findByTimesheetIdAndProjectId(timesheetId, projectId)
                .isPresent();
    }
    
    public Boolean existsApprovedProjectByLocationMappingId(Long locationMappingId) {
        return projectTimesheetStatusNewRepository
                .existsByIdLocationMappingIdAndStatus(
                        locationMappingId,
                        TimesheetAggregationHelper.STATUS_APPROVED
                );
    }
    
    
    public List<ProjectTimesheetDTO> findByLocationMappingId(Long locationMappingId) {

        if (locationMappingId == null) {
            return List.of();
        }

        return projectTimesheetStatusNewRepository
                .findByIdLocationMappingId(locationMappingId)
                .stream()
                .map(timesheetMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteByTimesheetIdAndLocationMappingIdAndProjectId(
            Long timesheetId,
            Long locationMappingId,
            Integer projectId) {

        if (timesheetId == null || locationMappingId == null || projectId == null) {
            return;
        }

        projectTimesheetStatusNewRepository
                .deleteByTimesheetIdAndLocationMappingIdAndProjectId(
                        timesheetId, locationMappingId, projectId);
    }
    
    
    public List<ProjectTimesheetDTO> findApprovedProjectsByTimesheetId(
            Long timesheetId) {

        return projectTimesheetStatusNewRepository
                .findByTimesheetIdAndStatus(
                        timesheetId,
                        TimesheetAggregationHelper.STATUS_APPROVED)
                .stream()
                .map(timesheetMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    
    public List<ProjectTimesheetDTO> findByTimesheetIdAndLocationMappingId(
            Long timesheetId,
            Long locationMappingId) {

        if (timesheetId == null || locationMappingId == null) {
            return List.of();
        }

        return projectTimesheetStatusNewRepository
                .findByTimesheetIdAndLocationMappingId(
                        timesheetId, locationMappingId)
                .stream()
                .map(timesheetMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public boolean existsApprovedProject(Long timesheetId) {

        List<ProjectTimesheetDTO> projects =
                findByTimesheetId(timesheetId);

        for (ProjectTimesheetDTO project : projects) {

            if (TimesheetAggregationHelper.STATUS_APPROVED
                    .equals(project.getStatus())) {
                    return true;
                
            }
        }
        return false;
    }

    
    public boolean isProjectApproved(
            Long timesheetId,
            Long locationMappingId,
            Integer projectId) {

        return projectTimesheetStatusNewRepository
                .existsApprovedProject(
                    timesheetId,
                    locationMappingId,
                    projectId,
                    TimesheetAggregationHelper.STATUS_APPROVED
                );
    }


}

