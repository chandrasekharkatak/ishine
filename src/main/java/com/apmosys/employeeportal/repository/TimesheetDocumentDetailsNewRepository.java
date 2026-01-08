package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDetailsNewDTO;
import com.apmosys.employeeportal.dto.*;
import com.apmosys.employeeportal.model.ClientStatusMasterNew;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;

public interface TimesheetDocumentDetailsNewRepository extends JpaRepository<TimesheetDocumentDetailsNew, Long> {

	// ========== BACKUP: Original methods renamed with _old suffix ==========
	// TimesheetDocumentDetailsNew findByDocIdAndActive_old(Long docId, Boolean
	// active);
	// TimesheetDocumentDetailsNew findByDocIdAndFinalFlag_old(Long docId, Boolean
	// finalFlag);
	// TimesheetDocumentDetailsNew findTopByTimesheetIdAndActive_old(Long
	// timesheetId,Boolean active);

	@Query("SELECT t FROM TimesheetDocumentDetails t WHERE t.timesheetId = :timesheetId and t.active = true")
	TimesheetDocumentDetailsNew findByTimesheetId_old(Long timesheetId);

	@Query("SELECT t.docId FROM TimesheetDocumentDetails t WHERE t.timesheetId = :timesheetId and t.active = true")
	Long findDocIdByTimesheetId_old(@Param("timesheetId") Long timesheetId);

	@Query("SELECT t.docId FROM TimesheetDocumentDetails t WHERE t.timesheetId = :timesheetId and t.active = true")
	List<Long> findDocIdsByTimesheetId_old(@Param("timesheetId") Long timesheetId);

	// ========== UPDATED: New methods using _new entities ==========
	TimesheetDocumentDetailsNew findByDocIdAndActive(Long docId, Boolean active);

	TimesheetDocumentDetailsNew findByDocIdAndFinalFlag(Long docId, Boolean finalFlag);

	TimesheetDocumentDetailsNew findTopByTimesheetIdAndActive(Long timesheetId, Boolean active);

	@Query("SELECT t FROM TimesheetDocumentDetailsNew t WHERE t.timesheetId = :timesheetId and t.active = true")
	TimesheetDocumentDetailsNew findByTimesheetId(Long timesheetId);

	@Query("SELECT t.docId FROM TimesheetDocumentDetailsNew t WHERE t.timesheetId = :timesheetId and t.active = true")
	Long findDocIdByTimesheetId(@Param("timesheetId") Long timesheetId);

	@Query("SELECT t.docId FROM TimesheetDocumentDetailsNew t WHERE t.timesheetId = :timesheetId and t.active = true")
	List<Long> findDocIdsByTimesheetId(@Param("timesheetId") Long timesheetId);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query("SELECT tdd FROM TimesheetDocumentDetails tdd \n" +
			"INNER JOIN Timesheet et on et.timesheetId = tdd.timesheetId \n" +
			"WHERE et.empId = :empId \n" +
			"AND et.date BETWEEN :fromDate AND :toDate and tdd.active = true")
	List<TimesheetDocumentDetailsNew> getDocsByEmpAndDateRange_old(
			@Param("empId") Long empId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);

	// ========== UPDATED: New query using _new entities ==========
	@Query("SELECT tdd FROM TimesheetDocumentDetailsNew tdd \n" +
			"INNER JOIN EmployeeTimesheetsNew et on et.timesheetId = tdd.timesheetId \n" +
			"WHERE et.empId = :empId \n" +
			"AND et.date BETWEEN :fromDate AND :toDate and tdd.active = true")
	List<TimesheetDocumentDetailsNew> getDocsByEmpAndDateRange(
			@Param("empId") Long empId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);

	// ========== BACKUP: Original query renamed with _old suffix ==========
	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO(" +
			"t.docId, t.docName, t.timesheetId, t.empId, t.active, " +
			"t.clientApprovalStatus, t.rmApprovalStatus, t.hrApprovalStatus, t.finalFlag, t.bulkApprovedDocId) " +
			"FROM TimesheetDocumentDetails t " +
			"WHERE t.timesheetId = :timesheetId AND t.active = true")
	List<TimesheetDocumentDetailsDTO> findAllDocIdByTimesheetId_old(@Param("timesheetId") Long timesheetId);

	// ========== UPDATED: New query using _new entities ==========
	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDetailsNewDTO(" +
			"tdd.docId, tdd.docName, fdn.docName, tdd.timesheetId, et.empId, tdd.active, " +
			"tdd.clientApprovalStatusId, tdd.finalFlag) " +
			"FROM TimesheetDocumentDetailsNew tdd " +
			"INNER JOIN EmployeeTimesheetsNew et ON et.timesheetId = tdd.timesheetId " +
			"LEFT JOIN FinalDocumentNew fdn ON fdn.finalDocId = tdd.bulkApprovedDocId " +
			"LEFT JOIN ClientStatusMasterNew csm ON csm.statusId = tdd.clientApprovalStatusId " +
			"WHERE tdd.timesheetId = :timesheetId AND tdd.active = true")
	List<TimesheetDocumentDetailsNewDTO> findAllDocIdByTimesheetId(@Param("timesheetId") Long timesheetId);

	// ========== UPDATED: New query using _new entities ==========
	@Query("SELECT d FROM TimesheetDocumentDetailsNew d " +
			"JOIN EmployeeTimesheetsNew t ON d.timesheetId = t.timesheetId " +
			"WHERE t.empId = :empId AND t.date = :date")
	List<TimesheetDocumentDetailsNew> findDocumentsByEmpIdAndDate(
			@Param("empId") Long empId,
			@Param("date") LocalDate date);

	List<TimesheetDocumentDetailsNew> findByDocId(Long docId);

	@Query("SELECT tdd FROM TimesheetDocumentDetailsNew tdd WHERE tdd.timesheetId = :timesheetId and tdd.active = true")
	List<TimesheetDocumentDetailsNew> findByTimesheetIdAndActive(@Param("timesheetId") Long timesheetId);

}
