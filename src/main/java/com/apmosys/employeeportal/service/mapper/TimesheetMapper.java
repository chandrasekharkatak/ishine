package com.apmosys.employeeportal.service.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetActivitiesDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetDocsDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetLocationsDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetProjectsDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqFlatDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.model.EmployeeTimesheetActivitiesMappingNew;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusId;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.utility.DateConversionUtil;

/**
 * Mapper class for converting between DTOs and Entities.
 * Handles bidirectional mapping for timesheet hierarchy.
 * 
 * @author System
 * @version 1.0
 */
@Component
public class TimesheetMapper {
	private final String pattern="yyyy-MM-dd HH:mm:ss";

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
        entity.setWorkCheckIn(DateConversionUtil.stringToLocalDateTime(dto.getWorkCheckIn(),pattern));
        entity.setWorkCheckOut(DateConversionUtil.stringToLocalDateTime(dto.getWorkCheckOut(),pattern));
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
        dto.setWorkCheckIn(DateConversionUtil.localDateTimeToString(entity.getWorkCheckIn(),pattern));
        dto.setWorkCheckOut(DateConversionUtil.localDateTimeToString(entity.getWorkCheckOut(),pattern));
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
        id.setLocationMappingId(dto.getLocationMappingId());
        entity.setId(id);

        entity.setPoNo(dto.getPoNo());
        entity.setClientApprovalStatus(dto.getClientApprovalStatus());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1); // Default to PENDING
        entity.setShadowEmpId(dto.getShadowEmpId());
        entity.setTotalClientWorkingMinutes(dto.getTotalClientWorkingMinutes());
        entity.setClientLocationId(dto.getClientLocationId() != null ? Integer.valueOf(dto.getClientLocationId().toString()) : null);
        entity.setClientSideId(dto.getClientSideId());
        entity.setIsNightShift(dto.getIsNightShift() == null ? false : dto.getIsNightShift());

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
    public EmployeeTimesheetActivitiesMappingNew toEntity(ActivityTimesheetDTO dto, Long timesheetId, Integer projectId, Long locationMappingId) {
        if (dto == null) {
            return null;
        }

        EmployeeTimesheetActivitiesMappingNew entity = new EmployeeTimesheetActivitiesMappingNew();
        
        // Set composite key
        entity.setTimesheetId(timesheetId != null ? timesheetId : dto.getTimesheetId());
        entity.setActivityId(dto.getActivityId());
        entity.setProjectId(projectId != null ? projectId : dto.getProjectId());
        entity.setId(dto.getId());
        entity.setLocationMappingId(locationMappingId);

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
        dto.setTimesheetId(entity.getTimesheetId());
        dto.setActivityId(entity.getActivityId());
        dto.setProjectId(entity.getProjectId());
        dto.setDescription(entity.getDescription());
        dto.setDurationMinutes(entity.getDurationMinutes() != null ? entity.getDurationMinutes().shortValue() : null);
      
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
                        activity -> activity.getProjectId()
                ));
    }
    
    public List<GetReporteesTimesheetReqDTO> map(
            List<GetReporteesTimesheetReqFlatDTO> rows) {

        Map<Long, GetReporteesTimesheetReqDTO> timesheetMap = new LinkedHashMap<>();

        for (GetReporteesTimesheetReqFlatDTO r : rows) {

            /* ================= TIMESHEET LEVEL ================= */
            GetReporteesTimesheetReqDTO timesheet =
                    timesheetMap.computeIfAbsent(
                            r.getTimesheetId(),
                            id -> new GetReporteesTimesheetReqDTO(
                                    id,
                                    r.getEmpId(),
                                    r.getEmployeeName(),
                                    r.getDayType(),
                                    r.getDate(),
                                    r.getIsNightShift(),
                                    r.getWorkCheckIn(),
                                    r.getWorkCheckOut(),
                                    new ArrayList<>(),
                                    new ArrayList<>()
                            )
                    );

            /* ================= LOCATION LEVEL ================= */
            GetReporteesTimesheetLocationsDTO location =
                    timesheet.getLocationSessions()
                            .stream()
                            .filter(l -> l.getLocationMappingId()
                                    .equals(r.getLocationMappingId()))
                            .findFirst()
                            .orElseGet(() -> {

                                GetReporteesTimesheetLocationsDTO loc =
                                        new GetReporteesTimesheetLocationsDTO(
                                                r.getWorkLocationType(),
                                                r.getLocationInTime(),
                                                r.getLocationOutTime(),
                                                r.getLocationMappingId(),
                                                new ArrayList<>()
                                        );

                                timesheet.getLocationSessions().add(loc);
                                return loc;
                            });

            /* ================= PROJECT LEVEL ================= */
            GetReporteesTimesheetProjectsDTO project =
                    location.getProjects()
                            .stream()
                            .filter(p -> p.getProjectId()
                                    .equals(r.getProjectId()))
                            .findFirst()
                            .orElseGet(() -> {

                                GetReporteesTimesheetProjectsDTO p =
                                        new GetReporteesTimesheetProjectsDTO(
                                                r.getProjectId(),
                                                r.getProjectName(),
                                                r.getClientName(),
                                                r.getClientLocation(),
                                                r.getPoNo(),
                                                r.getShadowEmp(),
                                                r.getStatus(),
                                                r.getTotalClientWorkingMinutes(),
                                                r.getDescription(),
                                                new ArrayList<>()
                                        );

                                location.getProjects().add(p);
                                return p;
                            });

            /* ================= ACTIVITY LEVEL ================= */
            if (r.getActivity() != null) {

                GetReporteesTimesheetActivitiesDTO activity =
                        new GetReporteesTimesheetActivitiesDTO(
                                r.getActivity(),
                                r.getActivityDescription(),
                                r.getDurationMinutes(),
                                r.getTeamName()
                        );

                project.getActivities().add(activity);
            }

            /* ================= DOCUMENT LEVEL ================= */
            if (r.getDocId() != null) {

                boolean alreadyAdded =
                        timesheet.getDocumentData()
                                .stream()
                                .anyMatch(d -> d.getDocId().equals(r.getDocId()));

                if (!alreadyAdded) {

                    GetReporteesTimesheetDocsDTO doc =
                            new GetReporteesTimesheetDocsDTO(
                                    r.getDocId(),
                                    r.getDocName(),
                                    r.getFinalFlag(),
                                    r.getBulkApprovedDocId(),
                                    r.getMimeType()
                            );

                    timesheet.getDocumentData().add(doc);
                }
            }
        }

        return new ArrayList<>(timesheetMap.values());
    }

}