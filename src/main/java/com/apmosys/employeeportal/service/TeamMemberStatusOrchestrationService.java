package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.enums.TeamMemberStatusExecutionStatus;
import com.apmosys.employeeportal.enums.TeamMemberStatusTriggerSource;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamMemberStatusOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(TeamMemberStatusOrchestrationService.class);

    private final TeamsService teamsService;
    private final TeamMemberStatusExecutionTrackerService trackerService;

    public void updateTeamMemberStatus(TeamMemberStatusTriggerSource triggerSource) {
        boolean schedulerTriggered = TeamMemberStatusTriggerSource.SCHEDULER.equals(triggerSource);
        LocalDate today = LocalDate.now();

        if (schedulerTriggered && !trackerService.shouldRunForScheduler(today)) {
            log.info("Skipping updateTeamMemberStatus. Already succeeded for today.");
            return;
        }

        try {
            executeTeamMemberStatusUpdateCoreLogic();
            if (schedulerTriggered) {
                trackerService.updateExecutionStatus(today, TeamMemberStatusExecutionStatus.SUCCESS);
            }
        } catch (Exception ex) {
            if (schedulerTriggered) {
                TeamMemberStatusExecutionStatus status = ex instanceof InterruptedException
                        ? TeamMemberStatusExecutionStatus.INTERRUPTED
                        : TeamMemberStatusExecutionStatus.FAILED;
                trackerService.updateExecutionStatus(today, status);
            }
            throw new RuntimeException("Failed to execute updateTeamMemberStatus", ex);
        }
    }

    /**
     * Core business logic intentionally unchanged.
     */
    protected void executeTeamMemberStatusUpdateCoreLogic() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        teamsService.processDeactivations(todayStart, now);
        teamsService.processActivations(todayStart, tomorrowStart, now);
        teamsService.updateDefaultProjectMappings();
    }
}
