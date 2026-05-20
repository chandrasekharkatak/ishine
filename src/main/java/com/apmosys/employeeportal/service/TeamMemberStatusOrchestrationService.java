package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.enums.SchedulerTriggerType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamMemberStatusOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(TeamMemberStatusOrchestrationService.class);

    private final TeamsService teamsService;
    private final SchedulerExecutionTrackerService schedulerExecutionTrackerService;

    private static final String METHOD_NAME = "updateTeamMemberStatus";

    public void updateTeamMemberStatus(SchedulerTriggerType triggerType) {
        updateTeamMemberStatus(triggerType, null);
    }

    public void updateTeamMemberStatus(SchedulerTriggerType triggerType, Long scopedProjectIdForMapping) {
        if (SchedulerTriggerType.SCHEDULER.equals(triggerType)
                && !schedulerExecutionTrackerService.shouldRunTodayForScheduler(METHOD_NAME)) {
            log.info("Skipping {}. Already executed successfully today.", METHOD_NAME);
            return;
        }

        schedulerExecutionTrackerService.logExecutionStart(METHOD_NAME, triggerType);
        try {
            executeTeamMemberStatusUpdateCoreLogic(scopedProjectIdForMapping);
            schedulerExecutionTrackerService.logExecutionSuccess(METHOD_NAME, triggerType);
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                schedulerExecutionTrackerService.logExecutionInterrupted(METHOD_NAME, triggerType, ex.getMessage());
            } else {
                schedulerExecutionTrackerService.logExecutionFailure(METHOD_NAME, triggerType, ex.getMessage());
            }
            throw new RuntimeException("Failed to execute updateTeamMemberStatus", ex);
        } finally {
            log.debug("updateTeamMemberStatus completed triggerType={}", triggerType);
        }
    }

    protected void executeTeamMemberStatusUpdateCoreLogic(Long scopedProjectIdForMapping) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        teamsService.processDeactivations(todayStart, now);
        teamsService.processActivations(todayStart, tomorrowStart, now);
        teamsService.updateDefaultProjectMappings(scopedProjectIdForMapping);
        if (scopedProjectIdForMapping == null) {
            teamsService.cleanupDuplicateDefaultProjectMappings();
        }
    }
}
