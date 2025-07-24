package com.apmosys.employeeportal.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.MilestoneUpdatedLog;

@Repository
public interface MilestoneUpdatedLogRepository extends JpaRepository<MilestoneUpdatedLog, Long> {
	
	
	Optional<MilestoneUpdatedLog> findByExtendedDate(Date ExtendedDate);

   
}