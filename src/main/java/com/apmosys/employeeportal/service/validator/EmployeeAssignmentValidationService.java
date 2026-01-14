package com.apmosys.employeeportal.service.validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.model.Team;
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

    /* -------------------- PROJECT + TEAM VALIDATION -------------------- */

    private void validateProjects(
            Long empId,
            LocalDate date,
            LocationSessionDTO location) {

        if (location.getProjects() == null) {
            return;
        }

        for (ProjectTimesheetDTO project : location.getProjects()) {
        	
        	//TODO can we validate selected project is correct or not for 
        	//timesheet date as start date end date may be null(does all or billable only project contains start and end date)

            // 1 Project ↔ Team validation
            Team team = validateProjectTeamMapping(
                    project.getProjectId(),
                    project.getTeamId()
            );

            // 2️ Employee ↔ Team (date-based)
            validateEmployeeTeamMapping(
                    empId,
                    team.getTeamId(),
                    date
            );

            // 3️ Activity ↔ Team validation
            validateActivitiesForTeam(
                    project.getActivities(),
                    team.getTeamId()
            );
        }
    }

    /* -------------------- RULE 1: PROJECT ↔ TEAM -------------------- */

    private Team validateProjectTeamMapping(
            Integer projectId,
            Long teamId) {

        Team team = projectTeamMappingRepository
                .findByTeamIdAndProjectIdAndIsActive(
                        teamId,
                        projectId,
                        "Y"
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Selected team is not active or "
                              + "not mapped to the selected project. "
                              + "ProjectId=" + projectId
                              + ", TeamId=" + teamId
                        ));

        return team;
    }

    /* -------------------- RULE 2 & 4: EMPLOYEE ↔ TEAM (DATE VALID) -------------------- */

    private void validateEmployeeTeamMapping(
            Long empId,
            Long teamId,
            LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        boolean exists = employeeTeamMappingRepository
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

    /* -------------------- RULE 3: ACTIVITY ↔ TEAM -------------------- */

    private void validateActivitiesForTeam(
            List<ActivityTimesheetDTO> activities,
            Long teamId) {

        if (activities == null || activities.isEmpty()) {
            return;
        }

        for (ActivityTimesheetDTO activityDTO : activities) {

            boolean exists =
                    teamActivityMappingRepository
                            .existsByActivityIdAndTeamId(
                                    activityDTO.getActivityId(),
                                    teamId
                            );

            if (!exists) {
                throw new IllegalArgumentException(
                        "Selected activity does not belong to "
                      + "the employee's team. "
                      + "ActivityId=" + activityDTO.getActivityId()
                      + ", TeamId=" + teamId
                );
            }
        }
    }

}
