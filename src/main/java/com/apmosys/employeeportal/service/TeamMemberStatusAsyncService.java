package com.apmosys.employeeportal.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.enums.SchedulerTriggerType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamMemberStatusAsyncService {

    private final TeamMemberStatusOrchestrationService teamMemberStatusOrchestrationService;

    @Async
    public void triggerFromInternalFlow() {
        triggerFromInternalFlow(null);
    }

    @Async
    public void triggerFromInternalFlow(Long scopedProjectIdForMapping) {
        teamMemberStatusOrchestrationService.updateTeamMemberStatus(
                SchedulerTriggerType.INTERNAL, scopedProjectIdForMapping);
    }

    @Async
    public void triggerFromController() {
        teamMemberStatusOrchestrationService.updateTeamMemberStatus(SchedulerTriggerType.CONTROLLER);
    }
}
