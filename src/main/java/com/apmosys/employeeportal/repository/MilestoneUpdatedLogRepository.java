package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.MilestoneUpdatedLog;

@Repository
public interface MilestoneUpdatedLogRepository extends JpaRepository<MilestoneUpdatedLog, Long> {
	
	
	List<MilestoneUpdatedLog> findByExtendedDate(Date extendedDate);
	
	
	 Optional<MilestoneUpdatedLog> findByMilestoneIdAndExtendedDate(Long milestoneId, Date extendedDate);
	 

	    Optional<MilestoneUpdatedLog> findByMilestoneNameAndExtendedDate(String milestoneName, Date extendedDate);
	    
	    Optional<MilestoneUpdatedLog> findTopByMilestoneIdOrderByUpdatedOnDesc(Long milestoneId);

	 
	 
	 

   
}