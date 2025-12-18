package com.apmosys.employeeportal.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.TimesheetDocumentApprovalDTO;
import com.apmosys.employeeportal.model.TimesheetDocumentApproval;

@Repository
public interface TimesheetDocumentApprovalRepository extends JpaRepository<TimesheetDocumentApproval, Long> {
	
	@Query("SELECT t FROM TimesheetDocumentApproval t WHERE t.timesheetId = :timesheetId")
    TimesheetDocumentApproval findByTimesheetId(@Param("timesheetId") Long timesheetId);
	
	@Query(
		    "SELECT new com.apmosys.employeeportal.dto.TimesheetDocumentApprovalDTO(" +
		    "  CASE " +
		    "    WHEN t.hierarchyOrder = 3 AND t.rejectionLevel = 2 THEN 'Rejected By HR' " +
		    "    WHEN t.hierarchyOrder = 2 AND t.rejectionLevel = 1 THEN 'Rejected By HOD' " +
		    "    WHEN t.hierarchyOrder = 1 AND t.rejectionLevel = 1 THEN 'Rejected By RM' " +
		    "    ELSE 'Other' " +
		    "  END, " +
		    "  COUNT(t)" +
		    ") " +
		    "FROM TimesheetDocumentApproval t " +
		    "WHERE (t.hierarchyOrder = 3 AND t.rejectionLevel = 2) " +
		    "   OR (t.hierarchyOrder = 2 AND t.rejectionLevel = 1) " +
		    "   OR (t.hierarchyOrder = 1 AND t.rejectionLevel = 1) " +
		    "GROUP BY " +
		    "  CASE " +
		    "    WHEN t.hierarchyOrder = 3 AND t.rejectionLevel = 2 THEN 'Rejected By HR' " +
		    "    WHEN t.hierarchyOrder = 2 AND t.rejectionLevel = 1 THEN 'Rejected By HOD' " +
		    "    WHEN t.hierarchyOrder = 1 AND t.rejectionLevel = 1 THEN 'Rejected By RM' " +
		    "    ELSE 'Other' " +
		    "  END"
		)
		List<TimesheetDocumentApprovalDTO> getRejectionCountsByLevel();

	List<TimesheetDocumentApproval> findAllByTimesheetIdIn(List<Long> timesheetIds);


}
