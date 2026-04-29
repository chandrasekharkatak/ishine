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
        teamMemberStatusOrchestrationService.updateTeamMemberStatus(SchedulerTriggerType.INTERNAL);
    }

    @Async
    public void triggerFromController() {
        teamMemberStatusOrchestrationService.updateTeamMemberStatus(SchedulerTriggerType.CONTROLLER);
    }
}
