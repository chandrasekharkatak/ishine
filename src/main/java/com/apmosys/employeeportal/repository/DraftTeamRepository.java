package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.DraftTeam;

public interface DraftTeamRepository extends JpaRepository<DraftTeam, Long> {

}
