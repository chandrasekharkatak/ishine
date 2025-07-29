package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.TimesheetDocumentDetails;

public interface TimesheetDocumentDetailsRepository extends JpaRepository<TimesheetDocumentDetails, Long>{

	TimesheetDocumentDetails findByDocId(Long docId);
	TimesheetDocumentDetails findTopByTimesheetIdOrderByUpdatedOnDesc(Long timesheetId);
	
	@Query("SELECT t.docId FROM TimesheetDocumentDetails t WHERE t.timesheetId = :timesheetId")
	Long findDocIdByTimesheetId(@Param("timesheetId") Long timesheetId);

}
