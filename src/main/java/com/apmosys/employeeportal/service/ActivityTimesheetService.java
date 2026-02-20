package com.apmosys.employeeportal.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.model.EmployeeTimesheetActivitiesMappingNew;
import com.apmosys.employeeportal.repository.TimesheetActivityMapNewRepository;
import com.apmosys.employeeportal.service.mapper.TimesheetMapper;

/**
 * Service for ActivityTimesheet CRUD operations.
 * Handles activity-level timesheet data (nested under projects).
 * 
 * @author System
 * @version 1.0
 */
@Service
public class ActivityTimesheetService {

    @Autowired
    private TimesheetActivityMapNewRepository timesheetActivityMapNewRepository;

    @Autowired
    private TimesheetMapper timesheetMapper;
    
  
    /**
     * Create a new ActivityTimesheet.
     * 
     * @param timesheetId Parent timesheet ID
     * @param projectId Parent project ID
     * @param dto ActivityTimesheetDTO
     * @return Created EmployeeTimesheetActivitiesMappingNew entity
     */
    @Transactional
    public EmployeeTimesheetActivitiesMappingNew create(Long timesheetId, Integer projectId, ActivityTimesheetDTO dto, Long locationMappingId) {
        if (timesheetId == null) {
            throw new IllegalArgumentException("Timesheet ID is required");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID is required");
        }
        if (dto == null) {
            throw new IllegalArgumentException("ActivityTimesheetDTO cannot be null");
        }
        if (dto.getActivityId() == null) {
            throw new IllegalArgumentException("Activity ID is required");
        }

        if(locationMappingId == null){
            throw new IllegalArgumentException("Location Mapping ID is required");
        }

        EmployeeTimesheetActivitiesMappingNew entity = timesheetMapper.toEntity(dto, timesheetId, projectId, locationMappingId);
        return timesheetActivityMapNewRepository.save(entity);
    }

    /**
     * Create multiple ActivityTimesheets.
     * 
     * @param timesheetId Parent timesheet ID
     * @param projectId Parent project ID
     * @param dtos List of ActivityTimesheetDTO
     * @return List of created entities
     */
    @Transactional
    public List<EmployeeTimesheetActivitiesMappingNew> createAll(Long timesheetId, Integer projectId, 
                                                                  List<ActivityTimesheetDTO> dtos, Long locationMappingId) {
        if (timesheetId == null || projectId == null) {
            throw new IllegalArgumentException("Timesheet ID and Project ID are required");
        }
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        List<EmployeeTimesheetActivitiesMappingNew> entities = dtos.stream()
                .map(dto -> timesheetMapper.toEntity(dto, timesheetId, projectId, locationMappingId))
                .collect(Collectors.toList());

        return timesheetActivityMapNewRepository.saveAll(entities);
    }

    /**
     * Find all Activities for a timesheet.
     * 
     * @param timesheetId Timesheet ID
     * @return List of ActivityTimesheetDTO
     */
    public List<ActivityTimesheetDTO> findByTimesheetId(Long timesheetId) {
        if (timesheetId == null) {
            return List.of();
        }

        List<EmployeeTimesheetActivitiesMappingNew> entities = timesheetActivityMapNewRepository
                .findByIdTimesheetId(timesheetId);
        return timesheetMapper.toActivityDTOList(entities);
    }

    /**
     * Find all Activities for a timesheet and project.
     * 
     * @param timesheetId Timesheet ID
     * @param projectId Project ID
     * @return List of ActivityTimesheetDTO
     */
    public List<ActivityTimesheetDTO> findByTimesheetIdAndProjectId(Long timesheetId, Integer projectId) {
        if (timesheetId == null || projectId == null) {
            return List.of();
        }

        List<EmployeeTimesheetActivitiesMappingNew> entities = timesheetActivityMapNewRepository
                .findByIdTimesheetId(timesheetId);

        return entities.stream()
                .filter(activity -> activity.getProjectId().equals(projectId))
                .map(timesheetMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find all activities for a timesheet, location and project (for edit response so each location gets only its activities).
     *
     * @param timesheetId       Timesheet ID
     * @param locationMappingId Location mapping ID
     * @param projectId         Project ID
     * @return List of ActivityTimesheetDTO
     */
    public List<ActivityTimesheetDTO> findByTimesheetIdAndLocationMappingIdAndProjectId(Long timesheetId,
                                                                                         Long locationMappingId,
                                                                                         Integer projectId) {
        if (timesheetId == null || locationMappingId == null || projectId == null) {
            return List.of();
        }

        List<EmployeeTimesheetActivitiesMappingNew> entities = timesheetActivityMapNewRepository
                .findByTimesheetIdAndLocationMappingIdAndProjectId(timesheetId, locationMappingId, projectId);

        return entities.stream()
                .map(timesheetMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find Activity by composite key.
     * 
     * @param timesheetId Timesheet ID
     * @param activityId Activity ID
     * @param projectId Project ID
     * @return ActivityTimesheetDTO or null if not found
     */
    public ActivityTimesheetDTO findByCompositeKey(Long id) {
         Optional<EmployeeTimesheetActivitiesMappingNew> entity = timesheetActivityMapNewRepository.findById(id);
        return entity.map(timesheetMapper::toDTO).orElse(null);
    }

    /**
     * Update ActivityTimesheet.
     * 
     * @param dto ActivityTimesheetDTO with updated data
     * @return Updated EmployeeTimesheetActivitiesMappingNew entity
     */
    @Transactional
    public EmployeeTimesheetActivitiesMappingNew update(ActivityTimesheetDTO dto) {
        if (dto == null || dto.getTimesheetId() == null || dto.getActivityId() == null || dto.getProjectId() == null) {
            throw new IllegalArgumentException("ActivityTimesheetDTO and all IDs are required");
        }

       EmployeeTimesheetActivitiesMappingNew entity = timesheetActivityMapNewRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ActivityTimesheet not found: timesheetId=" + dto.getTimesheetId() +
                        ", activityId=" + dto.getActivityId() + ", projectId=" + dto.getProjectId()));
       
       entity.setTimesheetId(dto.getTimesheetId());
       entity.setActivityId(dto.getActivityId());
       entity.setProjectId(dto.getProjectId());
       entity.setDescription(dto.getDescription());
       entity.setDurationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes().shortValue() : null);

        return timesheetActivityMapNewRepository.save(entity);
    }

    /**
     * Delete ActivityTimesheet by composite key.
     * 
     * @param timesheetId Timesheet ID
     * @param activityId Activity ID
     * @param projectId Project ID
     */
    @Transactional
    public void delete(Long id,Long timesheetId, Long activityId, Integer projectId) {
        if (timesheetId == null || activityId == null || projectId == null) {
            throw new IllegalArgumentException("Timesheet ID, Activity ID, and Project ID are required");
        }
        if (!timesheetActivityMapNewRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "ActivityTimesheet not found: timesheetId=" + timesheetId +
                    ", activityId=" + activityId + ", projectId=" + projectId);
        }

        timesheetActivityMapNewRepository.deleteById(id);
    }

    /**
     * Delete all Activities for a timesheet.
     * 
     * @param timesheetId Timesheet ID
     */
    @Transactional
    public void deleteByTimesheetId(Long timesheetId) {
        if (timesheetId == null) {
            throw new IllegalArgumentException("Timesheet ID is required");
        }

        List<EmployeeTimesheetActivitiesMappingNew> activities = timesheetActivityMapNewRepository
                .findByTimesheetId(timesheetId);
        timesheetActivityMapNewRepository.deleteAll(activities);
    }

    /**
     * Delete all Activities for a timesheet and project.
     * 
     * @param timesheetId Timesheet ID
     * @param projectId Project ID
     */
    @Transactional
    public void deleteByTimesheetIdAndProjectId(Long timesheetId, Integer projectId) {
        if (timesheetId == null || projectId == null) {
            throw new IllegalArgumentException("Timesheet ID and Project ID are required");
        }

        List<EmployeeTimesheetActivitiesMappingNew> activities = timesheetActivityMapNewRepository
                .findByTimesheetId(timesheetId);

        List<EmployeeTimesheetActivitiesMappingNew> toDelete = activities.stream()
                .filter(activity -> activity.getProjectId().equals(projectId))
                .collect(Collectors.toList());

        timesheetActivityMapNewRepository.deleteAll(toDelete);
    }

    /**
     * Calculate total duration for a list of activities.
     * 
     * @param activities List of ActivityTimesheetDTO
     * @return Total duration in minutes
     */
    public Integer calculateActivityTotals(List<ActivityTimesheetDTO> activities) {
        if (activities == null || activities.isEmpty()) {
            return 0;
        }

        return activities.stream()
                .mapToInt(activity -> activity.getDurationMinutes() != null ? activity.getDurationMinutes() : 0)
                .sum();
    }

    /**
     * Check if ActivityTimesheet exists.
     * 
     * @param timesheetId Timesheet ID
     * @param activityId Activity ID
     * @param projectId Project ID
     * @return true if exists, false otherwise
     */
    public boolean exists(Long id,Long timesheetId, Long activityId, Integer projectId) {
        if (timesheetId == null || activityId == null || projectId == null) {
            return false;
        }
        return timesheetActivityMapNewRepository.existsById(id);
    }
    
    
    @Transactional
    public void deleteActivitiesForProject(
            Long timesheetId,
            Long locationMappingId,
            Integer projectId) {

        timesheetActivityMapNewRepository
            .deleteByTimesheetIdAndLocationMappingIdAndProjectId(
                timesheetId, locationMappingId, projectId);
    }
    
    
    
    
    @Transactional
    public void deleteByTimesheetIdAndLocationMappingIdAndProjectId(
            Long timesheetId,
            Long locationMappingId,
            Integer projectId) {

        if (timesheetId == null || locationMappingId == null || projectId == null) {
            return;
        }

        timesheetActivityMapNewRepository
                .deleteByTimesheetIdAndLocationMappingIdAndProjectId(
                        timesheetId, locationMappingId, projectId);
    }
    
    
    
    @Transactional
    public void createAll(
            Long timesheetId,
            Long locationMappingId,
            Integer projectId,
            List<ActivityTimesheetDTO> activities) {

        if (activities == null || activities.isEmpty()) {
            return;
        }

        List<EmployeeTimesheetActivitiesMappingNew> entities =
                activities.stream()
                        .map(a -> {
                            EmployeeTimesheetActivitiesMappingNew e =
                                    timesheetMapper.toEntity(a,timesheetId,projectId, locationMappingId);
                            return e;
                        })
                        .collect(Collectors.toList());

        timesheetActivityMapNewRepository.saveAll(entities);
    }





}

