package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TimesheetDocumentDetails;

public interface TimesheetDocumentDetailsRepository extends JpaRepository<TimesheetDocumentDetails, Long>{

	TimesheetDocumentDetails findByDocId(Long docId);
	TimesheetDocumentDetails findTopByTimesheetIdOrderByUpdatedOnDesc(Long timesheetId);
}
