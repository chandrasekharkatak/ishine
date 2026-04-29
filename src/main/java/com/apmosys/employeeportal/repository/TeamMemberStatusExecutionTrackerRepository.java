package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TeamMemberStatusExecutionTracker;

@Repository
public interface TeamMemberStatusExecutionTrackerRepository
        extends JpaRepository<TeamMemberStatusExecutionTracker, Long> {

    Optional<TeamMemberStatusExecutionTracker> findTopByOrderByTrackerIdDesc();
}
