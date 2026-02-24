package com.apmosys.employeeportal.service.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.apmosys.employeeportal.dto.TimesheetDTO_new.RejectionDataDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.model.EmployeeTimesheetActivitiesMappingNew;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusId;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.utility.DateConversionUtil;
import com.apmosys.employeeportal.utility.TimesheetFormatUtil;

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
        entity.setIsNightShift(dto.getIsNightShift());
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
        // Non-working days have null workCheckIn/workCheckOut; avoid calling localDateTimeToString(null)
        dto.setWorkCheckIn(entity.getWorkCheckIn() == null ? null : DateConversionUtil.localDateTimeToString(entity.getWorkCheckIn(), pattern));
        dto.setWorkCheckOut(entity.getWorkCheckOut() == null ? null : DateConversionUtil.localDateTimeToString(entity.getWorkCheckOut(), pattern));
        dto.setIsNightShift(entity.getIsNightShift());
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
        dto.setPoId(entity.getPoId());
        dto.setClientApprovalStatus(entity.getClientApprovalStatus());
        dto.setStatus(entity.getStatus());
        dto.setShadowEmpId(entity.getShadowEmpId());
        dto.setTotalClientWorkingMinutes(entity.getTotalClientWorkingMinutes());
        dto.setLocationMappingId(entity.getId().getLocationMappingId());
        // Map clientLocationId (int in entity -> long in DTO)
        if (entity.getClientLocationId() != null) {
            dto.setClientLocationId(entity.getClientLocationId().longValue());
        }
        // Map description and clientSideId so frontend can use them in update flow
        dto.setDescription(entity.getDescription());
        dto.setClientSideId(entity.getClientSideId());
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
        
        // New inserts: do NOT set id - let JPA generate (IDENTITY). Using dto.getId() from
        // existing rows causes insert failures after delete-replace during update.
        entity.setTimesheetId(timesheetId != null ? timesheetId : dto.getTimesheetId());
        entity.setActivityId(dto.getActivityId());
        entity.setProjectId(projectId != null ? projectId : dto.getProjectId());
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
    
    public List<GetReporteesTimesheetReqDTO> mapNew(
            List<GetReporteesTimesheetReqFlatDTO> rows) {

        Map<Long, GetReporteesTimesheetReqDTO> timesheetMap = new LinkedHashMap<>();

        System.out.println(rows);
        for (GetReporteesTimesheetReqFlatDTO r : rows) {
            /* ================= TIMESHEET LEVEL ================= */
            GetReporteesTimesheetReqDTO timesheet =
                    timesheetMap.computeIfAbsent(
                            r.getTimesheetId(),
                            id -> new GetReporteesTimesheetReqDTO(
                                    id,
                                    r.getEmpId(),
                                    r.getEmploymentId(),
                                    r.getEmployeeName(),
                                    r.getDayType(),
                                    TimesheetFormatUtil.formatDate(r.getDate()),
                                    r.getIsNightShift(),
                                    TimesheetFormatUtil.formatTime(r.getWorkCheckIn()),
                                    TimesheetFormatUtil.formatTime(r.getWorkCheckOut()),
                                    r.getProjectCount(),
                                    r.getLocationCount(),
                                    r.getAppliedBy(),
                                    TimesheetFormatUtil.formatDateTime(r.getAppliedOn()),
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
                                                TimesheetFormatUtil.formatTime(r.getLocationInTime()),
                                                TimesheetFormatUtil.formatTime(r.getLocationOutTime()),
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
                                                TimesheetFormatUtil.formatMinutes(r.getTotalClientWorkingMinutes()),
                                                r.getDescription(),
                                                new ArrayList<>(), new ArrayList<>()
                                        );

                                location.getProjects().add(p);
                                return p;
                            });

            /* ================= ACTIVITY LEVEL ================= */
            if (r.getActivity() != null) {

//                GetReporteesTimesheetActivitiesDTO activity =
//                        new GetReporteesTimesheetActivitiesDTO(
//                                r.getActivity(),
//                                // r.getActivityDescription(),
//                                TimesheetFormatUtil.formatMinutes(r.getDurationMinutes()),
//                                r.getTeamName()
//                        );

//                project.getActivities().add(activity);
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
                                    r.getMimeType(),
                                    r.getDocsProjectId()
                            );

                    timesheet.getDocumentData().add(doc);
                }
            }
            /* ================= REJECTION LEVEL ================= */
//            if(r.getRejectionReason() != null && r.getRemarks() != null) {
//            	RejectionDataDTO rejectionData = new RejectionDataDTO(r.getDocsProjectId(),r.getLocationMappingId(),r.getTimesheetId(), r.getRejectionReason() , r.getRemarks(), r.getRejectedOn());
//            	if(!project.getRejectionReasons().contains(rejectionData))
//            	project.getRejectionReasons().add(rejectionData);
//            }
        }

        return new ArrayList<>(timesheetMap.values());
    }
    
    public List<GetReporteesTimesheetReqDTO> map(List<GetReporteesTimesheetReqFlatDTO> rows) {

        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, GetReporteesTimesheetReqDTO> timesheetMap = new LinkedHashMap<>();

        // timesheetId -> (locationMappingId -> locationDto)
        Map<Long, Map<Long, GetReporteesTimesheetLocationsDTO>> tsLocMap = new HashMap<>();

        // timesheetId -> (locationMappingId -> (projectId -> projectDto))
        // ✅ Still grouped as Timesheet -> Location -> Project (no grouping expectation changed)
        Map<Long, Map<Long, Map<Integer, GetReporteesTimesheetProjectsDTO>>> tsLocProjectMap = new HashMap<>();

        // Dedupe: project list insert once
        Map<Long, Map<Long, Set<Integer>>> seenProjects = new HashMap<>();

        // Activity dedupe per actual routing: timesheetId -> actLocId -> actProjectId -> set(actKey)
        Map<Long, Map<Long, Map<Integer, Set<String>>>> seenActivityKeys = new HashMap<>();

        // Rejection dedupe per actual routing: timesheetId -> rejLocId -> rejProjectId -> set(rejKey)
        Map<Long, Map<Long, Map<Integer, Set<String>>>> seenRejectionKeys = new HashMap<>();

        // docs at timesheet level
        Map<Long, Set<Long>> seenDocIds = new HashMap<>();

        for (GetReporteesTimesheetReqFlatDTO row : rows) {

            if (row == null || row.getTimesheetId() == null) continue;

            final Long timesheetId = row.getTimesheetId();

            // =======================
            // 1) TIMESHEET (create once)
            // =======================
            GetReporteesTimesheetReqDTO ts = timesheetMap.computeIfAbsent(timesheetId, id -> {
                GetReporteesTimesheetReqDTO dto = new GetReporteesTimesheetReqDTO();

                dto.setTimesheetId(id);
                dto.setEmpId(row.getEmpId());
                dto.setEmploymentId(row.getEmploymentId());
                dto.setEmployeeName(row.getEmployeeName());
                dto.setDayType(row.getDayType());

                dto.setDate(row.getDate() != null ? TimesheetFormatUtil.formatDate(row.getDate()) : null);
                dto.setIsNightShift(row.getIsNightShift());

                dto.setWorkCheckIn(row.getWorkCheckIn() != null ? TimesheetFormatUtil.formatTime(row.getWorkCheckIn()) : null);
                dto.setWorkCheckOut(row.getWorkCheckOut() != null ? TimesheetFormatUtil.formatTime(row.getWorkCheckOut()) : null);

                dto.setProjectCount(row.getProjectCount());
                dto.setLocationCount(row.getLocationCount());
                dto.setAppliedBy(row.getAppliedBy());

                dto.setAppliedOn(row.getAppliedOn() != null ? TimesheetFormatUtil.formatDateTime(row.getAppliedOn()) : null);

                dto.setLocationSessions(new ArrayList<>());
                dto.setDocumentData(new ArrayList<>());
                return dto;
            });

            // init maps
            tsLocMap.computeIfAbsent(timesheetId, k -> new LinkedHashMap<>());
            tsLocProjectMap.computeIfAbsent(timesheetId, k -> new HashMap<>());

            seenProjects.computeIfAbsent(timesheetId, k -> new HashMap<>());
            seenActivityKeys.computeIfAbsent(timesheetId, k -> new HashMap<>());
            seenRejectionKeys.computeIfAbsent(timesheetId, k -> new HashMap<>());

            seenDocIds.computeIfAbsent(timesheetId, k -> new HashSet<>());

            // =======================
            // Helper inline: Ensure Location exists for a given locId
            // =======================
            // (No helper methods requested, so this is inline repeated when needed)

            // =======================
            // 2) BASE ROW: ensure base location & base project exist (if present)
            // =======================
            if (row.getLocationMappingId() != null) {

                final Long baseLocId = row.getLocationMappingId();
                Map<Long, GetReporteesTimesheetLocationsDTO> locMap = tsLocMap.get(timesheetId);

                GetReporteesTimesheetLocationsDTO baseLocDto = locMap.get(baseLocId);
                if (baseLocDto == null) {
                    baseLocDto = new GetReporteesTimesheetLocationsDTO();
                    baseLocDto.setLocationMappingId(baseLocId);
                    baseLocDto.setWorkLocationType(row.getWorkLocationType());
                    baseLocDto.setLocationInTime(row.getLocationInTime() != null ? TimesheetFormatUtil.formatDateTime(row.getLocationInTime()) : null);
                    baseLocDto.setLocationOutTime(row.getLocationOutTime() != null ? TimesheetFormatUtil.formatDateTime(row.getLocationOutTime()) : null);
                    baseLocDto.setProjects(new ArrayList<>());

                    locMap.put(baseLocId, baseLocDto);
                    ts.getLocationSessions().add(baseLocDto);
                }

                tsLocProjectMap.get(timesheetId).computeIfAbsent(baseLocId, k -> new LinkedHashMap<>());
                seenProjects.get(timesheetId).computeIfAbsent(baseLocId, k -> new HashSet<>());

                // Ensure base project exists under base location (if projectId present)
                if (row.getProjectId() != null) {
                    final Integer baseProjectId = row.getProjectId();

                    Map<Integer, GetReporteesTimesheetProjectsDTO> projMap = tsLocProjectMap.get(timesheetId).get(baseLocId);
                    GetReporteesTimesheetProjectsDTO projDto = projMap.get(baseProjectId);

                    if (projDto == null) {
                        projDto = new GetReporteesTimesheetProjectsDTO();
                        projDto.setProjectId(baseProjectId);
                        projDto.setProjectName(row.getProjectName());
                        projDto.setClientName(row.getClientName());
                        projDto.setClientLocation(row.getClientLocation());
                        projDto.setPoNo(row.getPoNo());
                        projDto.setShadowEmp(row.getShadowEmp());
                        projDto.setStatus(row.getStatus());
                        projDto.setTotalClientWorkingMinutes(
                                row.getTotalClientWorkingMinutes() != null
                                        ? TimesheetFormatUtil.formatMinutes(row.getTotalClientWorkingMinutes())
                                        : null
                        );
                        projDto.setDescription(row.getDescription());
                        projDto.setActivities(new ArrayList<>());
                        projDto.setRejectionReasons(new ArrayList<>());

                        projMap.put(baseProjectId, projDto);
                        if (seenProjects.get(timesheetId).get(baseLocId).add(baseProjectId)) {
                            baseLocDto.getProjects().add(projDto);
                        }
                    }
                }
            }

            // =======================
            // 3) ACTIVITY: ROUTE using activityLocationMappingId + activityProjectId
            // without changing grouping expectations (still stored under Timesheet->Location->Project)
            // =======================
            boolean hasActivity =
                    row.getActivityTimesheetId() != null
                            || row.getActivityLocationMappingId() != null
                            || row.getActivityProjectId() != null
                            || (row.getActivity() != null && !row.getActivity().trim().isEmpty());

            if (hasActivity) {

                // Choose effective routing IDs:
                final Long actLocId = row.getActivityLocationMappingId() != null
                        ? row.getActivityLocationMappingId()
                        : row.getLocationMappingId();

                final Integer actProjectId = row.getActivityProjectId() != null
                        ? row.getActivityProjectId()
                        : row.getProjectId();

                if (actLocId != null && actProjectId != null) {

                    // Ensure location exists
                    Map<Long, GetReporteesTimesheetLocationsDTO> locMap = tsLocMap.get(timesheetId);
                    GetReporteesTimesheetLocationsDTO actLocDto = locMap.get(actLocId);

                    if (actLocDto == null) {
                        actLocDto = new GetReporteesTimesheetLocationsDTO();
                        actLocDto.setLocationMappingId(actLocId);
                        actLocDto.setWorkLocationType(row.getWorkLocationType());
                        actLocDto.setLocationInTime(row.getLocationInTime() != null ? TimesheetFormatUtil.formatDateTime(row.getLocationInTime()) : null);
                        actLocDto.setLocationOutTime(row.getLocationOutTime() != null ? TimesheetFormatUtil.formatDateTime(row.getLocationOutTime()) : null);
                        actLocDto.setProjects(new ArrayList<>());

                        locMap.put(actLocId, actLocDto);
                        ts.getLocationSessions().add(actLocDto);
                    }

                    // Ensure project exists under activity location
                    tsLocProjectMap.get(timesheetId).computeIfAbsent(actLocId, k -> new LinkedHashMap<>());
                    seenProjects.get(timesheetId).computeIfAbsent(actLocId, k -> new HashSet<>());

                    Map<Integer, GetReporteesTimesheetProjectsDTO> projMap = tsLocProjectMap.get(timesheetId).get(actLocId);
                    GetReporteesTimesheetProjectsDTO projDto = projMap.get(actProjectId);

                    if (projDto == null) {
                        projDto = new GetReporteesTimesheetProjectsDTO();
                        projDto.setProjectId(actProjectId);
                        projDto.setProjectName(row.getProjectName());
                        projDto.setClientName(row.getClientName());
                        projDto.setClientLocation(row.getClientLocation());
                        projDto.setPoNo(row.getPoNo());
                        projDto.setShadowEmp(row.getShadowEmp());
                        projDto.setStatus(row.getStatus());
                        projDto.setTotalClientWorkingMinutes(
                                row.getTotalClientWorkingMinutes() != null
                                        ? TimesheetFormatUtil.formatMinutes(row.getTotalClientWorkingMinutes())
                                        : null
                        );
                        projDto.setDescription(row.getDescription());
                        projDto.setActivities(new ArrayList<>());
                        projDto.setRejectionReasons(new ArrayList<>());

                        projMap.put(actProjectId, projDto);
                        if (seenProjects.get(timesheetId).get(actLocId).add(actProjectId)) {
                            actLocDto.getProjects().add(projDto);
                        }
                    }

                    // Dedupe structures for activity routing
                    seenActivityKeys.get(timesheetId).computeIfAbsent(actLocId, k -> new HashMap<>());
                    seenActivityKeys.get(timesheetId).get(actLocId).computeIfAbsent(actProjectId, k -> new HashSet<>());

                    // Dedupe key: ALL THREE (as requested)
                    final String actKey =
                            String.valueOf(row.getActivityTimesheetId())
                                    + "|" + String.valueOf(row.getActivityLocationMappingId())
                                    + "|" + String.valueOf(row.getActivityProjectId());

                    if (seenActivityKeys.get(timesheetId).get(actLocId).get(actProjectId).add(actKey)) {
                        GetReporteesTimesheetActivitiesDTO act = new GetReporteesTimesheetActivitiesDTO();

                        act.setTimesheetId(row.getActivityTimesheetId());
                        act.setLocationMappingId(row.getActivityLocationMappingId());
                        act.setProjectId(row.getActivityProjectId());
                        act.setActivity(row.getActivity());
                        act.setDurationMinutes(row.getDurationMinutes() != null
                                ? TimesheetFormatUtil.formatMinutes(row.getDurationMinutes())
                                : null
                        );

                        projDto.getActivities().add(act);
                    }
                }
            }

            // =======================
            // 4) REJECTION: ROUTE using rejectionLocationMappingId + rejectionProjectId
            // =======================
            boolean hasRejection =
                    row.getRejectionReason() != null
                            || row.getRemarks() != null
                            || row.getRejectedOn() != null
                            || row.getRejectionLocationMappingId() != null
                            || row.getRejectionProjectId() != null;

            if (hasRejection) {

                final Long rejLocId = row.getRejectionLocationMappingId() != null
                        ? row.getRejectionLocationMappingId()
                        : row.getLocationMappingId();

                final Integer rejProjectId = row.getRejectionProjectId() != null
                        ? row.getRejectionProjectId()
                        : row.getProjectId();

                if (rejLocId != null && rejProjectId != null) {

                    // Ensure location exists
                    Map<Long, GetReporteesTimesheetLocationsDTO> locMap = tsLocMap.get(timesheetId);
                    GetReporteesTimesheetLocationsDTO rejLocDto = locMap.get(rejLocId);

                    if (rejLocDto == null) {
                        rejLocDto = new GetReporteesTimesheetLocationsDTO();
                        rejLocDto.setLocationMappingId(rejLocId);
                        rejLocDto.setWorkLocationType(row.getWorkLocationType());
                        rejLocDto.setLocationInTime(row.getLocationInTime() != null ? TimesheetFormatUtil.formatDateTime(row.getLocationInTime()) : null);
                        rejLocDto.setLocationOutTime(row.getLocationOutTime() != null ? TimesheetFormatUtil.formatDateTime(row.getLocationOutTime()) : null);
                        rejLocDto.setProjects(new ArrayList<>());

                        locMap.put(rejLocId, rejLocDto);
                        ts.getLocationSessions().add(rejLocDto);
                    }

                    // Ensure project exists under rejection location
                    tsLocProjectMap.get(timesheetId).computeIfAbsent(rejLocId, k -> new LinkedHashMap<>());
                    seenProjects.get(timesheetId).computeIfAbsent(rejLocId, k -> new HashSet<>());

                    Map<Integer, GetReporteesTimesheetProjectsDTO> projMap = tsLocProjectMap.get(timesheetId).get(rejLocId);
                    GetReporteesTimesheetProjectsDTO projDto = projMap.get(rejProjectId);

                    if (projDto == null) {
                        projDto = new GetReporteesTimesheetProjectsDTO();
                        projDto.setProjectId(rejProjectId);
                        projDto.setProjectName(row.getProjectName());
                        projDto.setClientName(row.getClientName());
                        projDto.setClientLocation(row.getClientLocation());
                        projDto.setPoNo(row.getPoNo());
                        projDto.setShadowEmp(row.getShadowEmp());
                        projDto.setStatus(row.getStatus());
                        projDto.setTotalClientWorkingMinutes(
                                row.getTotalClientWorkingMinutes() != null
                                        ? TimesheetFormatUtil.formatMinutes(row.getTotalClientWorkingMinutes())
                                        : null
                        );
                        projDto.setDescription(row.getDescription());
                        projDto.setActivities(new ArrayList<>());
                        projDto.setRejectionReasons(new ArrayList<>());

                        projMap.put(rejProjectId, projDto);
                        if (seenProjects.get(timesheetId).get(rejLocId).add(rejProjectId)) {
                            rejLocDto.getProjects().add(projDto);
                        }
                    }

                    // Dedupe structures for rejection routing
                    seenRejectionKeys.get(timesheetId).computeIfAbsent(rejLocId, k -> new HashMap<>());
                    seenRejectionKeys.get(timesheetId).get(rejLocId).computeIfAbsent(rejProjectId, k -> new HashSet<>());

                    String rejOnFmt = row.getRejectedOn() != null ? TimesheetFormatUtil.formatDateTime(row.getRejectedOn()) : "";
                    String rejKey = "RSN:" + (row.getRejectionReason() == null ? "" : row.getRejectionReason().trim())
                            + "|RMK:" + (row.getRemarks() == null ? "" : row.getRemarks().trim())
                            + "|ON:" + rejOnFmt;

                    if (seenRejectionKeys.get(timesheetId).get(rejLocId).get(rejProjectId).add(rejKey)) {
                        RejectionDataDTO rej = new RejectionDataDTO();
                        rej.setRejectionReason(row.getRejectionReason());
                        rej.setRemark(row.getRemarks());
                        rej.setRejectedOn(rejOnFmt);
                        projDto.getRejectionReasons().add(rej);
                    }
                }
            }

            // =======================
            // 5) DOCUMENTS (timesheet level, dedupe by docId)
            // =======================
            if (row.getDocId() != null && seenDocIds.get(timesheetId).add(row.getDocId())) {
                GetReporteesTimesheetDocsDTO doc = new GetReporteesTimesheetDocsDTO();
                doc.setDocId(row.getDocId());
                doc.setDocName(row.getDocName());
                doc.setFinalFlag(row.getFinalFlag());
                doc.setBulkApprovedDocId(row.getBulkApprovedDocId());
                doc.setMimeType(row.getMimeType());
                doc.setDocsProjectId(row.getDocsProjectId());
                ts.getDocumentData().add(doc);
            }
        }

        return new ArrayList<>(timesheetMap.values());
    }

}