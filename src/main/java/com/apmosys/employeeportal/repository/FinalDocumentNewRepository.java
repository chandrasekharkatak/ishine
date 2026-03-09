package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.FinalDocumentDownloadDTO;
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

    @Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO_new.FinalDocumentDownloadDTO(etn.empId, e.name, p.projectName,fdn.fileUrl ) from FinalDocumentNew fdn \n"+
        "INNER JOIN TimesheetDocumentDetailsNew tdd on tdd.bulkApprovedDocId = fdn.finalDocId \n"+
        "INNER JOIN EmployeeTimesheetsNew etn on etn.timesheetId = tdd.timesheetId \n"+
        "INNER JOIN Employee e on e.empId = etn.empId \n"+
        "INNER JOIN Project p on p.projectId = fdn.projectId \n"+
        "WHERE tdd.projectId in :projectIds and etn.empId in :empIds and MONTH(etn.date) = :month and YEAR(etn.date) = :year\n"+ 
        "and tdd.finalFlag = true \n"+
        "AND tdd.clientApprovalStatusId = 2")
    List<FinalDocumentDownloadDTO> getAllFinalDocumentsByEmpIdAndProjectIdInMonthAndYear(@Param("empIds") List<Long> empIds, @Param("projectIds") List<Integer> projectIds, @Param("month") Integer month, @Param("year") Integer year );

    @Query("SELECT fdn from FinalDocumentNew fdn \n"+
    "INNER JOIN TimesheetDocumentDetailsNew tdd on tdd.bulkApprovedDocId = fdn.finalDocId \n"+
    "WHERE tdd.projectId = :projectId and tdd.timesheetId = :timesheetIds and tdd.finalFlag = true \n"+
    "AND tdd.clientApprovalStatusId = 2"
)
List<FinalDocumentNew> getDocsByTimesheetIdAndFinalFlag(
            @Param("timesheetIds") Long timesheetIds,
            @Param("projectId") Integer projectId
        );

    //     @Query("DELETE FROM FinalDocumentNew fdn \n"+
    //     "INNER JOIN TimesheetDocumentDetailsNew tdd on tdd.bulkApprovedDocId = fdn.finalDocId \n"+
    //     "WHERE tdd.timesheetId = :timesheetId and tdd.projectId = :projectId")
    // void deleteByTimesheetIdAndProjectId(@Param("timesheetId") Long timesheetId, @Param("projectId") Integer projectId);


	
 }
