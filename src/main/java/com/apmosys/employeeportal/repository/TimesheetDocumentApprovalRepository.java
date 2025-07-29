package com.apmosys.employeeportal.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TimesheetDocumentApproval;

@Repository
public interface TimesheetDocumentApprovalRepository extends JpaRepository<TimesheetDocumentApproval, Long> {
	
	@Query("SELECT t FROM TimesheetDocumentApproval t WHERE t.timesheetId = :timesheetId")
    TimesheetDocumentApproval findByTimesheetId(@Param("timesheetId") Long timesheetId);

}
