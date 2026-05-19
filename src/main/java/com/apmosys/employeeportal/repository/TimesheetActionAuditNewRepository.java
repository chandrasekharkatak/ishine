package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.TimesheetActionAuditNew;

public interface TimesheetActionAuditNewRepository extends JpaRepository<TimesheetActionAuditNew, Long>{

	@Modifying
	@Transactional
	@Query(value = "UPDATE timesheet_action_audit \n"
			+ "SET project_id = :primaryProjectId \n"
			+ "WHERE project_id IN (:deletedProjectIds)", nativeQuery = true)
	int bulkMoveActionAuditProjectToPrimary(
			@Param("primaryProjectId") Integer primaryProjectId,
			@Param("deletedProjectIds") List<Integer> deletedProjectIds
	);
	
}
