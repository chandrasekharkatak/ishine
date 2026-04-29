package com.apmosys.employeeportal.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.enums.TeamMemberStatusTriggerSource;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamMemberStatusAsyncService {

    private final TeamMemberStatusOrchestrationService teamMemberStatusOrchestrationService;

    @Async
    public void triggerFromInternalFlow() {
        teamMemberStatusOrchestrationService.updateTeamMemberStatus(TeamMemberStatusTriggerSource.INTERNAL);
    }
}
