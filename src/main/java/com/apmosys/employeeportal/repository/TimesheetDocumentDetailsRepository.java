package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.TimesheetDocumentDetails;

public interface TimesheetDocumentDetailsRepository extends JpaRepository<TimesheetDocumentDetails, Long>{

	TimesheetDocumentDetails findByDocIdAndActive(Long docId, Boolean active);
	
	TimesheetDocumentDetails findTopByTimesheetIdAndActive(Long timesheetId,Boolean active);
	
	@Query("SELECT t FROM TimesheetDocumentDetails t WHERE t.timesheetId = :timesheetId")
	TimesheetDocumentDetails findByTimesheetId(Long timesheetId);
	
	@Query("SELECT t.docId FROM TimesheetDocumentDetails t WHERE t.timesheetId = :timesheetId")
	Long findDocIdByTimesheetId(@Param("timesheetId") Long timesheetId);
	
	@Query("SELECT tdd FROM TimesheetDocumentDetails tdd \n" +
		       "INNER JOIN Timesheet et on et.timesheetId = tdd.timesheetId \n" +
		       "WHERE et.empId = :empId \n" +
		       "AND et.date BETWEEN :fromDate AND :toDate")
		List<TimesheetDocumentDetails> getDocsByEmpAndDateRange(
		    @Param("empId") Long empId,
		    @Param("fromDate") LocalDate fromDate,
		    @Param("toDate") LocalDate toDate
		);

}
