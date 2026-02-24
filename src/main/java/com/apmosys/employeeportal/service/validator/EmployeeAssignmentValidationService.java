package com.apmosys.employeeportal.service.validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.model.DayTypeMasterNew;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.Exception.TimesheetValidationFailedException;

@Component
public class EmployeeAssignmentValidationService {

	/*
	 *Project is mapped with team in Teams Table
	 *Employee is mapped with team in Employee Team mapping 
	 *And Activity is mapped against a team in Activities
	 *
	 */
	@Autowired
    private TeamRepository projectTeamMappingRepository;

    @Autowired
    private EmployeeTeamMapRepository employeeTeamMappingRepository;

    @Autowired
    private ActivitiesRepository teamActivityMappingRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DayTypeMasterNewRepository dayTypeMasterNewRepository;

	/**
     * Entry method to validate employee eligibility.
     * For non-fillable day types (holiday, week off, leave, etc.) only project existence is validated;
     * activity mapping validation is skipped since user does not fill activities on those days.
     *
     * @param empId     employee id
     * @param date      timesheet date
     * @param locations location sessions (projects and activities)
     * @param dayTypeId day type id (optional); when non-null and non-working, activity validation is skipped
     */
    public void validateEmployeeAssignments(
            Long empId,
            LocalDate date,
            List<LocationSessionDTO> locations,
            Integer dayTypeId) {

        if (locations == null || locations.isEmpty()) {
            return;
        }

        boolean skipActivityValidation = isNonFillableDayType(dayTypeId);

        for (LocationSessionDTO location : locations) {
            validateProjects(empId, date, location, skipActivityValidation);
        }
    }

    /**
     * True when dayTypeId is non-null and represents a non-working day (e.g. Holiday, Week Off, Leave).
     * On such days we skip activity mapping validation.
     */
    private boolean isNonFillableDayType(Integer dayTypeId) {
        if (dayTypeId == null) {
            return false;
        }
        Optional<DayTypeMasterNew> opt = dayTypeMasterNewRepository.findById(dayTypeId);
        return opt.map(dt -> !Boolean.TRUE.equals(dt.getIsWorkingDay())).orElse(false);
    }

    private void validateProjects(
            Long empId,
            LocalDate date,
            LocationSessionDTO location,
            boolean skipActivityValidation) {

        if (location.getProjects() == null) {
            return;
        }

        for (ProjectTimesheetDTO project : location.getProjects()) {

            // Validate project existence (basic sanity)
            validateProjectExists(project.getProjectId());

            // Activity mapping validation only for fillable (working) days; user does not fill activities on non-fillable days
            if (!skipActivityValidation) {
                validateActivities(
                        empId,
                        date,
                        project.getProjectId(),
                        project.getActivities()
                );
            }
        }
    }
    
    private void validateActivities(
            Long empId,
            LocalDate date,
            Integer projectId,
            List<ActivityTimesheetDTO> activities) {

        if (activities == null || activities.isEmpty()) {
            return;
        }

        for (ActivityTimesheetDTO activity : activities) {

            Long teamId = activity.getTeamId();

            if (teamId == null) {
                throw new IllegalArgumentException(
                        "Team is mandatory for activity. "
                      + "ActivityId=" + activity.getActivityId()
                    );
            }

            // 1️ Project ↔ Team validation
            validateProjectTeamMapping(projectId, teamId);

            // 2️ Employee ↔ Team (date-based)
            validateEmployeeTeamMapping(empId, teamId, date);

            // 3️ Activity ↔ Team validation
            validateActivityBelongsToTeam(
                    activity.getActivityId(),
                    teamId
            );
        }
    }
    
    private void validateProjectExists(Integer projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new TimesheetValidationFailedException(
                    "Invalid project selected.");
        }
    }

    private void validateProjectTeamMapping(
            Integer projectId,
            Long teamId) {

    	Team t=projectTeamMappingRepository
                .findByTeamIdAndProjectId(
                        teamId,
                        projectId
                );
             if(t==null) {
            	 throw new TimesheetValidationFailedException(
                         "Team is not mapped to the selected project.");
             }
                       
    }
   private void validateEmployeeTeamMapping(
            Long empId,
            Long teamId,
            LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        boolean exists =
                employeeTeamMappingRepository
                        .existsEmployeeTeamMappingForDate(
                                empId,
                                teamId,
                                startOfDay,
                                endOfDay
                        );

        if (!exists) {
            throw new IllegalArgumentException(
                    "Employee is not mapped to the selected team "
                  + "on the given date. "
                  + "EmpId=" + empId
                  + ", TeamId=" + teamId
                  + ", Date=" + date
            );
        }
    }
    
    private void validateActivityBelongsToTeam(
            Long activityId,
            Long teamId) {

        boolean exists =
                teamActivityMappingRepository
                        .existsByActivityIdAndTeamId(
                                activityId,
                                teamId
                        );

        if (!exists) {
            throw new TimesheetValidationFailedException(
                    "Selected activity does not belong to the selected team.");
        }
    }


}
