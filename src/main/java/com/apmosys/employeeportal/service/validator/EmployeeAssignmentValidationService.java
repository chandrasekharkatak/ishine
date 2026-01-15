package com.apmosys.employeeportal.service.validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;

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
	
	
	
	
	/**
     * Entry method to validate employee eligibility
     */
    public void validateEmployeeAssignments(
            Long empId,
            LocalDate date,
            List<LocationSessionDTO> locations) {

        if (locations == null || locations.isEmpty()) {
            return;
        }

        for (LocationSessionDTO location : locations) {
            validateProjects(empId, date, location);
        }
    }
    
    private void validateProjects(
            Long empId,
            LocalDate date,
            LocationSessionDTO location) {

        if (location.getProjects() == null) {
            return;
        }

        for (ProjectTimesheetDTO project : location.getProjects()) {

            // Validate project existence (basic sanity)
            validateProjectExists(project.getProjectId());

            // Activity-driven validation
            validateActivities(
                    empId,
                    date,
                    project.getProjectId(),
                    project.getActivities()
            );
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
            throw new IllegalArgumentException(
                    "Invalid project selected. ProjectId=" + projectId
            );
        }
    }

    private void validateProjectTeamMapping(
            Integer projectId,
            Long teamId) {

        projectTeamMappingRepository
                .findByTeamIdAndProjectIdAndIsActive(
                        teamId,
                        projectId,
                        "Y"
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Team is not mapped to the selected project. "
                              + "ProjectId=" + projectId
                              + ", TeamId=" + teamId
                        ));
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
            throw new IllegalArgumentException(
                    "Selected activity does not belong to the selected team. "
                  + "ActivityId=" + activityId
                  + ", TeamId=" + teamId
            );
        }
    }


}
