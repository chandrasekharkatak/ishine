package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.FinalDocumentNew;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;

public interface FinalDocumentNewRepository extends JpaRepository<FinalDocumentNew, Long> {  
	
    @Query("SELECT fd FROM FinalDocumentNew fd WHERE fd.projectId = :projectId")
    List<FinalDocumentNew> findByProjectId(@Param("projectId") Long projectId);
    

    @Query("SELECT fdn from FinalDocumentNew fdn \n"+
        "INNER JOIN TimesheetDocumentDetailsNew tdd on tdd.bulkApprovedDocId = fdn.finalDocId \n"+
        "WHERE tdd.projectId = :projectId and tdd.timesheetId in :timesheetIds and tdd.finalFlag = true \n"+
        "AND tdd.clientApprovalStatusId = 2"
    )
    List<FinalDocumentNew> getDocsByTimesheetIdsAndFinalFlag(
				@Param("timesheetIds") List<Long> timesheetIds,
                @Param("projectId") Integer projectId
			);

	
 }
