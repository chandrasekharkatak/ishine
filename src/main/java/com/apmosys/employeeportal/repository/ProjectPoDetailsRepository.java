package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.hibernate.annotations.Where;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.IshineToPoEmpDetailsSharingDTO;
import com.apmosys.employeeportal.dto.IshineToPoEmployeeDTO;
import com.apmosys.employeeportal.dto.PoDetailsDto;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.RmgResourceRequirementDto;
import com.apmosys.employeeportal.model.ProjectPoDetails;

@Repository
public interface ProjectPoDetailsRepository extends JpaRepository<ProjectPoDetails, Long> {

    @Query(value = "Select new com.apmosys.employeeportal.dto.PoDetailsDto(p.id, p.poId, p.projectId, p.poNo, p.poStartDate, p.poEndDate, p.active) \n"
            + " from ProjectPoDetails p where p.projectId=:projectId and p.active=true AND (p.poEndDate IS NULL OR DATE(p.poEndDate) >= CURRENT_DATE) ")
    List<PoDetailsDto> getActivePoDetailsDtoByProjectId(Integer projectId);

    @Query(value = "Select DISTINCT new com.apmosys.employeeportal.dto.PoDetailsDto(p.id, p.poId, p.projectId, p.poNo, p.poStartDate, p.poEndDate, p.active \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 1 THEN etm.empId END)  \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 2 THEN etm.empId END) \n"
            + ") \n"
            + "FROM ProjectPoDetails p  \n"
            + "INNER JOIN PoRequirementMapping prm ON p.poId=prm.poId and prm.active = true  \n"
            + "INNER JOIN EmployeeTeamMap etm ON etm.poRequirementMappingId = prm.poRequirementMappingId   \n"
            + "INNER JOIN Team t ON t.teamId = etm.teamId  \n"
            + "where p.projectId=:projectId \n"
            + "AND (p.active = true or etm.active IN (1, 2)) \n"
            + "GROUP BY p.id, p.poId, p.projectId, p.poNo, p.poStartDate, p.poEndDate, p.active")
    List<PoDetailsDto> getAllPoDetailsDtoByProjectId(Integer projectId);

    @Query(value = "Select new com.apmosys.employeeportal.dto.PoDetailsDto(ppd.id, ppd.poId, p.projectId, ppd.poNo, ppd.poStartDate, ppd.poEndDate, ppd.active \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 1 THEN etm.empId END)  \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 2 THEN etm.empId END) \n"
            + ") \n"
            + "FROM Project p  \n"
            + "LEFT JOIN ProjectPoDetails ppd ON p.projectId = ppd.projectId AND ((ppd.poEndDate IS NULL OR DATE(ppd.poEndDate) >= CURRENT_DATE) or :isAllProjects = true ) \n"
            + "LEFT JOIN PoRequirementMapping prm ON ppd.poId=prm.poId and prm.active = true \n"
            + "LEFT JOIN Team t ON t.projectId = p.projectId AND t.isActive = 'Y'  \n"
            + "LEFT JOIN EmployeeTeamMap etm ON t.teamId =etm.teamId AND etm.active IN (1, 2)\n"
            + "where p.projectId=:projectId \n"
            + "GROUP BY ppd.id, ppd.poId, p.projectId, ppd.poNo, ppd.poStartDate, ppd.poEndDate, ppd.active")
    List<PoDetailsDto> getAllProjectPoDetailsDtoByProjectId(Integer projectId, boolean isAllProjects);
    
    
    @Transactional
    @Modifying
    @Query("DELETE FROM ProjectPoDetails")
    void deleteAllRecords();

    List<ProjectPoDetails> findByProjectId(Integer projectId);

    @Query("SELECT new com.apmosys.employeeportal.dto.IshineToPoEmpDetailsSharingDTO( "
            + " ppo.poNo,ppo.active,p.clientId,p.projectId) "
            + "   FROM ProjectPoDetails ppo\n"
            + "   LEFT JOIN Project p\n"
            + "    on p.projectId=ppo.projectId"
            + "   WHERE ppo.poId = :poId AND ppo.poProjectId = :projectId and ppo.active=1")
    IshineToPoEmpDetailsSharingDTO findPoBasicDetails(Long poId, Long projectId);

    @Query("SELECT new com.apmosys.employeeportal.dto.IshineToPoEmployeeDTO( "
            + "        	        e.employeementId,e.name,prm.role,prm.experience,prm.department,"
            + "        	        d.deptId,prm.clientRoleId,p.clientId,"
            + "        	        COUNT(ts.id),MIN(ts.date),MAX(ts.date),e.isApmosysProduct) "
            + "        	    FROM ProjectPoDetails ppo "
            + "        	    LEFT JOIN EmployeeTeamMap etm ON etm.poId = ppo.poId AND etm.active = 1 "
            + "        	    LEFT JOIN Employee e ON e.empId = etm.empId "
            + "        	    LEFT JOIN PoRequirementMapping prm  "
            + "        	         ON prm.poRequirementMappingId = etm.poRequirementMappingId "
            + "        	    LEFT JOIN Department d ON d.name = prm.department "
            + "        	    LEFT JOIN EmployeeClientSideIdMapping ecsm ON ecsm.empId = etm.empId "
            + "				LEFT JOIN Project p on p.projectId=ppo.projectId "
            + "              LEFT JOIN ProjectPoDetails ppd on ppd.poId=ppo.poId "
            + "        	    LEFT JOIN Timesheet ts ON ts.empId = e.empId "
            + "        	        AND ts.date BETWEEN :startDate AND :endDate "
            + "        	    WHERE ppo.poId = :poId "
            + "        	      AND ppo.projectId = :projectId "
            + "				  AND ppd.clientAddressId IS NOT NULL	"
            + "        	    GROUP BY  e.employeementId, e.name,"
            + "        	        prm.role, prm.experience, prm.department,"
            + "        	        d.deptId,prm.clientRoleId, p.clientId,e.isApmosysProduct")
    List<IshineToPoEmployeeDTO> findEmployeesWithTimesheetStats(Long poId,
            Integer projectId, LocalDate startDate, LocalDate endDate);

    Optional<ProjectPoDetails> findByPoIdAndProjectIdAndActiveTrue(Long poId, Integer projectId);

    boolean existsByPoIdAndProjectId(Long poId, Integer projectId);

    @Query("Select po.poId from ProjectPoDetails po where po.projectId= :projectId and active = true")
    List<Long> findActivePoIdsByProjectId(@Param("projectId") Integer projectId);

    List<ProjectPoDetails> findByProjectIdAndActiveTrue(Integer projectId);

    Optional<ProjectPoDetails> findByProjectIdAndPoIdAndActiveTrue(Integer projectId, Long poId);

    List<ProjectPoDetails> findByProjectIdAndActiveFalse(Integer projectId);

		List<ProjectPoDetails> findByProjectIdAndPoIdIn(Integer projectId, Set<Long> poIdsFromPortal);
		
		// @Query("SELECT new com.apmosys.employeeportal.dto.IshineToPoEmployeeDTO( "
        	// 	+ "       e.employeementId,e.name,rd.role,rd.experience,rd.department,"
        	// 	+ "       prm.clientRoleId,p.clientId,COUNT(DISTINCT et.timesheetId),"
        	// 	+ "       MIN(et.date),MAX(et.date),e.isApmosysProduct,ppo.poId,"
        	// 	+ "		  et.empId,et.shadowEmpId,etm.isShadow ) "
        	// 	+ "     FROM Timesheet et  "
        	// 	+ "		LEFT JOIN TimesheetActivityMap etam on etam.timesheetId=et.timesheetId"
        	// 	+ "		LEFT JOIN Activity a on a.activityId=etam.activityId"
        	// 	+ "		LEFT JOIN TimesheetDocumentDetails edd on edd.timesheetId=et.timesheetId "
        	// 	+ "         AND edd.clientApprovalStatus in ('Approved','approved') AND edd.finalFlag=1 "
        	// 	+ "		LEFT JOIN Team t on t.teamId=a.teamId "
        	// 	+ "		LEFT JOIN Project p on p.projectId=t.projectId "
        	// 	+ "     LEFT JOIN ProjectPoDetails ppo on ppo.projectId = p.projectId "
        	// 	+ "          AND ppo.clientAddressId IS NOT NULL AND ppo.active=1"
        	// 	+ "		LEFT JOIN PoRequirementMapping prm ON prm.poId = ppo.poId "
        	// 	+ "		LEFT JOIN EmployeeTeamMap etm on etm.empId = et.empId AND etm.poId=t.poId "
        	// 	+ "            and etm.roleId = prm.roleId"
        	// 	+ "		LEFT JOIN RoleDetails rd on rd.roleId = etm.roleId "
        	// 	+ "		LEFT JOIN Employee e on e.empId=etm.empId "
        	// 	+ "		LEFT JOIN Department d ON d.name = prm.department "
        	// 	+ "		where 1=1 AND et.empId = :empId AND p.clientId = :clientId "
        	// 	+ "        AND t.poId = :poId and et.status in ('Approved','approved') "
        	// 	+ "        AND etm.active!= 2 AND et.dayType not in ('Leave') "
        	// 	+ "        AND et.date BETWEEN :startDate AND :endDate")
        	// IshineToPoEmployeeDTO findEmployeesWithTimesheetCount(Long empId,
        	//          LocalDate startDate,LocalDate endDate,Long poId,Integer clientId);


    @Query(value = "Select new com.apmosys.employeeportal.dto.PoDetailsDto(p.projectId, ppd.poId \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 1 THEN etm.empId END)  \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 2 THEN etm.empId END) \n"
            + ") \n"
            + "FROM Project p  \n"
            + "LEFT JOIN ProjectPoDetails ppd ON p.projectId = ppd.projectId AND (ppd.poEndDate IS NULL OR DATE(ppd.poEndDate) >= CURRENT_DATE) \n"
            + "LEFT JOIN PoRequirementMapping prm ON ppd.poId=prm.poId and prm.active = true \n"
            + "LEFT JOIN Team t ON t.projectId = p.projectId AND t.isActive = 'Y' \n"
            + "LEFT JOIN EmployeeTeamMap etm ON t.teamId = etm.teamId AND etm.active IN (1, 2)\n"
            + "where ppd.poId=:poId \n"
            + "GROUP BY ppd.poId, p.projectId")
    List<PoDetailsDto> getResourceRequirementCountByPoId(Long poId);

    @Query(value = "Select new com.apmosys.employeeportal.dto.PoDetailsDto(p.projectId, ppd.poId \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 1 THEN etm.empId END)  \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 2 THEN etm.empId END) \n"
            + ") \n"
            + "FROM Project p  \n"
            + "LEFT JOIN ProjectPoDetails ppd ON p.projectId = ppd.projectId AND (ppd.poEndDate IS NULL OR DATE(ppd.poEndDate) >= CURRENT_DATE) \n"
            + "LEFT JOIN PoRequirementMapping prm ON ppd.poId=prm.poId \n"
            + "LEFT JOIN Team t ON t.projectId = p.projectId AND t.isActive = 'Y' \n"
            + "LEFT JOIN EmployeeTeamMap etm ON t.teamId = etm.teamId AND etm.active IN (1, 2)\n"
            + "where ppd.poId=:poId and p.projectId=:projectId \n"
            + "GROUP BY ppd.poId, p.projectId")
    List<PoDetailsDto> getResourceRequirementCountByPoIdAndProjectId(Long poId, Integer projectId);

    @Query(value = "Select new com.apmosys.employeeportal.dto.PoDetailsDto(p.projectId, ppd.poId \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 1 THEN etm.empId END)  \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 2 THEN etm.empId END) \n"
            + ") \n"
            + "FROM Project p  \n"
            + "LEFT JOIN ProjectPoDetails ppd ON p.projectId = ppd.projectId AND (DATE(ppd.poStartDate) <= CURRENT_DATE OR :currentActivePO = false) AND (ppd.poEndDate IS NULL OR DATE(ppd.poEndDate) >= CURRENT_DATE) \n"
        //  + "LEFT JOIN PoRequirementMapping prm ON ppd.poId=prm.poId \n"
            + "LEFT JOIN Team t ON t.projectId = p.projectId AND t.isActive = 'Y'  \n"
            + "LEFT JOIN EmployeeTeamMap etm ON t.teamId = etm.teamId AND etm.active IN (1, 2) \n"
            + "where p.projectId=:projectId \n"
            + "GROUP BY p.projectId ")
    List<PoDetailsDto> getResourceRequirementCountByProjectId(Integer projectId, boolean currentActivePO);

    @Query(value = "SELECT DISTINCT p.poId \n"
            + "    FROM ProjectPoDetails p\n"
            + "    WHERE p.poId IN :poIds\n"
            + "      AND p.active = true")
    List<Long> findDistinctActivePoIds(@Param("poIds") List<Long> poIds);

    @Modifying
    @Query(value = "UPDATE ProjectPoDetails p\n"
            + "       SET p.empIdApmosysRm = :rmEmpId,\n"
            + "           p.apmosysRM = :rmName,\n"
            + "           p.apmosysRmEmail = :rmEmail,\n"
            + "           p.updatedBy = :updatedBy \n"
            + "     WHERE p.poId IN :poIds\n"
            + "       AND p.active = true")
    int updateRmForActivePos(
            @Param("poIds") List<Long> poIds,
            @Param("rmEmpId") Long rmEmpId,
            @Param("rmName") String rmName,
            @Param("rmEmail") String rmEmail,
            @Param("updatedBy") Long updatedBy);

    @Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.RmgResourceRequirementDto( \n"
            + " prm.poRequirementMappingId, ppd.poId, ppd.poNo, ppd.poStartDate, ppd.poEndDate, rd.roleId, rd.role, rd.experience, rd.department,prm.lineItemStartDate , prm.lineItemEndDate, prm.count"
            + " )  \n"
            + "FROM Project p \n"
            + "INNER JOIN ProjectPoDetails ppd ON ppd.projectId = p.projectId AND (DATE(ppd.poStartDate) <= CURRENT_DATE OR :currentActivePO = false) AND (ppd.poEndDate IS NULL OR DATE(ppd.poEndDate) >= CURRENT_DATE) \n"
            + "LEFT JOIN PoRequirementMapping prm ON prm.poId = ppd.poId AND prm.active = true \n"
            + "LEFT JOIN RoleDetails rd on rd.roleId = prm.roleId \n"
            + "WHERE p.projectId =:projectId  \n")
    List<RmgResourceRequirementDto> getResourceRequirementDetailsByProjectId(Integer projectId, boolean currentActivePO);

    Optional<ProjectPoDetails> findByNextPOAndProjectIdAndActiveTrue(Long nextPo, Integer projectId);

    List<ProjectPoDetails> findByPoProjectIdInAndActiveTrue(List<Long> poProjectIds);
        
       
    @Query(value =" Select ppo from ProjectPoDetails ppo where ppo.poId IN :poIds and ppo.active = true ")
    List<ProjectPoDetails> findByPoIdInAndActive(List<Long> poIds);


    @Query("SELECT new com.apmosys.employeeportal.dto.IshineToPoEmployeeDTO( "
        		+ "       e.employeementId,e.name,rd.role,rd.experience,rd.department,"
        		+ "       prm.clientRoleId,p.clientId,COUNT(DISTINCT et.timesheetId),"
        		+ "       MIN(et.date),MAX(et.date),e.isApmosysProduct,ppo.poId,"
        		+ "		  et.empId,ptsn.shadowEmpId, etm.isShadow ) "
        		+ "     FROM EmployeeTimesheetsNew et  "
                        + " inner join EmployeeTimesheetLocationMapping etlm on etlm.timesheetId = et.timesheetId "
                        + " inner join ProjectTimesheetStatusNew ptsn on ptsn.id.timesheetId = etlm.timesheetId "
                        + " and etlm.locationMappingId = ptsn.id.locationMappingId "
                        + " left join EmployeeTimesheetActivitiesMappingNew etam on etam.timesheetId = et.timesheetId "
        		// + "		LEFT JOIN TimesheetActivityMap etam on etam.timesheetId=et.timesheetId"
                        + " and ptsn.id.projectId = etam.projectId and etlm.locationMappingId = etam.locationMappingId "
        		+ "		LEFT JOIN Activity a on a.activityId=etam.activityId"
        		+ "		LEFT JOIN TimesheetDocumentDetailsNew edd on edd.timesheetId=et.timesheetId "
        		+ "         AND edd.clientApprovalStatusId = 1 AND edd.finalFlag = 1 "
        		+ "		LEFT JOIN Team t on t.teamId=a.teamId "
        		+ "		LEFT JOIN Project p on p.projectId=t.projectId "
        		+ "     LEFT JOIN ProjectPoDetails ppo on ppo.projectId = p.projectId "
        		+ "          AND ppo.clientAddressId IS NOT NULL AND ppo.active=1"
        		+ "		LEFT JOIN PoRequirementMapping prm ON prm.poId = ppo.poId "
        		+ "		LEFT JOIN EmployeeTeamMap etm on etm.empId = et.empId AND etm.poId=ppo.poId "
        		+ "            and etm.roleId = prm.roleId"
        		+ "		LEFT JOIN RoleDetails rd on rd.roleId = etm.roleId "
        		+ "		LEFT JOIN Employee e on e.empId=etm.empId "
        		+ "		LEFT JOIN Department d ON d.name = prm.department "
        		+ "		where 1=1 AND et.empId = :empId AND p.clientId = :clientId "
        		+ "        AND etm.poId = :poId and et.status = 2 "
        		+ "        AND etm.active!= 2 AND et.dayTypeId not in (5 ,9) "
        		+ "        AND et.date BETWEEN :startDate AND :endDate")
        	IshineToPoEmployeeDTO findEmployeesWithTimesheetCount(Long empId,
        	         LocalDate startDate,LocalDate endDate,Long poId,Integer clientId);
	ProjectPoDetails findByPoId(Long poId);

	boolean existsByPoIdAndProjectIdAndActiveTrue(Long poId, Integer projectId);

    @Query(value="SELECT DISTINCT p.projectName FROM ProjectPoDetails ppd \n"
            + "INNER JOIN Project p ON ppd.projectId = p.projectId AND (DATE(ppd.poStartDate) <= CURRENT_DATE OR :currentActivePO = false) AND (ppd.poEndDate IS NULL OR DATE(ppd.poEndDate) >= CURRENT_DATE) AND ppd.active  = 1 \n"
            + "Where LOWER(ppd.poNo) like %:poNo% ")
    List<String> getProjectNameByPoNoLike(String poNo, boolean currentActivePO);
        
   
    @Query(value="SELECT DISTINCT p.projectId FROM Project p \n"
            + "INNER JOIN ProjectPoDetails ppd ON ppd.projectId = p.projectId AND (DATE(ppd.poStartDate) <= CURRENT_DATE OR :currentActivePO = false) AND (ppd.poEndDate IS NULL OR DATE(ppd.poEndDate) >= CURRENT_DATE) AND ppd.active  = 1 \n")
    Set<Integer> findAllActivePOProjectIds(boolean currentActivePO); 

    @Query(value = "SELECT poId \n"
    		+ "  FROM ProjectPoDetails \n"
    		+ "  WHERE projectId IN (:projectIds) \n"
    		+ "  AND active = true")
	Set<Long> findActivePoIdsByProjectIdIn(@Param("projectIds") Set<Integer> projectIds); 

}