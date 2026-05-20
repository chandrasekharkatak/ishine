package com.apmosys.employeeportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.service.TeamMemberStatusAsyncService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/scheduler/run")
@RequiredArgsConstructor
public class SchedulerRunnerController {

    private final TeamMemberStatusAsyncService teamMemberStatusAsyncService;

    @PostMapping("/updateTeamMemberStatus")
    public ResponseEntity<String> runUpdateTeamMemberStatus() {
        teamMemberStatusAsyncService.triggerFromController();
        return ResponseEntity.ok("Triggered updateTeamMemberStatus");
    }
}
