package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDetailsNewDTO;
import com.apmosys.employeeportal.dto.*;
import com.apmosys.employeeportal.model.ClientStatusMasterNew;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
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

	@Modifying
	@Transactional
	@Query(value = "UPDATE timesheet_document_details_new \n"
			+ "SET project_id = :primaryProjectId, \n"
			+ "    updated_by = :updatedBy, \n"
			+ "    updated_on = NOW() \n"
			+ "WHERE project_id IN (:deletedProjectIds)", nativeQuery = true)
	int bulkMoveDocumentDetailsProjectToPrimary(
			@Param("primaryProjectId") Integer primaryProjectId,
			@Param("deletedProjectIds") List<Integer> deletedProjectIds,
			@Param("updatedBy") Long updatedBy
	);

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

	@Query("SELECT tdd FROM TimesheetDocumentDetailsNew tdd WHERE tdd.timesheetId = :timesheetId and tdd.projectId = :projectId and tdd.active = true")
	List<TimesheetDocumentDetailsNew> findByTimesheetIdAndProjectIdAndActive(@Param("timesheetId") Long timesheetId, @Param("projectId") Integer projectId);

	@Query("SELECT tdd FROM TimesheetDocumentDetailsNew tdd WHERE tdd.timesheetId = :timesheetId and tdd.active = true")
	List<TimesheetDocumentDetailsNew> findByTimesheetIdAndActive(@Param("timesheetId") Long timesheetId);

	@Query("SELECT tdd FROM TimesheetDocumentDetailsNew tdd WHERE tdd.bulkApprovedDocId = :bulkApproverId and tdd.active = true")
	List<TimesheetDocumentDetailsNew> getDocsByBulkApproverDocId(@Param("bulkApproverId") Long bulkApproverId);

	@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO(tdd) FROM TimesheetDocumentDetailsNew tdd WHERE tdd.timesheetId = :timesheetId and tdd.projectId in :projectIds and tdd.active = true")
	List<TimesheetDocumentDataDTO> getTimesheetDocumentDataByTimesheetIdAndProjectIds(@Param("timesheetId") Long timesheetId, @Param("projectIds") List<Long> projectIds);

	@Query("SELECT t FROM TimesheetDocumentDetailsNew t WHERE t.timesheetId = :timesheetId")
	List<TimesheetDocumentDetailsNew> findAllByTimesheetId(Long timesheetId);

	@Query("SELECT t FROM TimesheetDocumentDetailsNew t WHERE t.projectId = :projectId")
	List<TimesheetDocumentDetailsNew> findAllByProjectId(Long projectId);

	@Query( "SELECT etn \n" +
				"FROM EmployeeTimesheetsNew etn\n" +
				"INNER JOIN ProjectTimesheetStatusNew ptsn on etn.timesheetId = ptsn.id.timesheetId \n" +
				"INNER JOIN Project p on p.projectId = ptsn.id.projectId\n" +
				"INNER JOIN TimesheetDocumentDetailsNew tdd on tdd.timesheetId = etn.timesheetId AND tdd.projectId = ptsn.id.projectId\n" +
				"WHERE p.hasClientSideId = 1 \n" +
				"AND etn.dayTypeId IN (1,3,8) \n" + 
				"AND (etn.status = 3 OR etn.status = 1) \n" +
				// " OR tdd.bulkApprovedDocId IS NULL) \n"+
				"AND ptsn.id.projectId = :projectId \n" + 
				"AND etn.date BETWEEN :fromDate AND :toDate \n" +
				"AND etn.empId IN :empIds")
	List<EmployeeTimesheetsNew> getDocsByEmpIdsAndDate(
			@Param("empIds") List<Long> empIds,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate,
			@Param("projectId") Integer projectId
		);

	@Query("SELECT tdd from TimesheetDocumentDetailsNew tdd where tdd.timesheetId IN :timesheetIds and tdd.projectId = :projectId")
	List<TimesheetDocumentDetailsNew> getDocsByTimesheetIdsAndProjectId(@Param("timesheetIds") Set<Long> timesheetIds, @Param("projectId") Integer projectId);

	@Query("SELECT DISTINCT COALESCE(fdn.fileUrl, tdd.fileUrl, 'null') from TimesheetDocumentDetailsNew tdd \n"+
		   "left join FinalDocumentNew fdn on fdn.finalDocId = tdd.bulkApprovedDocId \n"+
		   "INNER JOIN EmployeeTimesheetsNew etn on etn.timesheetId = tdd.timesheetId \n"+
		   "INNER JOIN ProjectTimesheetStatusNew ptsn on ptsn.id.timesheetId = etn.timesheetId \n"+
		   "where etn.empId = :empId and etn.date = :date and ptsn.id.projectId = :projectId")
	String findFileUrlByEmpIdAndDate(@Param("empId") Long empId, @Param("date") LocalDate date, @Param("projectId") Integer projectId);

	@Query(value = "select tddn.final_flag from timesheet_document_details_new tddn where tddn.doc_id = :docId and tddn.active = true", nativeQuery= true)
	Byte getFinalFlagByDocId(@Param("docId") Long docId);

	@Query(value=" select tddn.bulk_approved_doc_id , fdn.file_url from timesheet_document_details_new tddn inner join final_document_new fdn  on tddn.bulk_approved_doc_id = fdn.final_doc_id "
	+ " where tddn.doc_id = :docId and tddn.active = true ",nativeQuery = true)
	List<Object[]> getFinalDocIdAndFileUrl(Long docId);

	@Modifying
	@Query(value = " UPDATE timesheet_document_details_new SET client_approval_status_id = 1, "+
    " final_flag = 0, "+
    " bulk_approved_doc_id = NULL, "+
    " updated_on = NOW() "+
	" WHERE doc_id = :docId "
	, nativeQuery = true)
	void resetApprovalStatus(Long docId); //need to add updated by here also

	@Modifying
	@Transactional
	@Query("DELETE FROM TimesheetDocumentDetailsNew tdd WHERE tdd.timesheetId = :timesheetId and tdd.projectId = :projectId")
	void deleteByTimesheetIdAndProjectId(@Param("timesheetId") Long timesheetId, @Param("projectId") Integer projectId);


}