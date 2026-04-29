package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.enums.TeamMemberStatusExecutionStatus;
import com.apmosys.employeeportal.model.TeamMemberStatusExecutionTracker;
import com.apmosys.employeeportal.repository.TeamMemberStatusExecutionTrackerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamMemberStatusExecutionTrackerService {

    private final TeamMemberStatusExecutionTrackerRepository trackerRepository;

    public boolean shouldRunForScheduler(LocalDate today) {
        TeamMemberStatusExecutionTracker tracker = trackerRepository.findTopByOrderByTrackerIdDesc().orElse(null);
        if (tracker == null || tracker.getLastExecutedDate() == null) {
            return true;
        }

        if (!today.equals(tracker.getLastExecutedDate())) {
            return true;
        }

        return EnumSet.of(
                TeamMemberStatusExecutionStatus.FAILED,
                TeamMemberStatusExecutionStatus.INTERRUPTED)
                .contains(tracker.getLastExecutedStatus());
    }

    @Transactional
    public void updateExecutionStatus(LocalDate executionDate, TeamMemberStatusExecutionStatus status) {
        TeamMemberStatusExecutionTracker tracker = trackerRepository.findTopByOrderByTrackerIdDesc()
                .orElseGet(TeamMemberStatusExecutionTracker::new);
        tracker.setLastExecutedDate(executionDate);
        tracker.setLastExecutedStatus(status);
        tracker.setUpdatedOn(LocalDateTime.now());
        trackerRepository.save(tracker);
    }
}
