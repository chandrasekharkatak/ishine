
package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.ExpiredPoDto;
import com.apmosys.employeeportal.dto.GetProjectDetailsForBulkDefaultUpdateProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.ProjectNameAndPrjoectIdDTO;
import com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO;
import com.apmosys.employeeportal.dto.ResourceCountDto;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.RmAndHodEmailDto;
import com.apmosys.employeeportal.dto.SkippedEmployeeDTO;
import com.apmosys.employeeportal.dto.SummaryChartDTO;
import com.apmosys.employeeportal.dto.TimeSheetDetailsDto;
import com.apmosys.employeeportal.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

	public List<Project> findAllByProjectManagerId(Long projectManagerId);
	
	public List<Project> findByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getActivitiesByTeamIdAndEmployeeId(Long teamId, Long empId);

	public Project findByProjectName(String projectName);

	@Query(nativeQuery = true)
	public List<Object[]> getAllProject();

	public Project findByPoProjectId(Long poProjectId);
	
	public boolean existsProjectByProjectName(String projectName);

	public List<Project> findBySyncProject(String sync);

	@Query(nativeQuery = true)
	public List<Object[]> getAllMyProjectByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> findProjectByIsDraftProject();

	@Query(nativeQuery = true)
	public List<Object[]> getAllInternalProject();

	public Project findByProjectId(Integer projectId);

	// added by anurag
	@Query(nativeQuery = true)
	public List<Object[]> findAllProjectByIsDraftAndIsActive();
	
	@Query(value ="select new com.apmosys.employeeportal.dto.ProjectFetchDTO(p.projectId, p.projectName, p.isDraftProject \n"+
			",case when exists (select 1 from EmployeeTeamMap etm where etm.teamId in \n"+
			"				   (select teamId from Team where projectId=p.projectId) and etm.active=2) then 2 \n"+
			"else 1 end \n"+
			",p.projectStatus )  \n"+
			"from Project p  \n"+
			"where p.isDraftProject IN ('false','true','Rejected') and p.projectId IN :projectIds")
	public List<ProjectFetchDTO> findAllProjectByIsDraftAndIsActiveOfProjectIds(@Param("projectIds") Set<Integer> projectIds);
	

	public List<Project> findProjectByDepartmentName(String name);

	public Project findByProjectIdAndProjectManagerId(Integer projectId, Long managerId);

	// @Query(nativeQuery=true,value="select DISTINCT po_project_type from projects where po_project_type IS NOT NULL")
	// List<String>finddistinctPoProjectType();

	@Query(nativeQuery=true,value="SELECT * FROM projects p inner join teams t on p.project_id = t.project_id WHERE STR_TO_DATE(p.po_end_date, '%Y-%m-%d') < CURDATE() ")
	public List<Project> getExpiredPolist();
	
	@Query(nativeQuery = true)
	public List<Object[]> getProjectInfo(Integer projectId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getPoProjectInfo(Long poProjectId);
	
	@Query(value = "select etm.team_id, t.team_name, etm.emp_id, e.name, etm.employee_role, e.billable_type, \n"
			+ "etm.start_date, etm.active, p.project_id, p.project_name, c.client_id, c.client_name, p.po_end_date, \n"
			+ "s.name spoc, tl.name teamLead, etm.employee_team_map_id, d.name as department, \n"
			+ "CASE WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id) \n"
			+ "ELSE CONCAT('A-', e.employeement_id) \n"
			+ "END \n"
			+ "from employee_team_mapping etm \n"
			+ "inner join teams t on t.team_id = etm.team_id \n"
			+ "inner join employee e on e.emp_id = etm.emp_id \n"
			+ "inner join projects p on p.project_id = t.project_id \n"
			+ "inner join clients c on c.client_id = p.client_id \n"
			+ "inner join job_role j on j.job_role_id = e.job_role_id\n"
			+ "inner join department d on d.dept_id = j.dept_id\n"
			+ "left join employee s on s.emp_id = t.spoc_id \n"
			+ "left join employee tl on tl.emp_id = t.team_lead_id \n"
			+ "where t.is_active != 'N' and etm.active != 0 and p.project_id = :projectId",nativeQuery = true)
	public List<Object[]> getTeamInfo(Integer projectId);
	
	@Query(nativeQuery=true,value="select DISTINCT po_project_type from projects where po_project_type IS NOT NULL")
	List<String>finddistinctPoProjectType();
	
	@Query(nativeQuery=true)
	public List<Object[]> getPoProjectDetailsForPoProjects();
	
	
	@Query(nativeQuery = true)
	public List<Object[]> getPoProjectDetailsBOthPOAndInternal();
	
	@Query(nativeQuery = true)
	List<Object[]> getExpiredPoProjects();
	
	@Query(nativeQuery = true)
	List<String> getEmployeeEmailsByProjectId(@Param("projectId") Integer projectId);

	@Query(nativeQuery = true)
	List<Object[]> getExpiredPoProjectsWithoutInterval();
	
	@Query(nativeQuery = true)
	List<Object[]> poProjectTimesheetSync(Set<Long> poProjectIdList);
	
	@Query(nativeQuery = true)
	public List<Object[]> getTeamIdsForPoProjectId(Long poProjectId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getEmpIdAndEmployeeRoleForLinkedPoTeams(Long teamId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeesByPoProjectId(String poProjectId);
	
	
	 @Query(value ="select projectId from Project where active = 'true' and poProjectId IS NULL")
	 Set<Integer> findAllActiveInternalProjectIds();
	 
	 @Query(value ="select p.projectId from Project p where p.active = 'true' and p.poProjectId IS NOT NULL")
	 Set<Integer> findAllActiveShankhProjectIds();
	 
	 @Query(value ="select p.projectId from Project p where p.active = 'true'")
	 Set<Integer> findAllActiveShankhInternalProjectIds();
	 
//	@Query(nativeQuery = true)
//	public int getAssignedEmployeesCountInProject(Long id);
    
	 
	    
	   
	 
	 
 @Query(value = "WITH employee_mapped AS (\n"
 		+ "    SELECT DISTINCT \n"
 		+ "        p.project_id, \n"
 		+ "        etm.emp_id as emp_ids, \n"
 		+ "        etm.active AS employee_active,\n"
 		+ "        po_project_id\n"
 		+ "    FROM projects p\n"
 		+ "    INNER JOIN teams t ON t.project_id = p.project_id\n"
 		+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
 		+ "    WHERE p.active = 'true' AND t.is_active = 'Y' AND etm.active IN (1, 2) \n"
 		+ "    and p.po_project_type = 'TNM'\n"
 		+ ")\n"
 		+ "SELECT \n"
 		+ "    project_id\n"
 		+ "    ,COUNT(DISTINCT CASE WHEN employee_active = 1 THEN emp_ids END) AS onboarded_employees\n"
 		+ "    ,COUNT(DISTINCT CASE WHEN employee_active = 2 THEN emp_ids END) AS pending_for_onboarded_employees\n"
 		+ "    ,COUNT(DISTINCT emp_ids) AS total_assigned_employees \n"
 		+ "FROM employee_mapped\n"
 		+ "WHERE po_project_id = :id \n"
 		+ "GROUP BY project_id",nativeQuery = true)
	public List<Object[]> getAssignedEmployeesCountInProject(@Param("id") Long id);
	 
	 
	 
	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeInformation(Long empId);

	@Query(value="SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO( \n"+
			"e.employeementId, e.name, d.name ,e.billableType, p.projectId,p.projectName, p.clientName,  \n" + 
			"p.apmosysRM ,p.clientRM ,p.poNo ,p.poProjectType ,p.poStartDate ,p.poEndDate )  \n" + 
			"from Project p  \n" + 
			"inner join Team t on p.projectId = t.projectId  \n" + 
			"inner join EmployeeTeamMap etm on t.teamId = etm.teamId  \n" + 
			"inner join Employee e on e.empId = etm.empId  \n" + 
			"inner join JobRole jr on e.jobRoleId = jr.jobRoleId  \n" + 
			"inner join Department d on d.deptId = jr.deptId  \n" + 
			"where p.active = 'true' AND t.isActive != 'N' AND etm.active != 0  \n" + 
			"AND e.employmentstatus != 'InActive'  \n" + 
			"AND e.billableType = 'Bench' AND (p.poProjectType like 'FIXED%COST' OR p.poProjectType like '%TNM%') \n"+
			"AND e.empId NOT BETWEEN 1 AND 6")
	public List<RMGFlatEmployeeProjectTeamDTO>  getExceptionEmployeeReport();
	
//	@Query(value="SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO( \n"+
//			"e.employeementId, e.name, d.name ,e.billableType, p.projectId,p.projectName, p.clientName,  \n" + 
//			"p.apmosysRM ,p.clientRM ,p.poNo ,p.poProjectType ,p.poStartDate ,p.poEndDate )  \n" + 
//			"from Project p  \n" + 
//			"inner join Team t on p.projectId = t.projectId  \n" + 
//			"inner join EmployeeTeamMap etm on t.teamId = etm.teamId  \n" + 
//			"inner join Employee e on e.empId = etm.empId  \n" + 
//			"inner join JobRole jr on e.jobRoleId = jr.jobRoleId  \n" + 
//			"inner join Department d on d.deptId = jr.deptId  \n" + 
//			"where p.active = 'true' AND t.isActive != 'N' AND etm.active != 0  \n" + 
//			"AND e.employmentstatus != 'InActive'  \n" + 
//			"AND e.billableType = 'Bench' AND (p.poProjectType like 'FIXED%COST' OR p.poProjectType like '%TNM%') \n"+
//			"AND e.empId NOT BETWEEN 1 AND 6")
//	public List<RMGFlatEmployeeProjectTeamDTO> getExceptionEmployeeReport();
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO( \n"+
			"e.employeementId, e.name, d.name ,e.billableType, p.projectId,p.projectName, p.clientName,  \n" + 
			"p.apmosysRM ,p.clientRM ,p.poNo ,p.poProjectType ,p.poStartDate ,p.poEndDate )  \n" + 
			"from Project p  \n" +
			"inner join Team t on p.projectId = t.projectId  \n" + 
			"inner join EmployeeTeamMap etm on t.teamId = etm.teamId  \n" + 
			"inner join Employee e on e.empId = etm.empId  \n" + 
			"inner join JobRole jr on e.jobRoleId = jr.jobRoleId  \n" + 
			"inner join Department d on d.deptId = jr.deptId  \n" + 
			"where p.active = 'true' and t.isActive != 'N' and etm.active != 0  \n" + 
			"and e.employmentstatus != 'InActive'  \n" + 
			"and e.billableType = 'Bench' and (p.poProjectType like 'FIXED%COST' OR p.poProjectType like '%TNM%') and d.deptId IN :deptIds AND e.empId NOT BETWEEN 1 AND 6")
	public List<RMGFlatEmployeeProjectTeamDTO> getExceptionEmployeeReportInDepartments(List<Long> deptIds);
	
//	@Query(value="SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO( \n"+
//			"e.employeementId, e.name, d.name ,e.billableType, p.projectId,p.projectName, p.clientName,  \n" + 
//			"p.apmosysRM ,p.clientRM ,p.poNo ,p.poProjectType ,p.poStartDate ,p.poEndDate )  \n" + 
//			"from Project p  \n" +
//			"inner join Team t on p.projectId = t.projectId  \n" + 
//			"inner join EmployeeTeamMap etm on t.teamId = etm.teamId  \n" + 
//			"inner join Employee e on e.empId = etm.empId  \n" + 
//			"inner join JobRole jr on e.jobRoleId = jr.jobRoleId  \n" + 
//			"inner join Department d on d.deptId = jr.deptId  \n" + 
//			"where p.active = 'true' and t.isActive != 'N' and etm.active != 0  \n" + 
//			"and e.employmentstatus != 'InActive'  \n" + 
//			"and e.billableType = 'Bench' and (p.poProjectType like 'FIXED%COST' OR p.poProjectType like '%TNM%') and d.deptId IN :deptIds AND e.empId NOT BETWEEN 1 AND 6")
//	public List<RMGFlatEmployeeProjectTeamDTO> getExceptionEmployeeReportInDepartments(List<Long> deptIds);
	
	
	@Query(value = "SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO( \n"+
			"e.employeementId, e.name, d.name ,e.billableType, p.projectId,p.projectName, p.clientName,  \n" + 
			"p.apmosysRM ,p.clientRM ,p.poNo ,p.poProjectType ,p.poStartDate ,p.poEndDate )  \n" + 
			"from Project p  \n" +
			"inner join Team t on p.projectId = t.projectId  \n" + 
			"inner join EmployeeTeamMap etm on t.teamId = etm.teamId  \n" + 
			"inner join Employee e on e.empId = etm.empId  \n" + 
			"inner join JobRole jr on e.jobRoleId = jr.jobRoleId  \n" + 
			"inner join Department d on d.deptId = jr.deptId  \n" + 
			"where p.active = 'true' and t.isActive != 'N' and etm.active != 0  \n" + 
			"and e.employmentstatus != 'InActive'  \n" + 
			"and e.billableType = 'Bench' and (p.poProjectType like 'FIXED%COST' OR p.poProjectType like '%TNM%') \n"
			+ "and d.deptId =:deptIds AND e.empId NOT BETWEEN 1 AND 6")
	List <RMGFlatEmployeeProjectTeamDTO>  getExceptionEmployeeReportInDepartment(Long deptId);
	
	@Query(value = "SELECT new com.apmosys.employeeportal.dto.ProjectFetchDTO( \n"
			+ "p.projectId, p.createdOn, p.projectName, p.state, p.clientId, p.poProjectId, p.active, \n"
			+ "p.syncProject, p.createdBy, p.updatedBy, p.updatedOn, p.isDraftProject, p.poEndDate, p.poNo, \n"
			+ "p.poProjectType, p.poStartDate, p.apmosysRM, p.clientRM, p.deptId, p.isRenewable, p.status,\n"
			+ "p.apmosysRmEmail, p.projectCompletionDate, p.projectStatus, p.internalProjectType,c.clientName,\n"
			+ "CASE \n"
			+ "	 WHEN p.isDraftProject = 'true' THEN 'Pending For Approval' \n"
			+ "	 WHEN p.isDraftProject = 'false' THEN 'Approved' \n"
			+ "	 WHEN p.isDraftProject = 'Rejected' THEN 'Rejected' \n"
			+ "	 WHEN p.isDraftProject = 'Completed' THEN 'Completed' \n"
			+  " WHEN p.isDraftProject IS NULL THEN 'Not Started' "
			+ "	 ELSE 'Un Mentioned Test Data' \n"
			+ "END, \n"
			+ "CASE \n"
			+ "  WHEN p.poProjectId IS NOT NULL THEN CONCAT('po', p.poProjectId) \n"
			+ "  ELSE CONCAT('', p.projectId) \n"
			+ "END ) \n"
			+ "FROM Project p \n"
			+ "LEFT JOIN Client c ON c.clientId = p.clientId \n"
//			+ "INNER JOIN Team t ON t.projectId = p.projectId \n"
//			+ "INNER JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId \n"
//			+ "INNER JOIN Employee e ON e.empId = etm.empId \n"
//			+ "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId \n"
//			+ "INNER JOIN Department d ON d.deptId = jr.deptId \n"
			+ "WHERE p.active = 'true' \n"
//			+ "AND  e.employmentstatus != 'InActive' \n"
//			+ "AND d.dept_id IN (:deptIds) \n"
//			+ "AND (:deptIds IS NULL OR d.dept_id IN (:deptIds)) \n"
           + "AND (:isProjectId IS FALSE OR p.projectId IN (:projectId)) "
           + "AND (:approvalCheck IS FALSE OR p.isDraftProject = :approvalStatus) "
           + "AND (:projectStatus IS NULL OR p.projectStatus = :projectStatus) "
            + "AND (:status IS NULL OR p.status = :status) "
            + "AND ( :approvalStatus <> 'All' OR p.isDraftProject IS NOT NULL )")
	List<ProjectFetchDTO> getAllActiveProjectList(@Param("projectStatus")String projectStatus, @Param("status")String status, @Param("approvalStatus")String approvalStatus, Set<Integer> projectId, boolean isProjectId,boolean approvalCheck);


	
//	@Query(value="SELECT DISTINCT new com.apmosys.employeeportal.dto.ProjectFetchDTO( \n"
//			+ "			p.projectId, p.createdOn, p.projectName, p.state, p.clientId, p.poProjectId, p.active, \n"
//			+ "			p.syncProject, p.createdBy, p.updatedBy, p.updatedOn, p.isDraftProject, p.poEndDate, p.poNo, \n"
//			+ "			p.poProjectType, p.poStartDate, p.apmosysRM, p.clientRM, p.deptId, p.isRenewable, p.status,\n"
//			+ "			p.apmosysRmEmail, p.projectCompletionDate, p.projectStatus, p.internalProjectType,c.clientName,\n"
//			+ "CASE \n"
//			+ "	 WHEN p.isDraftProject = 'true' THEN 'Pending For Approval' \n"
//			+ "	 WHEN p.isDraftProject = 'false' THEN 'Approved' \n"
//			+ "	 WHEN p.isDraftProject = 'Rejected' THEN 'Rejected' \n"
//			+ "	 WHEN p.isDraftProject = 'Completed' THEN 'Completed' \n"
//			+ "	 WHEN p.isDraftProject = null THEN 'Not Started' \n"
//			+ "	 ELSE 'Un Mentioned Test Data' \n"
//			+ "END, \n"
//			+ "CASE \n"
//			+ "  WHEN p.poProjectId IS NOT NULL THEN CONCAT('po', p.poProjectId) \n"
//			+ "  ELSE CONCAT('', p.projectId) \n"
//			+ "END ) \n"
//			+ "			FROM Project p\n"
//			+ "			LEFT JOIN Client c ON c.clientId = p.clientId\n"
//			+ "			inner join ProjectDepartmentMap pdm on pdm.projectId = p.projectId\n"
//			+ "			WHERE p.active = 'true'\n"
//			+ "			AND p.isDraftProject is null \n"
//			+ "			and (p.status != 'Completed' or p.projectStatus = 'Not Started') and pdm.deptId IN :deptIds")
//	List<ProjectFetchDTO> getAllNotStartedProjects(@Param("deptIds") List<Long> deptIds);
	
	@Query(value="SELECT  distinct new com.apmosys.employeeportal.dto.ProjectFetchDTO(\n"
			+ "			p.projectId, p.createdOn, p.projectName, p.state, p.clientId, p.poProjectId, p.active, \n"
			+ "			p.syncProject, p.createdBy, p.updatedBy, p.updatedOn, p.isDraftProject, p.poEndDate, p.poNo, \n"
			+ "			p.poProjectType, p.poStartDate, p.apmosysRM, p.clientRM, p.deptId, p.isRenewable, p.status,\n"
			+ "			p.apmosysRmEmail, p.projectCompletionDate, p.projectStatus, p.internalProjectType,c.clientName,\n"
			+ "			CASE \n"
			+ "				 WHEN p.isDraftProject = 'true' THEN 'Pending For Approval' \n"
			+ "				 WHEN p.isDraftProject = 'false' THEN 'Approved' \n"
			+ "				 WHEN p.isDraftProject = 'Rejected' THEN 'Rejected' \n"
			+ "				 WHEN p.isDraftProject = 'Completed' THEN 'Completed' \n"
			+ "	 WHEN 			  p.isDraftProject IS NULL THEN 'Not Started'\n"
			+ "				 ELSE 'Un Mentioned Test Data' \n"
			+ "			END, \n"
			+ "			CASE \n"
			+ "			  WHEN p.poProjectId IS NOT NULL THEN CONCAT('po', p.poProjectId) \n"
			+ "			  ELSE CONCAT('', p.poProjectId) \n"
			+ "			END AS projectViewId )\n"
			+ "			FROM Project p   \n"
			+ "			INNER JOIN ProjectDepartmentMap pdm on p.projectId = pdm.projectId\n"
			+ "            LEFT JOIN Client c ON c.clientId = p.clientId \n"
			+ "			where p.active= 'true' and p.isDraftProject is null \n"
			+ "			and (p.status != 'Completed' or p.status is null)\n"
			+ "			and not exists (select 1 from Team t where t.projectId = p.projectId) \n"
//			+ "			and date(p.poEndDate) > curdate()\n"
			+ "			and (p.internalProjectType is not null or date(p.poEndDate)>curdate())\n"
			+ "			  and (pdm.deptId IN (:deptIds))  ")
	List<ProjectFetchDTO> getAllNotStartedProjects(@Param("deptIds") List<Long> deptIds);
//	@Query(value = "SELECT \n"
//			+ "    distinct p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
//			+ "    p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
//			+ "    p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
//			+ "    p.apmosys_rm_email, p.project_completion_date, p.project_status, p.internal_project_type,c.client_name, \n"
//			+ "    CASE\n"
//			+ "        WHEN p.is_draft_project = 'true' THEN 'Pending For Approval'\n"
//			+ "        WHEN p.is_draft_project = 'false' THEN 'Approved'\n"
//			+ "        WHEN p.is_draft_project = 'Rejected' THEN 'Rejected'\n"
//			+ "        WHEN p.is_draft_project = 'Completed' THEN 'Completed'\n"
//			+ "        ELSE 'Un Mentioned Test Data'\n"
//			+ "    END AS draftStatus,\n"
//			+ " CASE \n"
//		    + "  WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
//		    + "  ELSE CAST(p.project_id AS CHAR) \n"
//		    + "  END AS projectViewId \n"
//			+ "FROM projects p\n"
//			+ "LEFT JOIN project_temp pt ON p.po_project_id = pt.po_project_id\n"
//			+ "LEFT JOIN clients c on p.client_id = c.client_id \n"
//			+ "INNER JOIN teams t on p.project_id = t.project_id\n"
//			+ "INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id\n"
//			+ "INNER JOIN employee e on e.emp_id = etm.emp_id \n"
//			+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
//			+ "INNER JOIN department d on d.dept_id = jr.dept_id\n"
//			+ "WHERE p.active = 'true' \n"
//			+ " AND e.employmentstatus != 'InActive' \n"
//			+ "AND d.dept_id IN (:deptIds) \n"
////			+ "AND (:deptIds IS NULL OR d.dept_id IN (:deptIds)) \n"
//			+ "AND (:approvalStatus IS NULL OR p.is_draft_project = :approvalStatus ) \n"
//			+ "AND (:projectStatus IS NULL OR p.project_status = :projectStatus )"
//			+ "AND (:status IS NULL OR p.status = :status )",
//	       nativeQuery = true)
//	List<Object[]> getAllActiveCompletedProjectList(@Param("deptIds") List<Long> deptIds,@Param("projectStatus")String projectStatus, @Param("status")String status, @Param("approvalStatus")String approvalStatus);
	
	@Query(value = "SELECT new com.apmosys.employeeportal.dto.ProjectFetchDTO( \n"
			+ "p.projectId, p.createdOn, p.projectName, p.state, p.clientId, p.poProjectId, p.active, \n"
			+ "p.syncProject, p.createdBy, p.updatedBy, p.updatedOn, p.isDraftProject, p.poEndDate, p.poNo, \n"
			+ "p.poProjectType, p.poStartDate, p.apmosysRM, p.clientRM, p.deptId, p.isRenewable, p.status,\n"
			+ "p.apmosysRmEmail, p.projectCompletionDate, p.projectStatus, p.internalProjectType,c.clientName,\n"
			+ "CASE \n"
			+ "	 WHEN p.isDraftProject = 'true' THEN 'Pending For Approval' \n"
			+ "	 WHEN p.isDraftProject = 'false' THEN 'Approved' \n"
			+ "	 WHEN p.isDraftProject = 'Rejected' THEN 'Rejected' \n"
			+ "	 WHEN p.isDraftProject = 'Completed' THEN 'Completed' \n"
			+ "	 WHEN p.isDraftProject = null THEN 'Not Started' \n"
			+ "	 ELSE 'Un Mentioned Test Data' \n"
			+ "END, \n"
			+ "CASE \n"
			+ "  WHEN p.poProjectId IS NOT NULL THEN CONCAT('po', p.poProjectId) \n"
			+ "  ELSE CONCAT('', p.projectId) \n"
			+ "END ) \n"
			+ "FROM Project p \n"
			+ "LEFT JOIN Client c ON c.clientId = p.clientId \n"
//			+ "INNER JOIN Team t ON t.projectId = p.projectId \n"
//			+ "INNER JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId \n"
//			+ "INNER JOIN Employee e ON e.empId = etm.empId \n"
//			+ "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId \n"
//			+ "INNER JOIN Department d ON d.deptId = jr.deptId \n"
			+ "WHERE p.active = 'true' \n"
//			+ "AND e.employmentstatus != 'InActive' \n"
//			+ "AND d.deptId IN (:deptIds) \n"
			+ "AND (:approvalStatus IS NULL OR p.isDraftProject = :approvalStatus ) \n"
			+ "AND (:projectStatus IS NULL OR p.projectStatus = :projectStatus )"
			+ "AND (:status IS NULL OR p.status = :status )")
			List<ProjectFetchDTO> getAllActiveCompletedProjectList(@Param("projectStatus")String projectStatus, @Param("status")String status, @Param("approvalStatus")String approvalStatus);
	
	
//	@Query(value = "SELECT COUNT(DISTINCT p.project_id)\n"
//			+ "FROM projects p \n"
//			+ "INNER JOIN teams t on p.project_id = t.project_id \n"
//			+ "INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id \n"
//			+ "INNER JOIN employee e on e.emp_id = etm.emp_id \n"
//			+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id \n"
//			+ "INNER JOIN department d on d.dept_id = jr.dept_id  \n"
//			+ "INNER JOIN project_department_map pdm on p.project_id = pdm.project_id\n"
//			+ "WHERE p.active = 'true' AND t.is_active = 'Y' and etm.active != 0\n"
//			+ "and p.is_draft_project = 'false'\n"
//			+ "and (:deptIds is null or pdm.dept_id IN (:deptIds));")
//	Integer getAllActiveProjecCountstList(@Param("approvalStatus")String approvalStatus, @Param("projectIds")Set<Integer> projectIds,@Param("projectFilter") Boolean projectFilter);

	@Query(value=" select  count( Distinct p.project_id) from projects p \n"
			+ "inner join project_department_map pdm on pdm.project_id = p.project_id \n"
			+ "where p.active= 'true' and p.is_draft_project = 'true'\n"
			+ "and ( pdm.dept_id IN (:deptIds))\n"
			+ ";" , nativeQuery = true)
	Integer getAllPendingForApprovalProjectCount(List<Long> deptIds);
	
	@Query(value="SELECT COUNT(DISTINCT p.project_id)\n"
			+ "FROM projects p \n"
			+ "INNER JOIN teams t on p.project_id = t.project_id \n"
			+ "INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id \n"
			+ "INNER JOIN employee e on e.emp_id = etm.emp_id \n"
			+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id \n"
			+ "INNER JOIN department d on d.dept_id = jr.dept_id  \n"
			+ "INNER JOIN project_department_map pdm on p.project_id = pdm.project_id\n"
			+ "WHERE p.active = 'true' AND t.is_active = 'Y' and etm.active != 0\n"
			+ "and p.is_draft_project = 'false'\n"
			+ "and ( pdm.dept_id IN (:deptIds))" , nativeQuery = true)
	Integer getAllApprovedProjectCount(List<Long> deptIds);
	
	@Query(value=" select  count( Distinct p.project_id) from projects p \n"
			+ "inner join project_department_map pdm on pdm.project_id = p.project_id \n"
			+ "where p.active= 'true' and upper(p.is_draft_project) = 'REJECTED'\n"
			+ "and ( pdm.dept_id IN (:deptIds));" , nativeQuery = true)
	Integer getAllRejectedProjectCount(List<Long> deptIds);
	
	@Query(value="WITH\n"
			+ "			    Approved_Count AS (\n"
			+ "			        SELECT COUNT(DISTINCT p.project_id) AS ct\n"
			+ "			        FROM projects p \n"
			+ "			        INNER JOIN teams t ON p.project_id = t.project_id \n"
			+ "			        INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
			+ "			        INNER JOIN project_department_map pdm ON p.project_id = pdm.project_id\n"
			+ "			        WHERE p.active = 'true' \n"
			+ "			          AND t.is_active = 'Y' \n"
			+ "			          AND etm.active != 0\n"
			+ "			          AND p.is_draft_project = 'false'\n"
			+ "			          AND (pdm.dept_id IN (:deptIds))\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    Not_Started_Count AS (\n"
			+ "			        SELECT COUNT(DISTINCT p.project_id) AS ct\n"
			+ "			        FROM projects p \n"
			+ "			        INNER JOIN project_department_map pdm ON pdm.project_id = p.project_id \n"
			+ "			        WHERE p.active = 'true' \n"
			+ "			          AND p.is_draft_project IS NULL \n"
			+ "			          AND (p.status != 'Completed' OR p.status IS NULL)\n"
			+ "			          AND NOT EXISTS (SELECT 1 FROM teams t WHERE t.project_id = p.project_id) \n"
			+ "			          AND (p.internal_project_type IS NOT NULL OR DATE(p.po_end_date) > CURDATE())\n"
			+ "			          AND (pdm.dept_id IN (:deptIds))\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    Pending_Approval_Count AS (\n"
			+ "			        SELECT COUNT(DISTINCT p.project_id) AS ct\n"
			+ "			        FROM projects p \n"
			+ "			        INNER JOIN project_department_map pdm ON pdm.project_id = p.project_id \n"
			+ "			        WHERE p.active = 'true' \n"
			+ "			          AND p.is_draft_project = 'true'\n"
			+ "			          AND (pdm.dept_id IN (:deptIds))\n"
			+ "			    ),\n"
			+ "			\n"
			+ "			    Rejected_Count AS (\n"
			+ "			        SELECT COUNT(DISTINCT p.project_id) AS ct\n"
			+ "			        FROM projects p \n"
			+ "			        INNER JOIN project_department_map pdm ON pdm.project_id = p.project_id \n"
			+ "			        WHERE p.active = 'true' \n"
			+ "			          AND UPPER(p.is_draft_project) = 'REJECTED'\n"
			+ "			          AND (pdm.dept_id IN (:deptIds))\n"
			+ "			    )\n"
			+ "			\n"
			+ "			SELECT\n"
			+ "			    (SELECT ct FROM Approved_Count) +\n"
			+ "			    (SELECT ct FROM Not_Started_Count) +\n"
			+ "			    (SELECT ct FROM Pending_Approval_Count) +\n"
			+ "			    (SELECT ct FROM Rejected_Count) AS total_project_count;" , nativeQuery = true)
	Integer getAllTotalProjectCount(List<Long> deptIds);
	
	
//	@Query(value="select  count( Distinct p.projectId) from Project p \n"
//			+ " inner join ProjectDepartmentMap pdm on pdm.projectId = p.projectId \n"
//			+ " where p.active= 'true' and p.isDraftProject is null "
//			+ "and (p.status != 'Completed' or "
//			+ "p.projectStatus = 'Not Started') "
//			+ "and pdm.deptId IN :deptIds")
//	Integer getAllNotStartedProjectCountInDept(List<Long> deptIds);
	
	@Query(value="select  count( Distinct p.projectId) from Project p \n"
			+ "	inner join ProjectDepartmentMap pdm on pdm.projectId = p.projectId \n"
			+ "	where p.active= 'true' and p.isDraftProject is null \n"
			+ "	and (p.status != 'Completed' or p.status is null)\n"
			+ "	and not exists (select 1 from Team t where t.projectId = p.projectId) \n"
//			+ "	and date(p.poEndDate) > curdate()\n"
			+ "	and (p.internalProjectType is not null or date(p.poEndDate)>curdate())\n"
			+ "	and (pdm.deptId IN (:deptIds))")
	Integer getAllNotStartedProjectCountInDept(List<Long> deptIds);
	

	
	@Query(nativeQuery = true,value ="select distinct count(*) from projects where active= 'true' and is_draft_project is null and status != 'Completed'")
	Integer getAllNotStartedProjectCount();
	
	
	  
	@Query(value = "SELECT COUNT(DISTINCT p.projectId)\n"
			+ "FROM Project p \n"
			+ "INNER JOIN Team t on p.projectId = t.projectId \n"
			+ "INNER JOIN EmployeeTeamMap etm on etm.teamId = t.teamId \n"
			+ "INNER JOIN Employee e on e.empId = etm.empId \n"
			+ "INNER JOIN JobRole jr on e.jobRoleId = jr.jobRoleId \n"
			+ "INNER JOIN Department d on d.deptId = jr.deptId \n"
			+ "WHERE p.active = 'true' \n"
			+ "AND t.isActive != 'N' \n"
			+ "AND etm.active != 0 \n"
			+ "AND e.employmentstatus != 'InActive' \n"
//			+ "AND d.dept_id IN (:deptIds) \n"
//			+ "AND (:isDeptFilter = false OR d.deptId IN :deptIds) \n"
			+ "AND (:projectStatus IS NULL OR p.projectStatus = :projectStatus )\n"
			+ "AND (:projectFilter = false OR p.projectId in (:projectIds))")
	Integer getAllCompleteProjectInIshineCountstList(@Param("projectStatus")String projectStatus,@Param("projectIds")Set<Integer> projectIds,@Param("projectFilter") Boolean projectFilter);

	@Query(value ="SELECT COUNT(DISTINCT p.projectId)\n"
			+ "FROM Project p \n"
//			+ "LEFT JOIN Team t on p.projectId = t.projectId \n"
//			+ "INNER JOIN EmployeeTeamMap etm on etm.teamId = t.teamId \n"
//			+ "INNER JOIN Employee e on e.empId = etm.empId \n"
//			+ "INNER JOIN JobRole jr on e.jobRoleId = jr.jobRoleId \n"
//			+ "INNER JOIN Department d on d.deptId = jr.deptId \n"
			+ "WHERE p.active = 'true' \n"
//			+ "AND t.isActive != 'N' \n"
//			+ "AND etm.active != 0 \n"
//			+ "AND e.employmentstatus != 'InActive' \n"
//			+ "AND d.dept_id IN (:deptIds) \n"
//			+ "AND (:isDeptFilter = false OR d.deptId IN :deptIds) \n"
			+ "AND p.isDraftProject IS NOT NULL \n"
			+ "AND (:projectFilter=false OR p.projectId in (:projectIds)) \n"
			+ "AND p.status = 'Completed' ")
	Integer getAllCompleteProjectInShankhCountstList( @Param("projectIds")Set<Integer> projectIds,@Param("projectFilter") Boolean projectFilter);
	
	@Query(value="SELECT COUNT(DISTINCT p.projectId)\n"
			+ "FROM Project p \n"
			+ "INNER JOIN Team t on p.projectId = t.projectId \n"
			+ "INNER JOIN EmployeeTeamMap etm on etm.teamId = t.teamId \n"
			+ "INNER JOIN Employee e on e.empId = etm.empId \n"
			+ "INNER JOIN JobRole jr on e.jobRoleId = jr.jobRoleId \n"
//			+ "INNER JOIN Department d on d.deptId = jr.deptId \n"
			+ "WHERE p.active = 'true' \n"
			+ "AND t.isActive != 'N' \n"
			+ "AND etm.active != 0 \n"
			+ "AND e.employmentstatus != 'InActive' \n"
//			+ "AND d.dept_id IN (:deptIds) \n"
//			+ "AND (:isDeptFilter = false OR d.deptId IN :deptIds) \n"
			+ "	AND  p.status = 'Completed' \n"
			+ "AND (:projectFilter=false OR p.projectId in (:projectIds))")
	Integer completedInSankhButTeamMapped(@Param("projectIds")Set<Integer> projectIds,@Param("projectFilter") Boolean projectFilter);
	 
	
//	@Query(value="SELECT \n"
//			+ "    distinct p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
//			+ "    p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
//			+ "    p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
//			+ "    p.apmosys_rm_email, p.project_completion_date, p.project_status, p.internal_project_type,c.client_name, \n"
//			+ "    CASE\n"
//			+ "        WHEN p.is_draft_project = 'true' THEN 'Pending For Approval'\n"
//			+ "        WHEN p.is_draft_project = 'false' THEN 'Approved'\n"
//			+ "        WHEN p.is_draft_project = 'Rejected' THEN 'Rejected'\n"
//			+ "        WHEN p.is_draft_project = 'Completed' THEN 'Completed'\n"
//			+ "        ELSE 'Un Mentioned Test Data'\n"
//			+ "    END AS draftStatus,\n"
//			+ " CASE \n"
//		    + "  WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
//		    + "  ELSE CAST(p.project_id AS CHAR) \n"
//		    + "  END AS projectViewId \n"
//			+ "			FROM projects p\n"
//			+ "			LEFT JOIN project_temp pt ON p.po_project_id = pt.po_project_id\n"
//			+ "			LEFT JOIN clients c on p.client_id = c.client_id \n"
//			+ "			INNER JOIN teams t on p.project_id = t.project_id\n"
//			+ "			INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id\n"
//			+ "			INNER JOIN employee e on e.emp_id = etm.emp_id \n"
//			+ "			INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
//			+ "			INNER JOIN department d on d.dept_id = jr.dept_id\n"
//			+ "			WHERE p.active = 'true' \n"
//			+ "            AND t.is_active != 'N' \n"
//			+ "			AND etm.active != 0 \n"
//			+ "            AND e.employmentstatus != 'InActive' \n"
//			+ "			AND d.dept_id IN (:deptIds) \n"
//			+ "			AND  p.status = 'Completed' ", nativeQuery=true)
//	List<Object[]> completedInSankhButTeamMappedList(@Param("deptIds") List<Long> deptIds);
	
	@Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.ProjectFetchDTO( \n"
			+ "p.projectId, p.createdOn, p.projectName, p.state, p.clientId, p.poProjectId, p.active, \n"
			+ "p.syncProject, p.createdBy, p.updatedBy, p.updatedOn, p.isDraftProject, p.poEndDate, p.poNo, \n"
			+ "p.poProjectType, p.poStartDate, p.apmosysRM, p.clientRM, p.deptId, p.isRenewable, p.status,\n"
			+ "p.apmosysRmEmail, p.projectCompletionDate, p.projectStatus, p.internalProjectType,c.clientName, \n"
			+ "CASE \n"
			+ "	 WHEN p.isDraftProject = 'true' THEN 'Pending For Approval' \n"
			+ "	 WHEN p.isDraftProject = 'false' THEN 'Approved' \n"
			+ "	 WHEN p.isDraftProject = 'Rejected' THEN 'Rejected' \n"
			+ "	 WHEN p.isDraftProject = 'Completed' THEN 'Completed' \n"
			+ "	 WHEN p.isDraftProject IS NULL THEN 'Not Started' \n"
			+ "	 ELSE 'Un Mentioned Test Data' \n"
			+ "END, \n" 
			+ "CASE \n"
			+ "  WHEN p.poProjectId IS NOT NULL THEN CONCAT('po', p.poProjectId) \n"
			+ "  ELSE CONCAT('', p.projectId) \n"
			+ "END ) \n"
			+ "FROM Project p \n"
			+ "LEFT JOIN Client c on p.clientId = c.clientId \n"
			+ "INNER JOIN Team t on p.projectId = t.projectId \n"
			+ "INNER JOIN EmployeeTeamMap etm on etm.teamId = t.teamId \n"
			+ "INNER JOIN Employee e on e.empId = etm.empId \n"
			+ "INNER JOIN JobRole jr on e.jobRoleId = jr.jobRoleId \n"
//			+ "INNER JOIN Department d on d.deptId = jr.deptId \n"
			+ "WHERE p.active = 'true' \n"
			+ "AND t.isActive != 'N' \n"
			+ "AND etm.active != 0 \n"
			+ "AND e.employmentstatus != 'InActive' \n"
//			+ "AND d.dept_id IN (:deptIds) \n"
//			+ "AND (:isDeptFilter = false OR d.deptId IN :deptIds) \n"
			+ "	AND  p.status = 'Completed' \n"
			+ "AND (:projectFilter=false OR p.projectId in (:projectIds))")
	List<ProjectFetchDTO> completedInSankhButTeamMappedList(@Param("projectIds")Set<Integer> projectIds,@Param("projectFilter") Boolean projectFilter);
	
	@Query(nativeQuery=true, value="SELECT \n"
			+ "    distinct p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
			+ "    p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
			+ "    p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
			+ "    p.apmosys_rm_email, p.project_completion_date, p.project_status, p.internal_project_type,c.client_name, \n"
			+ "    CASE\n"
			+ "        WHEN p.is_draft_project = 'true' THEN 'Pending For Approval'\n"
			+ "        WHEN p.is_draft_project = 'false' THEN 'Approved'\n"
			+ "        WHEN p.is_draft_project = 'Rejected' THEN 'Rejected'\n"
			+ "        WHEN p.is_draft_project = 'Completed' THEN 'Completed'\n"
			+ "        ELSE 'Un Mentioned Test Data'\n"
			+ "    END AS draftStatus,\n"
			+ " CASE \n"
		    + "  WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
		    + "  ELSE CAST(p.project_id AS CHAR) \n"
		    + "  END AS projectViewId \n"
			+ "FROM projects p\n"
			+ "	LEFT JOIN clients c on p.client_id = c.client_id \n"
			+ "JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "WHERE p.active = 'true' AND pm.project_manager_id  =:empId\n"
			+ "UNION\n"
			+ "SELECT \n"
			+ "    distinct p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
			+ "    p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
			+ "    p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
			+ "    p.apmosys_rm_email, p.project_completion_date, p.project_status, p.internal_project_type,c.client_name, \n"
			+ "    CASE\n"
			+ "        WHEN p.is_draft_project = 'true' THEN 'Pending For Approval'\n"
			+ "        WHEN p.is_draft_project = 'false' THEN 'Approved'\n"
			+ "        WHEN p.is_draft_project = 'Rejected' THEN 'Rejected'\n"
			+ "        WHEN p.is_draft_project = 'Completed' THEN 'Completed'\n"
			+ "        ELSE 'Un Mentioned Test Data'\n"
			+ "    END AS draftStatus,\n"
			+ " CASE \n"
		    + "  WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
		    + "  ELSE CAST(p.project_id AS CHAR) \n"
		    + "  END AS projectViewId \n"
			+ "FROM projects p\n"
			+ "JOIN project_overhead_mapping po ON p.project_id = po.project_id\n"
			+ "	LEFT JOIN clients c on p.client_id = c.client_id \n"
			+ "WHERE p.active = 'true' AND po.project_overhead_id  =:empId\n"
			+ "\n"
			+ "UNION\n"
			+ "SELECT \n"
			+ "    distinct p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
			+ "    p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
			+ "    p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
			+ "    p.apmosys_rm_email, p.project_completion_date, p.project_status, p.internal_project_type,c.client_name, \n"
			+ "    CASE\n"
			+ "        WHEN p.is_draft_project = 'true' THEN 'Pending For Approval'\n"
			+ "        WHEN p.is_draft_project = 'false' THEN 'Approved'\n"
			+ "        WHEN p.is_draft_project = 'Rejected' THEN 'Rejected'\n"
			+ "        WHEN p.is_draft_project = 'Completed' THEN 'Completed'\n"
			+ "        ELSE 'Un Mentioned Test Data'\n"
			+ "    END AS draftStatus,\n"
			+ " CASE \n"
		    + "  WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
		    + "  ELSE CAST(p.project_id AS CHAR) \n"
		    + "  END AS projectViewId \n"
			+ "FROM projects p\n"
			+ "	LEFT JOIN clients c on p.client_id = c.client_id \n"
			+ "JOIN teams t ON p.project_id = t.project_id\n"
			+ "WHERE p.active = 'true' AND t.is_active = 'Y' AND t.spoc_id  =:empId\n"
			+ "UNION\n"
			+ "SELECT \n"
			+ "    distinct p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
			+ "    p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
			+ "    p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
			+ "    p.apmosys_rm_email, p.project_completion_date, p.project_status, p.internal_project_type,c.client_name, \n"
			+ "    CASE\n"
			+ "        WHEN p.is_draft_project = 'true' THEN 'Pending For Approval'\n"
			+ "        WHEN p.is_draft_project = 'false' THEN 'Approved'\n"
			+ "        WHEN p.is_draft_project = 'Rejected' THEN 'Rejected'\n"
			+ "        WHEN p.is_draft_project = 'Completed' THEN 'Completed'\n"
			+ "        ELSE 'Un Mentioned Test Data'\n"
			+ "    END AS draftStatus,\n"
			+ " CASE \n"
		    + "  WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
		    + "  ELSE CAST(p.project_id AS CHAR) \n"
		    + "  END AS projectViewId \n"
			+ "FROM projects p\n"
			+ "	LEFT JOIN clients c on p.client_id = c.client_id \n"
			+ "JOIN teams t ON p.project_id = t.project_id\n"
			+ "WHERE p.active = 'true' AND t.is_active = 'Y' AND t.team_lead_id =:empId\n"
			+ "")
	List<Object[]> getAllDataListOfProjectsForPMSpocTLOverHeads(@Param("empId") Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getPreviousDefaultProjectDetails(Long empId);
//	
//	@Query(nativeQuery = true)
//	public List<Object[]> getProjectDetailsForBulkDefaultUpdateBench();
	
	
	@Query(value = "Select distinct new com.apmosys.employeeportal.dto.GetProjectDetailsForBulkDefaultUpdateProjectDTO( p.projectId, p.projectName, t.teamId, t.teamName, r.resourceOverviewId,  \n" + 
			"r.count, r.department, r.experience, r.role) from Project p  \n" + 
			"left join Team t on t.projectId = p.projectId  \n" + 
			"left join EmployeeTeamMap etm on etm.teamId = t.teamId  \n" + 
			"left join ResourceRequirement r on r.projectId = p.projectId  \n" + 
			"where p.active = 'true' and t.isActive = 'Y' and etm.active != 0 and p.internalProjectType = 'Bench' ")
	public List<GetProjectDetailsForBulkDefaultUpdateProjectDTO> getProjectDetailsForBulkDefaultUpdateBench();
	
	
//	@Query(nativeQuery = true)
//	public List<Object[]> getProjectDetailsForBulkDefaultUpdateOther();

	
	
	
	@Query(value = "Select distinct new com.apmosys.employeeportal.dto.GetProjectDetailsForBulkDefaultUpdateProjectDTO( p.projectId, p.projectName, t.teamId, t.teamName, r.resourceOverviewId, \n"  +
	 		"r.count, r.department,  r.experience, r.role) from Project p  \n" + 
	 		"left join Team t on t.projectId = p.projectId  \n" + 
	 		"left join EmployeeTeamMap etm on etm.teamId = t.teamId  \n" + 
	 		"left join ResourceRequirement r on r.projectId = p.projectId  \n" + 
	 		"where p.active = 'true' and t.isActive = 'Y' and etm.active != 0  \n" + 
	 		"and (p.internalProjectType is null or p.internalProjectType != 'Bench') ")
	public List<GetProjectDetailsForBulkDefaultUpdateProjectDTO> getProjectDetailsForBulkDefaultUpdateOther();

	
	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeInformationBulk(List<Long> empIds);
	
	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeInformationForDefaultProject(List<Long> empIds);
	
	@Query(nativeQuery = true)
	public List<Object[]> getBenchEmployeeMoreThan30Days();
	
	@Query(value = "SELECT DISTINCT p.project_id FROM projects p " +
            "JOIN project_manager_mapping pm ON p.project_id = pm.project_id " +
            "WHERE pm.project_manager_id = :empId AND p.active = 'true' AND p.po_project_id IS NULL", nativeQuery = true)
  Set<Integer> findActiveInternalProjectIdsByProjectManager(@Param("empId") Long empId);
	
	@Query(value = "SELECT DISTINCT p.project_id FROM projects p " +
            "JOIN project_overhead_mapping pom ON p.project_id = pom.project_id " +
            "WHERE pom.project_overhead_id = :empId AND p.active = 'true' AND p.po_project_id IS NULL", nativeQuery = true)
  Set<Integer> findActiveInternalProjectIdsByProjectOverhead(@Param("empId") Long empId);
	
	
	
	
	
	@Query(nativeQuery = true,value ="(\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NOT NULL AND pm.project_manager_id = :empId\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN project_overhead_mapping po ON p.project_id = po.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NOT NULL AND po.project_overhead_id = :empId\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN teams t ON p.project_id = t.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NOT NULL\n"
			+ "          AND t.is_active = 'Y' AND t.spoc_id = :empId\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN teams t ON p.project_id = t.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NOT NULL\n"
			+ "          AND t.is_active = 'Y' AND t.team_lead_id = :empId\n"
			+ "    )")
	Set<Integer> findShankhProjectsByManagerOverheadOrSpocOrTeamLead(@Param("empId") Long empId);
	
	@Query(nativeQuery = true,value ="(\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NULL AND pm.project_manager_id = :empId\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN project_overhead_mapping po ON p.project_id = po.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NULL AND po.project_overhead_id = :empId\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN teams t ON p.project_id = t.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NULL\n"
			+ "          AND t.is_active = 'Y' AND t.spoc_id = :empId\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN teams t ON p.project_id = t.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NULL\n"
			+ "          AND t.is_active = 'Y' AND t.team_lead_id = :empId\n"
			+ "    )")
	Set<Integer> findInternalProjectsByManagerOverheadOrSpocOrTeamLead(@Param("empId") Long empId);
	

	

	@Query(nativeQuery = true,value ="(\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "        WHERE p.active = 'true'  AND pm.project_manager_id = :empId\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN project_overhead_mapping po ON p.project_id = po.project_id\n"
			+ "        WHERE p.active = 'true' AND po.project_overhead_id = :empId\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN teams t ON p.project_id = t.project_id\n"
			+ "        WHERE p.active = 'true' \n"
			+ "          AND t.is_active = 'Y' AND t.spoc_id = :empId\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN teams t ON p.project_id = t.project_id\n"
			+ "        WHERE p.active = 'true'\n"
			+ "          AND t.is_active = 'Y' AND t.team_lead_id = :empId\n"
			+ "    )")
	Set<Integer> findAllShankhInternalProjectsByManagerOverheadOrSpocOrTeamLead(@Param("empId") Long empId);
	

	
	
	
	@Query("SELECT DISTINCT NEW com.apmosys.employeeportal.dto.SummaryChartDTO(p.projectName, COUNT(DISTINCT et.timesheetId) as totalTimesheetsFilled ) " +
		       "FROM Employee e " +
		       "INNER JOIN EmployeeTeamMap etm ON etm.empId = e.empId " +
		       "INNER JOIN Team t ON t.teamId = etm.teamId " +
		       "INNER JOIN Project p ON p.projectId = t.projectId " +
		       "INNER JOIN Timesheet et ON et.empId = e.empId " +
		       "INNER JOIN TimesheetActivityMap etam ON etam.timesheetId = et.timesheetId " +
		       "INNER JOIN Activity a ON etam.activityId = a.activityId AND t.teamId = a.teamId " +
		       "WHERE e.empId = :empId " +
		       "GROUP BY e.empId, p.projectName")
		public List<SummaryChartDTO> getProjectTimesheetSummaryByEmpId(@Param("empId") Long empId);
	
//	@Query("SELECT DISTINCT NEW com.apmosys.employeeportal.dto.EmployeeDTO(p.projectName, COUNT(DISTINCT et.timesheetId)) " +
//		       "FROM Employee e " +
//		       "INNER JOIN e.EmployeeTeamMap etm " +
//		       "INNER JOIN etm.Team t " +
//		       "INNER JOIN t.Project p " +
//		       "INNER JOIN e.Timesheet et " +
//		       "INNER JOIN et.TimesheetActivityMap etam " +
//		       "INNER JOIN etam.Activity a " +
//		       "WHERE e.empId = :empId AND t.teamId = a.teamId " +
//		       "GROUP BY e.empId, p.projectName")
//		public List<EmployeeDTO> getProjectTimesheetSummaryByEmpId(@Param("empId") Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getBenchEmployeeMoreThan30DaysInDeptIds(List<Long> deptIds);
	
	@Query(nativeQuery = true)
	public List<Object[]> getBenchEmployeeMoreThan30DaysInDeptId(Long deptId);
	
	@Query(nativeQuery = true,value="select client_name from clients where client_id = :clientId")
	public String getClientNameByClientId(Integer clientId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getProjectStatusByPoProjectId(Set<Long> poProjectId);

	@Query(value = "select p.* from project_manager_mapping pm " +
            "inner join projects p on pm.project_id = p.project_id " +
            "where pm.project_manager_id = :projectManagerId and pm.active = 1", 
    nativeQuery = true)
public List<Project> findProjectsOfProjectManager(Long projectManagerId);

	@Query(value = "select p.* from project_overhead_mapping po\n"
			+ "inner join projects p on po.project_id = p.project_id\n"
			+ "where po.project_overhead_id= :projectOverheadId and po.active =1 " , nativeQuery = true)
	public List<Project> findProjectOfProjectOverhead(Long projectOverheadId);

	
	
	@Query(value = "select p.* from teams t\n"
			+ "inner join projects p on t.project_id = p.project_id\n"
			+ "where t.team_lead_id= :teamLeadId and t.is_active = 'Y'" , nativeQuery = true)
	public List<Project> findProjectOfTeamLead(Long teamLeadId);

	
	@Query(value = "select p.* from teams t\n"
			+ "inner join projects p on t.project_id = p.project_id\n"
			+ "where t.spoc_id= :spocId and t.is_active = 'Y'" , nativeQuery = true)
	public List<Project> findProjectOfSpoc(Long spocId);
	
	@Query(value ="SELECT DISTINCT NEW com.apmosys.employeeportal.dto.ResourceManagementDTO(projectId,poProjectId,projectName)\n"
			+ "from Project where active = 'true' and poProjectId IS NOT NULL")
	public List<ResourceManagementDTO> getAllActivePOProjects();
	
	
	@Query(value="SELECT DISTINCT new com.apmosys.employeeportal.dto.ProjectFetchDTO( \n"
			+ "			p.projectId, p.createdOn, p.projectName, p.state, p.clientId, p.poProjectId, p.active, \n"
			+ "			p.syncProject, p.createdBy, p.updatedBy, p.updatedOn, p.isDraftProject, p.poEndDate, p.poNo, \n"
			+ "			p.poProjectType, p.poStartDate, p.apmosysRM, p.clientRM, p.deptId, p.isRenewable, p.status,\n"
			+ "			p.apmosysRmEmail, p.projectCompletionDate, p.projectStatus, p.internalProjectType,c.clientName,\n"
			+ "CASE \n"
			+ "	 WHEN p.isDraftProject = 'true' THEN 'Pending For Approval' \n"
			+ "	 WHEN p.isDraftProject = 'false' THEN 'Approved' \n"
			+ "	 WHEN p.isDraftProject = 'Rejected' THEN 'Rejected' \n"
			+ "	 WHEN p.isDraftProject = 'Completed' THEN 'Completed' \n"
			+ "	 WHEN p.isDraftProject = null THEN 'Not Started' \n"
			+ "	 ELSE 'Un Mentioned Test Data' \n"
			+ "END, \n"
			+ "CASE \n"
			+ "  WHEN p.poProjectId IS NOT NULL THEN CONCAT('po', p.poProjectId) \n"
			+ "  ELSE CONCAT('', p.projectId) \n"
			+ "END AS projectViewId) \n"
			+ "			FROM Project p\n"
			+ "			LEFT JOIN Client c ON c.clientId = p.clientId\n"
			+ "			inner join ProjectDepartmentMap pdm on pdm.projectId = p.projectId\n"
			+ "			WHERE p.projectStatus = 'Completed' and pdm.deptId IN :deptIds")
	List<ProjectFetchDTO> getAllCompletedProjectListInIshine(@Param("deptIds") List<Long> deptIds);
	
	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.ProjectFetchDTO(" +
		    "p.projectId, " +
		    "p.createdOn, " +
		    "p.projectName, " +
		    "p.state, " +
		    "p.clientId, " +
		    "p.poProjectId, " +
		    "p.active, " +
		    "p.syncProject, " +
		    "p.createdBy, " +
		    "p.updatedBy, " +
		    "p.updatedOn, " +
		    "p.isDraftProject, " +
		    "p.poEndDate, " +
		    "p.poNo, " +
		    "p.poProjectType, " +
		    "p.poStartDate, " +
		    "p.apmosysRM, " +
		    "p.clientRM, " +
		    "p.deptId, " +
		    "p.isRenewable, " +
		    "p.status, " +
		    "p.apmosysRmEmail, " +
		    "p.projectCompletionDate, " +
		    "p.projectStatus, " +
		    "p.internalProjectType, " +
		    "c.clientName, " +
		    "CASE " +
		        "WHEN p.isDraftProject = 'true' THEN 'Pending For Approval' " +
		        "WHEN p.isDraftProject = 'false' THEN 'Approved' " +
		        "WHEN p.isDraftProject = 'Rejected' THEN 'Rejected' " +
		        "WHEN p.isDraftProject = 'Completed' THEN 'Completed' " +
		        "WHEN p.isDraftProject IS NULL THEN 'Not Started' " +
		        "ELSE 'Un Mentioned Test Data' " +
		    "END, " +
		    "CASE " +
		        "WHEN p.poProjectId IS NOT NULL THEN CONCAT('po', CAST(p.poProjectId AS string)) " +
		        "ELSE CAST(p.poProjectId AS string) " +
		    "END AS projectViewId " +
		") " +
		"FROM Project p " +
		"INNER JOIN Team t ON p.projectId = t.projectId " +
		"INNER JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId " +
		"INNER JOIN Employee e ON e.empId = etm.empId " +
		"INNER JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId " +
		"INNER JOIN Department d ON d.deptId = jr.deptId " +
		"INNER JOIN ProjectDepartmentMap pdm ON p.projectId = pdm.projectId " +
		"LEFT JOIN Client c ON c.clientId = p.clientId " +
		"WHERE p.active = 'true' " +
		"AND t.isActive = 'Y' " +
		"AND etm.active != 0 " +
		"AND p.isDraftProject = 'false' " +
		"AND pdm.deptId IN :deptIds")
		List<ProjectFetchDTO> getAllApprovedProjectList(@Param("deptIds") List<Long> deptIds);
	
	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.ProjectFetchDTO(" +
		    "p.projectId, " +
		    "p.createdOn, " +
		    "p.projectName, " +
		    "p.state, " +
		    "p.clientId, " +
		    "p.poProjectId, " +
		    "p.active, " +
		    "p.syncProject, " +
		    "p.createdBy, " +
		    "p.updatedBy, " +
		    "p.updatedOn, " +
		    "p.isDraftProject, " +
		    "p.poEndDate, " +
		    "p.poNo, " +
		    "p.poProjectType, " +
		    "p.poStartDate, " +
		    "p.apmosysRM, " +
		    "p.clientRM, " +
		    "p.deptId, " +
		    "p.isRenewable, " +
		    "p.status, " +
		    "p.apmosysRmEmail, " +
		    "p.projectCompletionDate, " +
		    "p.projectStatus, " +
		    "p.internalProjectType, " +
		    "c.clientName, " +
		    "CASE " +
		        "WHEN p.isDraftProject = 'true' THEN 'Pending For Approval' " +
		        "WHEN p.isDraftProject = 'false' THEN 'Approved' " +
		        "WHEN p.isDraftProject = 'Rejected' THEN 'Rejected' " +
		        "WHEN p.isDraftProject = 'Completed' THEN 'Completed' " +
		        "WHEN p.isDraftProject IS NULL THEN 'Not Started' " +
		        "ELSE 'Un Mentioned Test Data' " +
		    "END, " +
		    "CASE " +
		        "WHEN p.poProjectId IS NOT NULL THEN CONCAT('po', CAST(p.poProjectId AS string)) " +
		        "ELSE CAST(p.poProjectId AS string) " +
		    "END" +
		") " +
		"FROM Project p " +
		"INNER JOIN ProjectDepartmentMap pdm ON p.projectId = pdm.projectId " +
		"LEFT JOIN Client c ON c.clientId = p.clientId " +
		"WHERE p.active = 'true' " +
		"AND UPPER(p.isDraftProject) = 'REJECTED' " +
		"AND (pdm.deptId IN :deptIds)")
		List<ProjectFetchDTO> getAllRejectedProjectList(@Param("deptIds") List<Long> deptIds);
	
	@Query("SELECT  distinct new com.apmosys.employeeportal.dto.ProjectFetchDTO(\n"
			+ "			p.projectId, p.createdOn, p.projectName, p.state, p.clientId, p.poProjectId, p.active, \n"
			+ "			p.syncProject, p.createdBy, p.updatedBy, p.updatedOn, p.isDraftProject, p.poEndDate, p.poNo, \n"
			+ "			p.poProjectType, p.poStartDate, p.apmosysRM, p.clientRM, p.deptId, p.isRenewable, p.status,\n"
			+ "			p.apmosysRmEmail, p.projectCompletionDate, p.projectStatus, p.internalProjectType,c.clientName,\n"
			+ "			CASE \n"
			+ "				 WHEN p.isDraftProject = 'true' THEN 'Pending For Approval' \n"
			+ "				 WHEN p.isDraftProject = 'false' THEN 'Approved' \n"
			+ "				 WHEN p.isDraftProject = 'Rejected' THEN 'Rejected' \n"
			+ "				 WHEN p.isDraftProject = 'Completed' THEN 'Completed' \n"
			+ "	 WHEN 			  p.isDraftProject IS NULL THEN 'Not Started'\n"
			+ "				 ELSE 'Un Mentioned Test Data' \n"
			+ "			END, \n"
			+ "			CASE \n"
			+ "			  WHEN p.poProjectId IS NOT NULL THEN CONCAT('po', p.poProjectId) \n"
			+ "			  ELSE CONCAT('', p.poProjectId) \n"
			+ "			END AS projectViewId ) \n"
			+ "			FROM Project p   \n"
			+ "			INNER JOIN ProjectDepartmentMap pdm on p.projectId = pdm.projectId\n"
			+ "            LEFT JOIN Client c ON c.clientId = p.clientId \n"
			+ "			where p.active= 'true' and p.isDraftProject = 'true'\n"
			+ "			 and (pdm.deptId IN (:deptIds))")
	List<ProjectFetchDTO> getAllPendingProjectList(@Param("deptIds") List<Long> deptIds);
	


	@Query(value="SELECT  distinct\n"
			+ "			    p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
			+ "			    p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
			+ "			    p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
			+ "			    p.apmosys_rm_email, p.project_completion_date, p.project_status, p.internal_project_type,c.client_name,\n"
			+ "			    'Approved' AS approval_status, \n"
			+ "			    CASE \n"
			+ "			      WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id) \n"
			+ "			      ELSE CONCAT('', p.project_id) \n"
			+ "			    END AS project_view_id\n"
			+ "			FROM projects p \n"
			+ "			INNER JOIN teams t ON p.project_id = t.project_id \n"
			+ "			INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
			+ "			INNER JOIN project_department_map pdm ON p.project_id = pdm.project_id\n"
			+ "			LEFT JOIN clients c ON c.client_id = p.client_id \n"
			+ "			WHERE p.active = 'true' AND t.is_active = 'Y' AND etm.active != 0\n"
			+ "			AND p.is_draft_project = 'false'\n"
			+ "			AND (pdm.dept_id IN (:deptIds))\n"
			+ "			\n"
			+ "			UNION ALL\n"
			+ "			\n"
			+ "			SELECT  distinct\n"
			+ "			    p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
			+ "			    p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
			+ "			    p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
			+ "			    p.apmosys_rm_email, p.project_completion_date, p.project_status, p.internal_project_type,c.client_name,\n"
			+ "			    'Pending For Approval' AS approval_status,\n"
			+ "			    CASE \n"
			+ "			      WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id) \n"
			+ "			      ELSE CONCAT('', p.project_id) \n"
			+ "			    END AS project_view_id\n"
			+ "			FROM projects p   \n"
			+ "			INNER JOIN project_department_map pdm ON p.project_id = pdm.project_id\n"
			+ "			LEFT JOIN clients c ON c.client_id = p.client_id \n"
			+ "			WHERE p.active= 'true' AND p.is_draft_project = 'true'\n"
			+ "			AND (pdm.dept_id IN (:deptIds))\n"
			+ "			\n"
			+ "			UNION ALL\n"
			+ "			\n"
			+ "			SELECT  distinct\n"
			+ "			    p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
			+ "			    p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
			+ "			    p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
			+ "			    p.apmosys_rm_email, p.project_completion_date, p.project_status, p.internal_project_type,c.client_name,\n"
			+ "			    'Not Started' AS approval_status,\n"
			+ "			    CASE \n"
			+ "			      WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id) \n"
			+ "			      ELSE CONCAT('', p.project_id) \n"
			+ "			    END AS project_view_id\n"
			+ "			FROM projects p   \n"
			+ "			INNER JOIN project_department_map pdm ON p.project_id = pdm.project_id\n"
			+ "			LEFT JOIN clients c ON c.client_id = p.client_id \n"
			+ "			WHERE p.active= 'true' AND p.is_draft_project IS NULL \n"
			+ "			AND (p.status != 'Completed' OR p.status IS NULL)\n"
			+ "			AND NOT EXISTS (SELECT 1 FROM teams t WHERE t.project_id = p.project_id) \n"
			+ "			AND (p.internal_project_type IS NOT NULL OR DATE(p.po_end_date) > CURDATE())\n"
			+ "			AND (pdm.dept_id IN (:deptIds))\n"
			+ "			\n"
			+ "			UNION ALL\n"
			+ "			\n"
			+ "			SELECT  distinct\n"
			+ "			    p.project_id, p.created_on, p.project_name, p.state, p.client_id, p.po_project_id, p.active, \n"
			+ "			    p.sync_project, p.created_by, p.updated_by, p.updated_on, p.is_draft_project, p.po_end_date, p.po_no, \n"
			+ "			    p.po_project_type, p.po_start_date, p.apmosysrm, p.clientrm, p.dept_id, p.is_renewable, p.status,\n"
			+ "			    p.apmosys_rm_email, p.project_completion_date, p.project_status, p.internal_project_type,c.client_name,\n"
			+ "			    'Rejected' AS approval_status,\n"
			+ "			    CASE \n"
			+ "			      WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id) \n"
			+ "			      ELSE CONCAT('', p.project_id) \n"
			+ "			    END AS project_view_id\n"
			+ "			FROM projects p   \n"
			+ "			INNER JOIN project_department_map pdm ON p.project_id = pdm.project_id\n"
			+ "			LEFT JOIN clients c ON c.client_id = p.client_id \n"
			+ "			WHERE p.active= 'true' AND UPPER(p.is_draft_project) = 'REJECTED'\n"
			+ "			AND (pdm.dept_id IN (:deptIds));" , nativeQuery = true)
	List<Object[]> getAllTotalProjectList(@Param("deptIds") List<Long> deptIds);
	
	
	@Query(value="select  count( Distinct p.projectId) from Project p \n"
			+ "			 inner join ProjectDepartmentMap pdm on pdm.projectId = p.projectId \n"
			+ "			 where  p.projectStatus = 'Completed' and pdm.deptId IN :deptIds")
	Integer getAllCompletedProjectCountInIshine(@Param("deptIds") List<Long> deptIds);
	
	@Transactional
	@Modifying
    @Query(value = "CALL sp_SyncProjectsFromTemp", nativeQuery = true)
    void callSyncProjectsSP();
	
	
	

	@Query(value = "select count(distinct p.project_id) from projects p\n"
			+ "INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n"
			+ "INNER JOIN department d ON pd.dept_id = d.dept_id\n"
			+ "INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "INNER JOIN clients c ON p.client_id = c.client_id\n"
			+ "LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
			+ " LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n"
			+ " left JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
			+ " left JOIN department d1 on d1.dept_id = j1.dept_id\n"
			+ "WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and po_project_type = 'TNM'\n"
			+ " and d.dept_id in (:deptIds)" , nativeQuery = true)
	Integer getAllActiveTNMProjectsCount(@Param("deptIds") List<Long> deptIds);
	
	
	
	@Query(value = "SELECT\n"
		    + " distinct p.project_id,project_name, po_no, p.client_id, p.po_project_id, p.active,po_project_type,\n"
		    + " GROUP_CONCAT(DISTINCT e1.name ORDER BY e1.name SEPARATOR ', ') as Project_Manager,\n"
		    + " c.client_name, clientrm, p.dept_id,apmosysrm, date(po_start_date) po_start_date, date(po_end_date) po_end_date,\n"
		    + " p.state, p.created_on, p.status PO_project_status,p.project_completion_date,p.project_status Ishine_project_status, p.internal_project_type,\n"
		    + " CASE \n"
		    + " WHEN p.is_draft_project = 'true' THEN 'Pending For Approval' \n"
		    + " WHEN p.is_draft_project = 'false' THEN 'Approved' \n"
		    + " WHEN p.is_draft_project = 'Rejected' THEN 'Rejected' \n"
		    + " WHEN p.is_draft_project = 'Completed' THEN 'Completed' \n"
		    + " WHEN p.is_draft_project IS NULL THEN 'Not Started' \n"
		    + " ELSE 'Un Mentioned Test Data' \n"
		    + " END as Approval_status, \n"
		    + " CASE \n"
		    + " WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
		    + " ELSE CAST(p.project_id AS CHAR) \n"
		    + " END AS projectViewId, \n"
		    + " GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ', ') AS department_names\n"
		    + "FROM projects p\n"
		    + "INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n"
		    + " INNER JOIN department d ON pd.dept_id = d.dept_id \n"
		    + " INNER JOIN teams t ON p.project_id = t.project_id \n"
		    + " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
		    + " INNER JOIN clients c ON p.client_id = c.client_id \n"
		    + " LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
		    + " LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n"
		    + " LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
		    + " LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
		    + "WHERE 1=1\n"
		    + " and po_project_type = 'TNM' \n"
		    + " and d.dept_id in (:deptIds)\n"
		    + "AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
		    + " GROUP BY p.project_id, project_name, po_no, p.client_id, p.po_project_id, p.active, po_project_type, c.client_name,"
		    + " clientrm, p.dept_id, apmosysrm, po_start_date, po_end_date, p.state, p.created_on, "
		    + "p.status, p.project_completion_date, p.project_status, p.internal_project_type", nativeQuery = true)
		List<Object[]> getAllActiveTNMProjectsList(@Param("deptIds") List<Long> deptIds);
	
	
		@Query(value = "SELECT count(distinct p.project_id)\n"
				+ "FROM projects p\n"
				+ "inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
				+ " inner JOIN department d ON pd.dept_id = d.dept_id \n"
				+ " inner JOIN teams t ON p.project_id = t.project_id \n"
				+ " inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
				+ " inner JOIN clients c ON p.client_id = c.client_id \n"
				+ " inner JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
				+ " LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
				+ " LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
				+ " LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
				+ "WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and po_project_type = 'TNM'\n"
				+ " AND DATE(p.po_end_date) < CURDATE()\n"
				+ " and d.dept_id in (:deptIds)\n"
				+ " and DATE(p.po_end_date) between :from_Date and :to_Date", nativeQuery = true)
				Integer getExpiredProjectCount(@Param("deptIds") List<Long> deptIds,
				                              @Param("from_Date") String fromDate,
				                              @Param("to_Date") String toDate);
		
		
	
	@Query(value = "SELECT\n"
			+ "			     distinct p.project_id,project_name, po_no, p.client_id, p.po_project_id, p.active,po_project_type,\n"
			+ "			     GROUP_CONCAT(DISTINCT e1.name ORDER BY e1.name SEPARATOR ', ') as Project_Manager,\n"
			+ "			     c.client_name, clientrm, p.dept_id,apmosysrm, date(po_start_date) po_start_date, date(po_end_date) po_end_date,\n"
			+ "			     p.state, p.created_on, p.status PO_project_status,p.project_completion_date,p.project_status Ishine_project_status, p.internal_project_type,\n"
			+ "			      CASE \n"
			+ "			     WHEN p.is_draft_project = 'true' THEN 'Pending For Approval' \n"
			+ "			     WHEN p.is_draft_project = 'false' THEN 'Approved' \n"
			+ "			     WHEN p.is_draft_project = 'Rejected' THEN 'Rejected' \n"
			+ "			     WHEN p.is_draft_project = 'Completed' THEN 'Completed' \n"
			+ "			     WHEN p.is_draft_project = null THEN 'Not Started' \n"
			+ "			     ELSE 'Un Mentioned Test Data' \n"
			+ "			     END as Approval_status, \n"
			+ "			      CASE \n"
			+ "			       WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
			+ "			       ELSE CAST(p.project_id AS CHAR) \n"
			+ "			       END AS projectViewId , \n"
			+ "			     GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ', ') AS department_names\n"
			+ "			      FROM projects p\n"
			+ "			      inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
			+ "			      inner JOIN department d ON pd.dept_id = d.dept_id \n"
			+ "			      inner JOIN teams t ON p.project_id = t.project_id \n"
			+ "			      inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
			+ "			      inner JOIN clients c ON p.client_id = c.client_id \n"
			+ "			      inner JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
			+ "			      LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
			+ "			      LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
			+ "			      LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
			+ "			      WHERE\n"
			+ "			     po_project_type = 'TNM'\n"
			+ "                 and etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
			+ "			     AND DATE(p.po_end_date) < CURDATE()\n"
			+ "			        and d.dept_id in (:deptIds)\n"
			+ "					and DATE(p.po_end_date) between :from_Date and :to_Date\n"
			+ "				 GROUP BY\n"
			+ "			     p.project_id,project_name, po_no,p.client_id, p.po_project_id, p.active, po_project_type, c.client_name, clientrm, p.dept_id, apmosysrm, \n"
			+ "			     po_start_date, po_end_date, p.state, p.created_on, p.status,p.project_completion_date,p.project_status, \n"
			+ "			     p.internal_project_type", nativeQuery = true)
		List<Object[]> getExpiredProjectList(@Param("deptIds") List<Long> deptIds,
                @Param("from_Date") String fromDate,
                @Param("to_Date") String toDate);


		
		@Query("SELECT new com.apmosys.employeeportal.dto.ExpiredPoDto(" +
			       "p.poNo, " +
			       "p.projectName, " +
			       "p.poEndDate, " +
			       "p.apmosysRM, " +
			       "c.clientName, " +
			       "p.clientRM, " +
			       "DATEDIFF(CURRENT_DATE, p.poEndDate)) " +
			       "FROM Project p " +
			       "JOIN ProjectDepartmentMap pd ON p.projectId = pd.projectId " +
			       "JOIN Department d ON pd.deptId = d.deptId " +
			       "JOIN Team t ON p.projectId = t.projectId " +
			       "JOIN EmployeeTeamMap etm ON t.teamId = etm.teamId " +
			       "JOIN Client c ON p.clientId = c.clientId " +
			       "JOIN ProjectManagerMapping pm ON p.projectId = pm.projectId " +
			       "LEFT JOIN Employee e1 ON e1.empId = pm.projectManagerId " +
			       "LEFT JOIN JobRole j1 ON j1.jobRoleId = e1.jobRoleId " +
			       "LEFT JOIN Department d1 ON d1.deptId = j1.deptId " +
			       "WHERE p.poProjectType = 'TNM' " +
			       "AND etm.active != 0 " +
			       "AND t.isActive != 'N' " +
			       "AND p.active != 'false' " +
			       "AND p.poEndDate < CURRENT_DATE")
			List<ExpiredPoDto> getAllExpiredTNMProject();


	    

@Query(value="SELECT p FROM Project p \n"
			+ "WHERE p.active = 'true' \n"
			+ "AND p.projectId = :projectId \n"
			+ "AND p.active = 'true'")
	Project getByProjectId(Integer projectId);

//	@Query(value="WITH\n"
//			+ "   All_Applicable_Projects AS (\n"
//			+ "        SELECT DISTINCT\n"
//			+ "            p.project_id,\n"
//			+ "            p.project_name,\n"
//			+ "            p.po_no\n"
//			+ "        FROM\n"
//			+ "            projects p\n"
//			+ "        INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n"
//			+ "        INNER JOIN department d ON pd.dept_id = d.dept_id\n"
//			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
//			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
//			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
//			+ "        INNER JOIN clients c ON p.client_id = c.client_id\n"
//			+ "        WHERE\n"
//			+ "            etm.active != 0\n"
//			+ "            AND t.is_active != 'N'\n"
//			+ "            AND p.active != 'false'\n"
//			+ "            AND p.has_client_side_id = 1\n"
//			+ "            AND DATE(etm.start_date) < CURDATE()\n"
//			+ "    ) ,  Authorized_Projects AS (\n"
//			+ "        SELECT DISTINCT project_id FROM (\n"
//			+ "            SELECT p.project_id\n"
//			+ "             FROM projects p\n"
//			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
//			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
//			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
//			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
//			+ "        LEFT JOIN employee_client_side_id_mapping ecsm ON ecsm.emp_id = e.emp_id AND ecsm.project_id = p.project_id\n"
//			+ "        WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
//			+ "          AND p.has_client_side_id = true AND e.employmentstatus != 'InActive'\n"
//			+ "            and EXISTS (\n"
//			+ "                SELECT 1 FROM employee e\n"
//			+ "                JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
//			+ "                JOIN department d ON jr.dept_id = d.dept_id\n"
//			+ "                WHERE e.emp_id = :emp_id AND (\n"
//			+ "            jr.employee_role IN ('SuperAdmin')\n"
//			+ "            OR d.name = 'HR'\n"
//			+ "      )\n"
//			+ "            )\n"
//			+ "\n"
//			+ "            UNION\n"
//			+ "\n"
//			+ "            SELECT p.project_id\n"
//			+ "            FROM projects p\n"
//			+ "            JOIN teams t ON p.project_id = t.project_id\n"
//			+ "            JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
//			+ "            JOIN employee e ON etm.emp_id = e.emp_id\n"
//			+ "            JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
//			+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
//			+ "            AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
//			+ "			AND p.has_client_side_id = true\n"
//			+ "\n"
//			+ "            UNION\n"
//			+ "\n"
//			+ "            SELECT p.project_id\n"
//			+ "            FROM projects p\n"
//			+ "            LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
//			+ "            LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
//			+ "            LEFT JOIN teams t ON p.project_id = t.project_id\n"
//			+ "            LEFT JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
//			+ "            WHERE 1=1 and\n"
//			+ "            (\n"
//			+ "			  pm.project_manager_id = :emp_id\n"
//			+ "              OR pom.project_overhead_id = :emp_id\n"
//			+ "              OR t.spoc_id = :emp_id\n"
//			+ "              OR t.team_lead_id = :emp_id\n"
//			+ "              OR etm.emp_id = :emp_id\n"
//			+ "			)\n"
//			+ "            AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
//			+ "			AND p.has_client_side_id = true\n"
//			+ "        ) AS projects_list\n"
//			+ "    )\n"
//			+ "SELECT\n"
//			+ "    aap.project_name,\n"
//			+ "    aap.po_no,\n"
//			+ "    aap.project_id\n"
//			+ "FROM\n"
//			+ "    All_Applicable_Projects aap\n"
//			+ "INNER JOIN\n"
//			+ "    Authorized_Projects ap ON aap.project_id = ap.project_id",nativeQuery = true)
//	public List<Object[]> getProjectWithCliendSideID(@Param("emp_id") Long emp_id);
	
	
	  @Query(nativeQuery = true,value ="WITH RECURSIVE\n"
	  		+ "    Date_Generator (dt) AS (\n"
	  		+ "        SELECT DATE_FORMAT(CURDATE(), '%Y-%m-01') \n"
	  		+ "        UNION ALL\n"
	  		+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM Date_Generator WHERE dt < CURDATE() \n"
	  		+ "    ),\n"
	  		+ "\n"
	  		+ "    WorkingDays_Summary AS (\n"
	  		+ "        SELECT COUNT(*) AS expected_fill_count\n"
	  		+ "        FROM Date_Generator\n"
	  		+ "        WHERE dt NOT IN (\n"
	  		+ "            SELECT date_of_holiday\n"
	  		+ "            FROM holiday\n"
	  		+ "            WHERE MONTH(date_of_holiday) = MONTH(CURRENT_DATE())\n"
	  		+ "              AND YEAR(date_of_holiday) = YEAR(CURRENT_DATE())\n"
	  		+ "        )\n"
	  		+ "    ),\n"
	  		+ "\n"
	  		+ "    Base_Employees AS (\n"
	  		+ "        SELECT DISTINCT e.emp_id, e.name,p.project_name,ecsm.client_side_id,e.employeement_id,p.project_id\n"
	  		+ "        FROM projects p\n"
	  		+ "        INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n"
	  		+ "        INNER JOIN department d ON pd.dept_id = d.dept_id\n"
	  		+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
	  		+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
	  		+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
	  		+ "        LEFT JOIN employee_client_side_id_mapping ecsm on ecsm.emp_id = e.emp_id\n"
	  		+ "        WHERE etm.active != 0\n"
	  		+ "          AND t.is_active != 'N'\n"
	  		+ "          AND p.active != 'false'\n"
	  		+ "          AND p.has_client_side_id = 1\n"
	  		+ "          AND date(etm.start_date) < curdate()\n"
	  		+ "    ),\n"
	  		+ "    Timesheet_Summary AS (\n"
	  		+ "        SELECT\n"
	  		+ "            emp_id,\n"
	  		+ "            date,\n"
	  		+ "            COUNT(emp_id) AS submitted_count\n"
	  		+ "        FROM employee_timesheets\n"
	  		+ "        WHERE MONTH(date) = MONTH(CURRENT_DATE())\n"
	  		+ "          AND YEAR(date) = YEAR(CURRENT_DATE())\n"
	  		+ "          AND day_type LIKE '%Working%' and date(date) between :from_Date and :to_Date  \n"
	  		+ "        GROUP BY emp_id\n"
	  		+ "    ),\n"
	  		+ "    Document_Summary AS (\n"
	  		+ "        SELECT\n"
	  		+ "            emp_id,\n"
	  		+ "            COUNT(CASE WHEN client_approval_status = 'pending' THEN 1 END) AS Client_pending_count,\n"
	  		+ "            COUNT(CASE WHEN client_approval_status = 'approved' THEN 1 END) AS Client_Approved_count\n"
	  		+ "        FROM timesheet_document_details\n"
	  		+ "        WHERE MONTH(created_on) = MONTH(CURRENT_DATE())\n"
	  		+ "          AND YEAR(created_on) = YEAR(CURRENT_DATE())\n"
	  		+ "        GROUP BY emp_id\n"
	  		+ "    )\n"
	  		+ "\n"
	  		+ "SELECT SQL_CALC_FOUND_ROWS  distinct \n"
	  		+ "    e.name,\n"
	  		+ "    e.emp_id,\n"
	  		+ "    e.employeement_id,\n"
	  		+ "    e.client_side_id,\n"
	  		+ "    e.project_name,\n"
	  		+ "\n"
	  		+ "    wds.expected_fill_count,\n"
	  		+ "\n"
	  		+ "    IFNULL(ts.submitted_count, 0) AS submitted_count,\n"
	  		+ "    IFNULL(ds.Client_pending_count, 0) AS Client_pending_count,\n"
	  		+ "    IFNULL(ds.Client_Approved_count, 0) AS Client_Approved_count\n"
	  		+ "FROM\n"
	  		+ "    Base_Employees e\n"
	  		+ "CROSS JOIN\n"
	  		+ "    WorkingDays_Summary wds\n"
	  		+ "LEFT JOIN\n"
	  		+ "    Timesheet_Summary ts ON e.emp_id = ts.emp_id\n"
	  		+ "LEFT JOIN\n"
	  		+ "    Document_Summary ds ON e.emp_id = ds.emp_id\n"
	  		+ " WHERE e.project_id = :proj_ID\n"
	  		+ "ORDER BY\n"
	  		+ "    e.name LIMIT :offset, :pageSize" )
		    List<Object[]> getEmployeeTimesheetsByProject(
		        @Param("proj_ID") Integer projectId,
		        @Param("from_Date") String fromDate,
		        @Param("to_Date") String toDate,int offset,int pageSize);

		    @Query(value = " WITH RECURSIVE\n"
		    		+ "    Date_Parameters AS (\n"
		    		+ "        SELECT STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
		    		+ "               CASE\n"
		    		+ "                   WHEN CAST(:year AS UNSIGNED) = YEAR(CURDATE()) AND CAST(:month AS UNSIGNED) = MONTH(CURDATE())\n"
		    		+ "                   THEN CURDATE()\n"
		    		+ "                   ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
		    		+ "               END AS to_date\n"
		    		+ "    ),\n"
		    		+ "    All_Dates_In_Range(dt) AS (\n"
		    		+ "        SELECT from_date FROM Date_Parameters\n"
		    		+ "        UNION ALL\n"
		    		+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range\n"
		    		+ "        WHERE dt < (SELECT to_date FROM Date_Parameters)\n"
		    		+ "    ),\n"
		    		+ "    Authorized_Employees AS (\n"
		    		+ "        SELECT DISTINCT e.emp_id\n"
		    		+ "        FROM employee e\n"
		    		+ "        WHERE (\n"
		    		+ "            EXISTS (\n"
		    		+ "                SELECT 1\n"
		    		+ "                FROM employee u\n"
		    		+ "                JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
		    		+ "                JOIN department d ON jr.dept_id = d.dept_id\n"
		    		+ "                WHERE u.emp_id = :emp_id\n"
		    		+ "                  AND (jr.employee_role IN ('SuperAdmin')\n"
		    		+ "                  OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
		    		+ "            )\n"
		    		+ "            OR e.job_role_id IN (\n"
		    		+ "                SELECT jr.job_role_id FROM job_role jr\n"
		    		+ "                WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
		    		+ "            )\n"
		    		+ "            OR e.emp_id IN (\n"
		    		+ "                SELECT etm.emp_id\n"
		    		+ "                FROM employee_team_mapping etm\n"
		    		+ "                JOIN teams t ON t.team_id = etm.team_id\n"
		    		+ "                WHERE t.project_id IN (\n"
		    		+ "                    SELECT p.project_id\n"
		    		+ "                    FROM projects p\n"
		    		+ "                    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
		    		+ "                    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
		    		+ "                    LEFT JOIN teams t2 ON p.project_id = t2.project_id\n"
		    		+ "                    LEFT JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id\n"
		    		+ "                    WHERE pm.project_manager_id = :emp_id\n"
		    		+ "                       OR pom.project_overhead_id = :emp_id\n"
		    		+ "                       OR t2.spoc_id = :emp_id\n"
		    		+ "                       OR t2.team_lead_id = :emp_id\n"
		    		+ "                       OR etm2.emp_id = :emp_id\n"
		    		+ "                )\n"
		    		+ "            )\n"
		    		+ "        )\n"
		    		+ "    ),\n"
		    		+ "    Project_Manager_Summary AS (\n"
		    		+ "        SELECT p.project_id,\n"
		    		+ "               GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') as Project_Manager\n"
		    		+ "        FROM projects p\n"
		    		+ "        LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
		    		+ "        LEFT JOIN employee e ON e.emp_id = pm.project_manager_id\n"
		    		+ "        GROUP BY p.project_id\n"
		    		+ "    ),\n"
		    		+ "    Base_Project_Employees AS (\n"
		    		+ "        SELECT DISTINCT e.emp_id, p.project_id, p.project_name, p.po_no,\n"
		    		+ "                        COALESCE(p.po_project_type, p.internal_project_type) AS project_type,\n"
		    		+ "                        p.apmosysrm, p.apmosys_rm_email, p.clientrm, c.client_name,\n"
		    		+ "                        etm.start_date AS employee_project_start_date, etm.end_date\n"
		    		+ "        FROM projects p\n"
		    		+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
		    		+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
		    		+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
		    		+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
		    		+ "        INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
		    		+ "        WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
		    		+ "          AND p.has_client_side_id = TRUE AND e.employmentstatus != 'InActive'\n"
		    		+ "          AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
		    		+ "          AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters))\n"
		    		+ "    ),\n"
		    		+ "    Expected_Client_Side_DSR_Dates AS (\n"
		    		+ "        SELECT\n"
		    		+ "            bpe.emp_id,\n"
		    		+ "            bpe.project_id,\n"
		    		+ "            adir.dt\n"
		    		+ "        FROM Base_Project_Employees bpe\n"
		    		+ "        CROSS JOIN All_Dates_In_Range adir\n"
		    		+ "        LEFT JOIN holiday h ON h.date_of_holiday = adir.dt\n"
		    		+ "        LEFT JOIN employee_timesheets et_leave ON et_leave.emp_id = bpe.emp_id\n"
		    		+ "                                               -- AND et_leave.project_id = bpe.project_id\n"
		    		+ "                                               AND et_leave.date = adir.dt\n"
		    		+ "                                               AND upper(et_leave.day_type) LIKE '%LEAVE%'\n"
		    		+ "        WHERE adir.dt < CURDATE()\n"
		    		+ "          AND adir.dt >= DATE(bpe.employee_project_start_date)\n"
		    		+ "          AND (bpe.end_date IS NULL OR adir.dt <= bpe.end_date)\n"
		    		+ "          AND (\n"
		    		+ "              (h.date_of_holiday IS NULL AND DAYOFWEEK(adir.dt) NOT IN (1)\n"
		    		+ "               AND NOT (DAYOFWEEK(adir.dt) = 7 AND (DAY(adir.dt) BETWEEN 8 AND 14 OR DAY(adir.dt) BETWEEN 22 AND 28)))\n"
		    		+ "          )\n"
		    		+ "          AND et_leave.date IS NULL\n"
		    		+ "    ),\n"
		    		+ "    Expected_Client_Side_DSR AS (\n"
		    		+ "        SELECT emp_id, project_id, COUNT(DISTINCT dt) AS expected_dsr_days\n"
		    		+ "        FROM Expected_Client_Side_DSR_Dates\n"
		    		+ "        GROUP BY emp_id, project_id\n"
		    		+ "    ),\n"
		    		+ "    Employee_Actual_Working_Days AS (\n"
		    		+ "        SELECT DISTINCT bpe.emp_id, bpe.project_id, adir.dt\n"
		    		+ "        FROM Base_Project_Employees bpe\n"
		    		+ "        CROSS JOIN All_Dates_In_Range adir\n"
		    		+ "        LEFT JOIN holiday h ON h.date_of_holiday = adir.dt\n"
		    		+ "        WHERE adir.dt < CURDATE()\n"
		    		+ "          AND adir.dt >= DATE(bpe.employee_project_start_date)\n"
		    		+ "          AND (bpe.end_date IS NULL OR adir.dt <= bpe.end_date)\n"
		    		+ "          AND (\n"
		    		+ "              (h.date_of_holiday IS NULL AND DAYOFWEEK(adir.dt) NOT IN (1)\n"
		    		+ "               AND NOT (DAYOFWEEK(adir.dt) = 7 AND (DAY(adir.dt) BETWEEN 8 AND 14 OR DAY(adir.dt) BETWEEN 22 AND 28)))\n"
		    		+ "              OR EXISTS (\n"
		    		+ "                  SELECT 1 FROM employee_timesheets et\n"
		    		+ "                  JOIN timesheet_document_details tdd ON et.timesheet_id = tdd.timesheet_id\n"
		    		+ "                  WHERE et.emp_id = bpe.emp_id\n"
		    		+ "                    AND et.project_id = bpe.project_id\n"
		    		+ "                    AND et.date = adir.dt\n"
		    		+ "                    AND tdd.active = TRUE\n"
		    		+ "              )\n"
		    		+ "          )\n"
		    		+ "    ),\n"
		    		+ "    Missing_Days AS (\n"
		    		+ "        SELECT awd.emp_id, awd.project_id, awd.dt\n"
		    		+ "        FROM Employee_Actual_Working_Days awd\n"
		    		+ "        WHERE awd.dt < CURDATE()\n"
		    		+ "          AND NOT EXISTS (\n"
		    		+ "              SELECT 1 FROM employee_timesheets et\n"
		    		+ "              WHERE et.emp_id = awd.emp_id\n"
		    		+ "                AND et.project_id = awd.project_id\n"
		    		+ "                AND et.date = awd.dt\n"
		    		+ "                AND et.day_type LIKE '%Working%'\n"
		    		+ "          )\n"
		    		+ "    ),\n"
		    		+ "    Defaulter_Employees AS (\n"
		    		+ "        SELECT emp_id, project_id\n"
		    		+ "        FROM Missing_Days\n"
		    		+ "        GROUP BY emp_id, project_id\n"
		    		+ "        HAVING COUNT(*) >= 2\n"
		    		+ "    ),\n"
		    		+ "    Apmosys_Timesheet_Filled_Days AS (\n"
		    		+ "        SELECT\n"
		    		+ "            et.emp_id, et.project_id,\n"
		    		+ "            COUNT(DISTINCT et.date) AS filled_working_days\n"
		    		+ "        FROM employee_timesheets et\n"
		    		+ "        INNER JOIN Expected_Client_Side_DSR_Dates ecd ON et.emp_id = ecd.emp_id AND et.project_id = ecd.project_id AND et.date = ecd.dt\n"
		    		+ "        WHERE (et.day_type LIKE '%Working%' OR upper(et.day_type) LIKE '%LEAVE%')\n"
		    		+ "        GROUP BY et.emp_id, et.project_id\n"
		    		+ "    ),\n"
		    		+ "    Document_Summary AS (\n"
		    		+ "        SELECT tdd.emp_id, et.project_id,\n"
		    		+ "                COUNT(DISTINCT CASE WHEN upper(tdd.client_approval_status) = 'APPROVED' AND final_flag = 1 THEN tdd.timesheet_id END) AS Client_Approved_count,\n"
		    		+ "                COUNT(DISTINCT CASE WHEN upper(tdd.client_approval_status) = 'PENDING' AND tdd.timesheet_id\n"
		    		+ "                                                                NOT IN (SELECT timesheet_id FROM timesheet_document_details WHERE upper(client_approval_status) = 'APPROVED') THEN tdd.timesheet_id END) AS Client_pending_count\n"
		    		+ "                FROM timesheet_document_details tdd\n"
		    		+ "        INNER JOIN employee_timesheets et on tdd.timesheet_id = et.timesheet_id\n"
		    		+ "        INNER JOIN Expected_Client_Side_DSR_Dates ecd ON et.emp_id = ecd.emp_id AND et.project_id = ecd.project_id AND et.date = ecd.dt\n"
		    		+ "        WHERE DATE(et.date) BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters) AND active = TRUE\n"
		    		+ "        GROUP BY tdd.emp_id, et.project_id\n"
		    		+ "    ),\n"
		    		+ "    Employee_Final_Summary AS (\n"
		    		+ "        SELECT\n"
		    		+ "            bpe.emp_id, bpe.project_id, bpe.project_name, pms.Project_Manager, bpe.po_no, bpe.project_type, bpe.client_name,\n"
		    		+ "            bpe.apmosysrm, bpe.apmosys_rm_email, bpe.clientrm,\n"
		    		+ "            COALESCE(ecsd.expected_dsr_days, 0) AS expected_dsr_count,\n"
		    		+ "            COALESCE(atfd.filled_working_days, 0) AS ishine_timesheet_filled_count,\n"
		    		+ "            GREATEST(0, COALESCE(ecsd.expected_dsr_days, 0) - (COALESCE(ds.Client_Approved_count, 0) + COALESCE(ds.Client_pending_count, 0))) AS ClientSideNotFilledTimesheets_count,\n"
		    		+ "            COALESCE(ds.Client_pending_count, 0) AS ClientSidePendingTimesheet_count,\n"
		    		+ "            COALESCE(ds.Client_Approved_count, 0) AS Client_Approved_count,\n"
		    		+ "            CASE\n"
		    		+ "                WHEN de.emp_id IS NOT NULL THEN 'Defaulter'\n"
		    		+ "                WHEN COALESCE(ecsd.expected_dsr_days, 0) > (COALESCE(ds.Client_Approved_count, 0) + COALESCE(ds.Client_pending_count, 0))\n"
		    		+ "                     OR COALESCE(ds.Client_pending_count, 0) > 0 THEN 'Pending'\n"
		    		+ "                ELSE 'Approved'\n"
		    		+ "            END AS employee_status\n"
		    		+ "        FROM Base_Project_Employees bpe\n"
		    		+ "        LEFT JOIN Project_Manager_Summary pms ON bpe.project_id = pms.project_id\n"
		    		+ "        LEFT JOIN Document_Summary ds ON bpe.emp_id = ds.emp_id AND bpe.project_id = ds.project_id\n"
		    		+ "        LEFT JOIN Defaulter_Employees de ON bpe.emp_id = de.emp_id AND bpe.project_id = de.project_id\n"
		    		+ "        LEFT JOIN Apmosys_Timesheet_Filled_Days atfd ON bpe.emp_id = atfd.emp_id AND bpe.project_id = atfd.project_id\n"
		    		+ "        LEFT JOIN Expected_Client_Side_DSR ecsd ON bpe.emp_id = ecsd.emp_id AND bpe.project_id = ecsd.project_id\n"
		    		+ "    ),\n"
		    		+ "    Project_Level_Summary AS (\n"
		    		+ "        SELECT project_id, project_name, Project_Manager, po_no, project_type, client_name,\n"
		    		+ "               apmosysrm, apmosys_rm_email, clientrm,\n"
		    		+ "               SUM(expected_dsr_count) AS total_expected_fill_count,\n"
		    		+ "               SUM(ishine_timesheet_filled_count) AS total_ishine_filled,\n"
		    		+ "               SUM(ClientSideNotFilledTimesheets_count) AS total_client_side_not_filled,\n"
		    		+ "               SUM(ClientSidePendingTimesheet_count) AS total_client_side_pending,\n"
		    		+ "               SUM(Client_Approved_count) AS total_client_approved,\n"
		    		+ "               CASE\n"
		    		+ "                   WHEN SUM(CASE WHEN employee_status = 'Defaulter' THEN 1 ELSE 0 END) > 0 THEN 'Defaulter'\n"
		    		+ "                   WHEN SUM(CASE WHEN employee_status = 'Pending' THEN 1 ELSE 0 END) > 0 THEN 'Pending'\n"
		    		+ "                   ELSE 'Approved'\n"
		    		+ "               END AS project_status,\n"
		    		+ "               CASE WHEN SUM(expected_dsr_count) > 0 THEN ROUND((SUM(Client_Approved_count) / SUM(expected_dsr_count)) * 100, 2) ELSE 0 END AS ClientSideApproved_Percent,\n"
		    		+ "               CASE WHEN SUM(expected_dsr_count) > 0 THEN ROUND((SUM(ClientSidePendingTimesheet_count) / SUM(expected_dsr_count)) * 100, 2) ELSE 0 END AS ClientSidePending_Percent,\n"
		    		+ "               CASE WHEN SUM(expected_dsr_count) > 0 THEN ROUND((SUM(ClientSideNotFilledTimesheets_count) / SUM(expected_dsr_count)) * 100, 2) ELSE 0 END AS NotFilled_Percent\n"
		    		+ "        FROM Employee_Final_Summary\n"
		    		+ "        GROUP BY project_id, project_name, Project_Manager, po_no, project_type, client_name,\n"
		    		+ "                 apmosysrm, apmosys_rm_email, clientrm\n"
		    		+ "    )\n"
		    		+ "SELECT SQL_CALC_FOUND_ROWS\n"
		    		+ "    pls.project_id, pls.project_name, pls.Project_Manager, pls.po_no, pls.project_type, pls.client_name,\n"
		    		+ "    pls.apmosysrm, pls.apmosys_rm_email, pls.clientrm,\n"
		    		+ "    pls.total_expected_fill_count, pls.total_client_side_not_filled,\n"
		    		+ "    pls.total_client_side_pending, pls.total_client_approved, pls.project_status,\n"
		    		+ "    pls.ClientSideApproved_Percent, pls.ClientSidePending_Percent, pls.NotFilled_Percent\n"
		    		+ "FROM Project_Level_Summary pls\n"
		    		+ "WHERE\n"
		    		+ "    (:status = 'All' OR pls.project_status = :status)\n"
		    		+ "ORDER BY pls.project_name LIMIT :offset, :pageSize; ",
		            nativeQuery = true)
		    public List<Object[]> getProjectViewForClientAttendanceStatus(
		            @Param("month") Integer month,
		            @Param("year") Integer year,
		            @Param("status") String status,
		            @Param("emp_id") Long emp_id,
		            int offset,int pageSize
		    );


		    @Query(value = " WITH RECURSIVE\n"
		    		+ "    Date_Parameters AS (\n"
		    		+ "        SELECT STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d') AS from_date,\n"
		    		+ "               CASE\n"
		    		+ "                   WHEN CAST(:year AS UNSIGNED) = YEAR(CURDATE()) AND CAST(:month AS UNSIGNED) = MONTH(CURDATE())\n"
		    		+ "                   THEN CURDATE()\n"
		    		+ "                   ELSE LAST_DAY(STR_TO_DATE(CONCAT(:year, '-', :month, '-01'), '%Y-%m-%d'))\n"
		    		+ "               END AS to_date\n"
		    		+ "    ),\n"
		    		+ "    All_Dates_In_Range(dt) AS (\n"
		    		+ "        SELECT from_date FROM Date_Parameters\n"
		    		+ "        UNION ALL\n"
		    		+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range\n"
		    		+ "        WHERE dt < (SELECT to_date FROM Date_Parameters)\n"
		    		+ "    ),\n"
		    		+ "    Authorized_Employees AS (\n"
		    		+ "        SELECT DISTINCT e.emp_id\n"
		    		+ "        FROM employee e\n"
		    		+ "        WHERE (\n"
		    		+ "            EXISTS (\n"
		    		+ "                SELECT 1\n"
		    		+ "                FROM employee u\n"
		    		+ "                JOIN job_role jr ON u.job_role_id = jr.job_role_id\n"
		    		+ "                JOIN department d ON jr.dept_id = d.dept_id\n"
		    		+ "                WHERE u.emp_id = :emp_id\n"
		    		+ "                  AND (jr.employee_role IN ('SuperAdmin')\n"
		    		+ "                  OR d.name IN ('HR', 'Accounts', 'Resource Management Group'))\n"
		    		+ "            )\n"
		    		+ "            OR e.job_role_id IN (\n"
		    		+ "                SELECT jr.job_role_id FROM job_role jr\n"
		    		+ "                WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
		    		+ "            )\n"
		    		+ "            OR e.emp_id IN (\n"
		    		+ "                SELECT etm.emp_id\n"
		    		+ "                FROM employee_team_mapping etm\n"
		    		+ "                JOIN teams t ON t.team_id = etm.team_id\n"
		    		+ "                WHERE t.project_id IN (\n"
		    		+ "                    SELECT p.project_id\n"
		    		+ "                    FROM projects p\n"
		    		+ "                    LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
		    		+ "                    LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
		    		+ "                    LEFT JOIN teams t2 ON p.project_id = t2.project_id\n"
		    		+ "                    LEFT JOIN employee_team_mapping etm2 ON etm2.team_id = t2.team_id\n"
		    		+ "                    WHERE pm.project_manager_id = :emp_id\n"
		    		+ "                       OR pom.project_overhead_id = :emp_id\n"
		    		+ "                       OR t2.spoc_id = :emp_id\n"
		    		+ "                       OR t2.team_lead_id = :emp_id\n"
		    		+ "                       OR etm2.emp_id = :emp_id\n"
		    		+ "                )\n"
		    		+ "            )\n"
		    		+ "        )\n"
		    		+ "    ),\n"
		    		+ "    Project_Manager_Summary AS (\n"
		    		+ "        SELECT p.project_id,\n"
		    		+ "               GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') as Project_Manager\n"
		    		+ "        FROM projects p\n"
		    		+ "        LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
		    		+ "        LEFT JOIN employee e ON e.emp_id = pm.project_manager_id\n"
		    		+ "        GROUP BY p.project_id\n"
		    		+ "    ),\n"
		    		+ "    Base_Project_Employees AS (\n"
		    		+ "        SELECT DISTINCT e.emp_id, p.project_id, p.project_name, p.po_no,\n"
		    		+ "                        COALESCE(p.po_project_type, p.internal_project_type) AS project_type,\n"
		    		+ "                        p.apmosysrm, p.apmosys_rm_email, p.clientrm, c.client_name,\n"
		    		+ "                        etm.start_date AS employee_project_start_date, etm.end_date\n"
		    		+ "        FROM projects p\n"
		    		+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
		    		+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
		    		+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
		    		+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
		    		+ "        INNER JOIN Authorized_Employees ae ON e.emp_id = ae.emp_id\n"
		    		+ "        WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
		    		+ "          AND e.employmentstatus != 'InActive'\n"
		    		+ "          AND DATE(etm.start_date) <= (SELECT to_date FROM Date_Parameters)\n"
		    		+ "          AND (etm.end_date IS NULL OR DATE(etm.end_date) >= (SELECT from_date FROM Date_Parameters))\n"
		    		+ "    ),\n"
		    		+ "    Expected_Client_Side_DSR_Dates AS (\n"
		    		+ "        SELECT\n"
		    		+ "            bpe.emp_id,\n"
		    		+ "            bpe.project_id,\n"
		    		+ "            adir.dt\n"
		    		+ "        FROM Base_Project_Employees bpe\n"
		    		+ "        CROSS JOIN All_Dates_In_Range adir\n"
		    		+ "        LEFT JOIN holiday h ON h.date_of_holiday = adir.dt\n"
		    		+ "        LEFT JOIN employee_timesheets et_leave ON et_leave.emp_id = bpe.emp_id\n"
		    		+ "                                               -- AND et_leave.project_id = bpe.project_id\n"
		    		+ "                                               AND et_leave.date = adir.dt\n"
		    		+ "                                               AND upper(et_leave.day_type) LIKE '%LEAVE%'\n"
		    		+ "        WHERE adir.dt < CURDATE()\n"
		    		+ "          AND adir.dt >= DATE(bpe.employee_project_start_date)\n"
		    		+ "          AND (bpe.end_date IS NULL OR adir.dt <= bpe.end_date)\n"
		    		+ "          AND (\n"
		    		+ "              (h.date_of_holiday IS NULL AND DAYOFWEEK(adir.dt) NOT IN (1)\n"
		    		+ "               AND NOT (DAYOFWEEK(adir.dt) = 7 AND (DAY(adir.dt) BETWEEN 8 AND 14 OR DAY(adir.dt) BETWEEN 22 AND 28)))\n"
		    		+ "          )\n"
		    		+ "          AND et_leave.date IS NULL\n"
		    		+ "    ),\n"
		    		+ "    Expected_Client_Side_DSR AS (\n"
		    		+ "        SELECT emp_id, project_id, COUNT(DISTINCT dt) AS expected_dsr_days\n"
		    		+ "        FROM Expected_Client_Side_DSR_Dates\n"
		    		+ "        GROUP BY emp_id, project_id\n"
		    		+ "    ),\n"
		    		+ "    Employee_Actual_Working_Days AS (\n"
		    		+ "        SELECT DISTINCT bpe.emp_id, bpe.project_id, adir.dt\n"
		    		+ "        FROM Base_Project_Employees bpe\n"
		    		+ "        CROSS JOIN All_Dates_In_Range adir\n"
		    		+ "        LEFT JOIN holiday h ON h.date_of_holiday = adir.dt\n"
		    		+ "        WHERE adir.dt < CURDATE()\n"
		    		+ "          AND adir.dt >= DATE(bpe.employee_project_start_date)\n"
		    		+ "          AND (bpe.end_date IS NULL OR adir.dt <= bpe.end_date)\n"
		    		+ "          AND (\n"
		    		+ "              (h.date_of_holiday IS NULL AND DAYOFWEEK(adir.dt) NOT IN (1)\n"
		    		+ "               AND NOT (DAYOFWEEK(adir.dt) = 7 AND (DAY(adir.dt) BETWEEN 8 AND 14 OR DAY(adir.dt) BETWEEN 22 AND 28)))\n"
		    		+ "              OR EXISTS (\n"
		    		+ "                  SELECT 1 FROM employee_timesheets et\n"
		    		+ "                  JOIN timesheet_document_details tdd ON et.timesheet_id = tdd.timesheet_id\n"
		    		+ "                  WHERE et.emp_id = bpe.emp_id\n"
		    		+ "                    AND et.project_id = bpe.project_id\n"
		    		+ "                    AND et.date = adir.dt\n"
		    		+ "                    AND tdd.active = TRUE\n"
		    		+ "              )\n"
		    		+ "          )\n"
		    		+ "    ),\n"
		    		+ "    Missing_Days AS (\n"
		    		+ "        SELECT awd.emp_id, awd.project_id, awd.dt\n"
		    		+ "        FROM Employee_Actual_Working_Days awd\n"
		    		+ "        WHERE awd.dt < CURDATE()\n"
		    		+ "          AND NOT EXISTS (\n"
		    		+ "              SELECT 1 FROM employee_timesheets et\n"
		    		+ "              WHERE et.emp_id = awd.emp_id\n"
		    		+ "                AND et.project_id = awd.project_id\n"
		    		+ "                AND et.date = awd.dt\n"
		    		+ "                AND et.day_type LIKE '%Working%'\n"
		    		+ "          )\n"
		    		+ "    ),\n"
		    		+ "    Defaulter_Employees AS (\n"
		    		+ "        SELECT emp_id, project_id\n"
		    		+ "        FROM Missing_Days\n"
		    		+ "        GROUP BY emp_id, project_id\n"
		    		+ "        HAVING COUNT(*) >= 2\n"
		    		+ "    ),\n"
		    		+ "    Apmosys_Timesheet_Filled_Days AS (\n"
		    		+ "        SELECT\n"
		    		+ "            et.emp_id, et.project_id,\n"
		    		+ "            COUNT(DISTINCT et.date) AS filled_working_days\n"
		    		+ "        FROM employee_timesheets et\n"
		    		+ "        INNER JOIN Expected_Client_Side_DSR_Dates ecd ON et.emp_id = ecd.emp_id AND et.project_id = ecd.project_id AND et.date = ecd.dt\n"
		    		+ "        WHERE (et.day_type LIKE '%Working%' OR upper(et.day_type) LIKE '%LEAVE%')\n"
		    		+ "        GROUP BY et.emp_id, et.project_id\n"
		    		+ "    ),\n"
		    		+ "    Document_Summary AS (\n"
		    		+ "        SELECT tdd.emp_id, et.project_id,\n"
		    		+ "                COUNT(DISTINCT CASE WHEN upper(tdd.client_approval_status) = 'APPROVED' AND final_flag = 1 THEN tdd.timesheet_id END) AS Client_Approved_count,\n"
		    		+ "                COUNT(DISTINCT CASE WHEN upper(tdd.client_approval_status) = 'PENDING' AND tdd.timesheet_id\n"
		    		+ "                                                                NOT IN (SELECT timesheet_id FROM timesheet_document_details WHERE upper(client_approval_status) = 'APPROVED') THEN tdd.timesheet_id END) AS Client_pending_count\n"
		    		+ "                FROM timesheet_document_details tdd\n"
		    		+ "        INNER JOIN employee_timesheets et on tdd.timesheet_id = et.timesheet_id\n"
		    		+ "        INNER JOIN Expected_Client_Side_DSR_Dates ecd ON et.emp_id = ecd.emp_id AND et.project_id = ecd.project_id AND et.date = ecd.dt\n"
		    		+ "        WHERE DATE(et.date) BETWEEN (SELECT from_date FROM Date_Parameters) AND (SELECT to_date FROM Date_Parameters) AND active = TRUE\n"
		    		+ "        GROUP BY tdd.emp_id, et.project_id\n"
		    		+ "    ),\n"
		    		+ "    Employee_Final_Summary AS (\n"
		    		+ "        SELECT\n"
		    		+ "            bpe.emp_id, bpe.project_id, bpe.project_name, pms.Project_Manager, bpe.po_no, bpe.project_type, bpe.client_name,\n"
		    		+ "            bpe.apmosysrm, bpe.apmosys_rm_email, bpe.clientrm,\n"
		    		+ "            COALESCE(ecsd.expected_dsr_days, 0) AS expected_dsr_count,\n"
		    		+ "            COALESCE(atfd.filled_working_days, 0) AS ishine_timesheet_filled_count,\n"
		    		+ "            GREATEST(0, COALESCE(ecsd.expected_dsr_days, 0) - (COALESCE(ds.Client_Approved_count, 0) + COALESCE(ds.Client_pending_count, 0))) AS ClientSideNotFilledTimesheets_count,\n"
		    		+ "            COALESCE(ds.Client_pending_count, 0) AS ClientSidePendingTimesheet_count,\n"
		    		+ "            COALESCE(ds.Client_Approved_count, 0) AS Client_Approved_count,\n"
		    		+ "            CASE\n"
		    		+ "                WHEN de.emp_id IS NOT NULL THEN 'Defaulter'\n"
		    		+ "                WHEN COALESCE(ecsd.expected_dsr_days, 0) > (COALESCE(ds.Client_Approved_count, 0) + COALESCE(ds.Client_pending_count, 0))\n"
		    		+ "                     OR COALESCE(ds.Client_pending_count, 0) > 0 THEN 'Pending'\n"
		    		+ "                ELSE 'Approved'\n"
		    		+ "            END AS employee_status\n"
		    		+ "        FROM Base_Project_Employees bpe\n"
		    		+ "        LEFT JOIN Project_Manager_Summary pms ON bpe.project_id = pms.project_id\n"
		    		+ "        LEFT JOIN Document_Summary ds ON bpe.emp_id = ds.emp_id AND bpe.project_id = ds.project_id\n"
		    		+ "        LEFT JOIN Defaulter_Employees de ON bpe.emp_id = de.emp_id AND bpe.project_id = de.project_id\n"
		    		+ "        LEFT JOIN Apmosys_Timesheet_Filled_Days atfd ON bpe.emp_id = atfd.emp_id AND bpe.project_id = atfd.project_id\n"
		    		+ "        LEFT JOIN Expected_Client_Side_DSR ecsd ON bpe.emp_id = ecsd.emp_id AND bpe.project_id = ecsd.project_id\n"
		    		+ "    ),\n"
		    		+ "    Project_Level_Summary AS (\n"
		    		+ "        SELECT project_id, project_name, Project_Manager, po_no, project_type, client_name,\n"
		    		+ "               apmosysrm, apmosys_rm_email, clientrm,\n"
		    		+ "               SUM(expected_dsr_count) AS total_expected_fill_count,\n"
		    		+ "               SUM(ishine_timesheet_filled_count) AS total_ishine_filled,\n"
		    		+ "               SUM(ClientSideNotFilledTimesheets_count) AS total_client_side_not_filled,\n"
		    		+ "               SUM(ClientSidePendingTimesheet_count) AS total_client_side_pending,\n"
		    		+ "               SUM(Client_Approved_count) AS total_client_approved,\n"
		    		+ "               CASE\n"
		    		+ "                   WHEN SUM(CASE WHEN employee_status = 'Defaulter' THEN 1 ELSE 0 END) > 0 THEN 'Defaulter'\n"
		    		+ "                   WHEN SUM(CASE WHEN employee_status = 'Pending' THEN 1 ELSE 0 END) > 0 THEN 'Pending'\n"
		    		+ "                   ELSE 'Approved'\n"
		    		+ "               END AS project_status,\n"
		    		+ "               CASE WHEN SUM(expected_dsr_count) > 0 THEN ROUND((SUM(Client_Approved_count) / SUM(expected_dsr_count)) * 100, 2) ELSE 0 END AS ClientSideApproved_Percent,\n"
		    		+ "               CASE WHEN SUM(expected_dsr_count) > 0 THEN ROUND((SUM(ClientSidePendingTimesheet_count) / SUM(expected_dsr_count)) * 100, 2) ELSE 0 END AS ClientSidePending_Percent,\n"
		    		+ "               CASE WHEN SUM(expected_dsr_count) > 0 THEN ROUND((SUM(ClientSideNotFilledTimesheets_count) / SUM(expected_dsr_count)) * 100, 2) ELSE 0 END AS NotFilled_Percent\n"
		    		+ "        FROM Employee_Final_Summary\n"
		    		+ "        GROUP BY project_id, project_name, Project_Manager, po_no, project_type, client_name,\n"
		    		+ "                 apmosysrm, apmosys_rm_email, clientrm\n"
		    		+ "    )\n"
		    		+ "SELECT SQL_CALC_FOUND_ROWS\n"
		    		+ "    pls.project_id, pls.project_name, pls.Project_Manager, pls.po_no, pls.project_type, pls.client_name,\n"
		    		+ "    pls.apmosysrm, pls.apmosys_rm_email, pls.clientrm,\n"
		    		+ "    pls.total_expected_fill_count, pls.total_client_side_not_filled,\n"
		    		+ "    pls.total_client_side_pending, pls.total_client_approved, pls.project_status,\n"
		    		+ "    pls.ClientSideApproved_Percent, pls.ClientSidePending_Percent, pls.NotFilled_Percent\n"
		    		+ "FROM Project_Level_Summary pls\n"
		    		+ "WHERE\n"
		    		+ "    (:status = 'All' OR pls.project_status = :status)\n"
		    		+ "ORDER BY pls.project_name LIMIT :offset, :pageSize; ",
		            nativeQuery = true)
		    public List<Object[]> getProjectViewForAllEmpAttendanceStatus(
		            @Param("month") Integer month,
		            @Param("year") Integer year,
		            @Param("status") String status,
		            @Param("emp_id") Long emp_id,
		            int offset,int pageSize
		    );

		    
		    
//		    @Query(value = "WITH RECURSIVE\n"
//		    		+ "	 		    Date_Generator (dt) AS (\n"
//		    		+ "	 		        SELECT DATE_FORMAT(CURDATE(), '%Y-%m-01')\n"
//		    		+ "	 		        UNION ALL\n"
//		    		+ "	 		        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM Date_Generator WHERE dt < CURDATE()\n"
//		    		+ "	 		    ),\n"
//		    		+ "	 		\n"
//		    		+ "	 		    WorkingDays_Summary AS (\n"
//		    		+ "	 		        SELECT COUNT(*) AS expected_fill_count\n"
//		    		+ "	 		        FROM Date_Generator\n"
//		    		+ "	 		        WHERE dt NOT IN (\n"
//		    		+ "	 		            SELECT date_of_holiday\n"
//		    		+ "	 		            FROM holiday\n"
//		    		+ "	 		            WHERE MONTH(date_of_holiday) = MONTH(CURRENT_DATE())\n"
//		    		+ "	 		              AND YEAR(date_of_holiday) = YEAR(CURRENT_DATE())\n"
//		    		+ "	 		        )\n"
//		    		+ "	 		    ),\n"
//		    		+ "	 		\n"
//		    		+ "	 		    Project_Manager_Summary AS (\n"
//		    		+ "	 		        SELECT\n"
//		    		+ "	 		            p.project_id,\n"
//		    		+ "	 		            GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') as Project_Manager\n"
//		    		+ "	 		        FROM projects p\n"
//		    		+ "	 		        LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
//		    		+ "	 		        LEFT JOIN employee e ON e.emp_id = pm.project_manager_id\n"
//		    		+ "	 		        GROUP BY p.project_id\n"
//		    		+ "	 		    ),\n"
//		    		+ "	 		    Base_Project_Employees AS (\n"
//		    		+ "	 		        SELECT DISTINCT\n"
//		    		+ "	 		            e.emp_id,\n"
//		    		+ "	 		            p.project_id,\n"
//		    		+ "	 		            p.project_name,\n"
//		    		+ "	 		            p.po_no,\n"
//		    		+ "	 		            CASE\n"
//		    		+ "	 		                WHEN p.po_project_type IS NOT NULL THEN p.po_project_type\n"
//		    		+ "	 		                WHEN p.internal_project_type IS NOT NULL THEN p.internal_project_type\n"
//		    		+ "	 		            END AS project_type,\n"
//		    		+ "	 		            p.apmosysrm,\n"
//		    		+ "	 		            p.apmosys_rm_email,\n"
//		    		+ "	 		            p.clientrm,\n"
//		    		+ "	 		            c.client_name\n"
//		    		+ "	 		        FROM projects p\n"
//		    		+ "	 		        INNER JOIN teams t ON p.project_id = t.project_id\n"
//		    		+ "	 		        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
//		    		+ "	 		        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
//		    		+ "	 		        INNER JOIN clients c ON c.client_id = p.client_id\n"
//		    		+ "	 		        WHERE etm.active != 0\n"
//		    		+ "	 		          AND t.is_active != 'N'\n"
//		    		+ "	 		          AND p.active != 'false'\n"
//		    		+ "	 		          AND p.has_client_side_id = true\n"
//		    		+ "	 		          AND e.employmentstatus != 'InActive'\n"
//		    		+ "	 		          AND date(etm.start_date) < curdate()\n"
//		    		+ "	 		    ),\n"
//		    		+ "	 		\n"
//		    		+ "	 		    Timesheet_Summary AS (\n"
//		    		+ "	 		        SELECT emp_id, COUNT(emp_id) AS submitted_count\n"
//		    		+ "	 		        FROM employee_timesheets\n"
//		    		+ "	 		        WHERE MONTH(date) = MONTH(CURRENT_DATE()) AND YEAR(date) = YEAR(CURRENT_DATE()) AND day_type LIKE '%Working%'\n"
//		    		+ "	 		        GROUP BY emp_id\n"
//		    		+ "	 		    ),\n"
//		    		+ "	 		    Document_Summary AS (\n"
//		    		+ "	 		        SELECT emp_id,\n"
//		    		+ "	 		            COUNT(CASE WHEN client_approval_status = 'pending' THEN 1 END) AS Client_pending_count,\n"
//		    		+ "	 		            COUNT(CASE WHEN client_approval_status = 'approved' THEN 1 END) AS Client_Approved_count\n"
//		    		+ "	 		        FROM timesheet_document_details\n"
//		    		+ "	 		        WHERE MONTH(created_on) = MONTH(CURRENT_DATE()) AND YEAR(created_on) = YEAR(CURRENT_DATE())\n"
//		    		+ "	 		        GROUP BY emp_id\n"
//		    		+ "	 		    ),\n"
//		    		+ "	 		\n"
//		    		+ "	 		    Per_Employee_Summary AS (\n"
//		    		+ "	 		        SELECT\n"
//		    		+ "	 		            bpe.project_id, bpe.project_name, pms.Project_Manager, bpe.po_no, bpe.project_type, bpe.client_name,\n"
//		    		+ "	 		            bpe.apmosysrm, bpe.apmosys_rm_email, bpe.clientrm,\n"
//		    		+ "	 		            wds.expected_fill_count,\n"
//		    		+ "	 		            IFNULL(ts.submitted_count, 0) AS ishine_timesheet_filled_count,\n"
//		    		+ "	 		            wds.expected_fill_count - (IFNULL(ds.Client_pending_count, 0) + IFNULL(ds.Client_Approved_count, 0)) AS ClientSideNotFilledTimesheets_count,\n"
//		    		+ "	 		            IFNULL(ds.Client_pending_count, 0) AS ClientSidePendingTimesheet_count,\n"
//		    		+ "	 		            IFNULL(ds.Client_Approved_count, 0) AS Client_Approved_count\n"
//		    		+ "	 		        FROM Base_Project_Employees bpe\n"
//		    		+ "	 		        CROSS JOIN WorkingDays_Summary wds\n"
//		    		+ "	 		        LEFT JOIN Project_Manager_Summary pms ON bpe.project_id = pms.project_id\n"
//		    		+ "	 		        LEFT JOIN Timesheet_Summary ts ON bpe.emp_id = ts.emp_id\n"
//		    		+ "	 		        LEFT JOIN Document_Summary ds ON bpe.emp_id = ds.emp_id\n"
//		    		+ "	 		    )\n"
//		    		+ "	 		\n"
//		    		+ "	 		SELECT\n"
//		    		+ "	 		    project_id,project_name, Project_Manager, po_no, project_type, client_name,\n"
//		    		+ "	 		    apmosysrm, apmosys_rm_email, clientrm,\n"
//		    		+ "	 		    SUM(expected_fill_count) AS total_expected_fill_count,\n"
//		    		+ "	 		    SUM(ishine_timesheet_filled_count) AS total_ishine_filled,\n"
//		    		+ "	 		    SUM(ClientSideNotFilledTimesheets_count) AS total_client_side_not_filled,\n"
//		    		+ "	 		    SUM(ClientSidePendingTimesheet_count) AS total_client_side_pending,\n"
//		    		+ "	 		    SUM(Client_Approved_count) AS total_client_approved,\n"
//		    		+ "	 		    CASE\n"
//		    		+ "	 		        WHEN SUM(expected_fill_count) > 0 THEN ROUND((SUM(Client_Approved_count) / SUM(expected_fill_count)) * 100, 2)\n"
//		    		+ "	 		        ELSE 0\n"
//		    		+ "	 		    END AS ClientSideApproved_Percent,\n"
//		    		+ "	 		    CASE\n"
//		    		+ "	 		        WHEN SUM(expected_fill_count) > 0 THEN ROUND((SUM(ClientSidePendingTimesheet_count) / SUM(expected_fill_count)) * 100, 2)\n"
//		    		+ "	 		        ELSE 0\n"
//		    		+ "	 		    END AS ClientSidePending_Percent,\n"
//		    		+ "	 		    CASE\n"
//		    		+ "	 		        WHEN SUM(expected_fill_count) > 0 THEN ROUND((SUM(ClientSideNotFilledTimesheets_count) / SUM(expected_fill_count)) * 100, 2)\n"
//		    		+ "	 		        ELSE 0\n"
//		    		+ "	 		    END AS NotFilled_Percent\n"
//		    		+ "	 		FROM Per_Employee_Summary\n"
//		    		+ "	 		GROUP BY\n"
//		    		+ "	 		    project_id,project_name, Project_Manager, po_no, project_type, client_name,\n"
//		    		+ "	 		    apmosysrm, apmosys_rm_email, clientrm ",
//		            nativeQuery = true)
//		public List<Object[]> getProjectViewForClientAttendanceStatus();

		    														



	@Query(nativeQuery=true,value="WITH RECURSIVE\n"
			+ "    Date_Parameters AS (\n"
			+ "	SELECT\n"
			+ "		COALESCE(NULLIF(:fromDate, ''), DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS from_date,\n"
			+ "		COALESCE(NULLIF(:toDate, ''), CURDATE()) AS to_date\n"
			+ "),\n"
			+ "    All_Dates_In_Range(dt) AS (\n"
			+ "        SELECT from_date FROM Date_Parameters\n"
			+ "        UNION ALL\n"
			+ "        SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM All_Dates_In_Range\n"
			+ "        WHERE dt < (SELECT to_date FROM Date_Parameters)\n"
			+ "    ),\n"
			+ "    Base_Project_Employees AS (\n"
			+ "        SELECT DISTINCT\n"
			+ "            etm.team_id, t.team_name, e.emp_id, e.name, etm.employee_role, e.billable_type,\n"
			+ "            etm.start_date, etm.active, p.project_id, p.project_name,\n"
			+ "            p.has_client_side_id, \n"
			+ "            c.client_id, c.client_name,ecsm.client_side_id,p.po_no,\n"
			+ "            s.name spoc, tl.name teamLead, etm.employee_team_map_id,\n"
			+ "            e.reporting_manager_id, e.manager_id, e.approvals_to, \n"
			+ "             CASE\n"
			+ "                WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-',e.employeement_id)\n"
			+ "                ELSE CONCAT('A-',e.employeement_id)\n"
			+ "            END AS employement_id,d.name dept_name,e1.name project_manager_name,\n"
			+ "            e.email,\n"
			+ "            e.mobile_no\n"
			+ "        FROM projects p\n"
			+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
			+ "        LEFT JOIN employee tl on tl.emp_id = t.team_lead_id\n"
			+ "        LEFT JOIN employee s on s.emp_id = t.spoc_id\n"
			+ "        LEFT JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
			+ "        LEFT JOIN department d on d.dept_id = jr.dept_id\n"
			+ "        LEFT JOIN employee_client_side_id_mapping ecsm on e.emp_id = ecsm.emp_id and ecsm.project_id = p.project_id\n"
			+ "        LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
			+ "        LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
			+ "        WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' AND e.employmentstatus != 'InActive' \n"
			+ "        AND pm.active = 1\n"
			+ "        AND date(etm.start_date) < curdate()\n"
			+ "    ),\n"
			+ "\n"
			+ "    Dynamic_Expected_Days AS (\n"
			+ "        SELECT DISTINCT be.emp_id, adir.dt\n"
			+ "        FROM Base_Project_Employees be\n"
			+ "        CROSS JOIN All_Dates_In_Range adir\n"
			+ "        WHERE\n"
			+ "            adir.dt <= CURDATE()\n"
			+ "            AND NOT EXISTS (\n"
			+ "                SELECT 1 FROM employee_timesheets et\n"
			+ "                WHERE et.emp_id = be.emp_id AND et.date = adir.dt\n"
			+ "                  AND (et.day_type LIKE '%Leave%' OR et.day_type LIKE '%Client Holiday%')\n"
			+ "            )\n"
			+ "            AND (\n"
			+ "                (\n"
			+ "                    adir.dt NOT IN (SELECT date_of_holiday FROM holiday)\n"
			+ "                )\n"
			+ "                OR\n"
			+ "                EXISTS (\n"
			+ "                    SELECT 1 FROM employee_timesheets et\n"
			+ "                    WHERE et.emp_id = be.emp_id AND et.date = adir.dt AND et.day_type LIKE '%Working%'\n"
			+ "                )\n"
			+ "            )\n"
			+ "    ),\n"
			+ "    Expected_Days_Summary AS (\n"
			+ "        SELECT emp_id, COUNT(dt) as expected_fill_count\n"
			+ "        FROM Dynamic_Expected_Days\n"
			+ "        GROUP BY emp_id\n"
			+ "    ),\n"
			+ "    Apmosys_Timesheet_Summary AS (\n"
			+ "        SELECT\n"
			+ "            emp_id,\n"
			+ "            COUNT(DISTINCT date) as filled_count\n"
			+ "        FROM employee_timesheets ts\n"
			+ "        JOIN Date_Parameters dp ON ts.date BETWEEN dp.from_date AND dp.to_date\n"
			+ "        WHERE ts.day_type LIKE '%Working%'\n"
			+ "        GROUP BY emp_id\n"
			+ "    ),\n"
			+ "    \n"
			+ "    Document_Summary AS (\n"
			+ "        SELECT\n"
			+ "            emp_id,\n"
			+ "            COUNT(DISTINCT CASE WHEN upper(client_approval_status) = 'PENDING' AND final_flag = 0 THEN DATE(created_on) END) AS Client_pending_count,\n"
			+ "            COUNT(DISTINCT CASE WHEN upper(client_approval_status) = 'APPROVED' AND final_flag = 1 THEN DATE(created_on) END) AS Client_Approved_count\n"
			+ "        FROM timesheet_document_details tdd\n"
			+ "        JOIN Date_Parameters dp ON DATE(tdd.created_on) BETWEEN dp.from_date AND dp.to_date\n"
			+ "        WHERE active = true\n"
			+ "        GROUP BY emp_id\n"
			+ "    )\n"
			+ "SELECT emp_id, client_side_id, start_date, team_name, team_id, name, spoc, billable_type,\n"
			+ "	   employee_role, dept_name, project_id, project_name, \n"
			+ "       group_concat(distinct project_manager_name separator ', ') project_manager_name,\n"
			+ "       po_no, client_name, expectedTimesheetFillCount, apmosysTimesheetFilledCount,\n"
			+ "       client_side_not_filled_count, clientSidePendingCount, clientSideApprovedCount,\n"
			+ "       reporting_manager_id, employement_id \n"
			+ "FROM (       \n"
			+ "		SELECT\n"
			+ "			bpe.emp_id,\n"
			+ "			CASE WHEN bpe.has_client_side_id = true THEN bpe.client_side_id ELSE null END as client_side_id,\n"
			+ "			bpe.start_date, bpe.team_name, bpe.team_id, bpe.name, bpe.spoc, bpe.billable_type,\n"
			+ "			bpe.employee_role, bpe.dept_name, bpe.project_id, bpe.project_name, \n"
			+ "			bpe.project_manager_name, bpe.po_no, bpe.client_name,\n"
			+ "			\n"
			+ "			IFNULL(eds.expected_fill_count, 0) AS expectedTimesheetFillCount,\n"
			+ "			IFNULL(ats.filled_count, 0) AS apmosysTimesheetFilledCount,\n"
			+ "			\n"
			+ "			CASE\n"
			+ "				WHEN bpe.has_client_side_id = true THEN\n"
			+ "					GREATEST(0, IFNULL(eds.expected_fill_count, 0) - (IFNULL(ds.Client_pending_count, 0) + IFNULL(ds.Client_Approved_count, 0)))\n"
			+ "				ELSE 0\n"
			+ "			END as client_side_not_filled_count,\n"
			+ "			\n"
			+ "			CASE\n"
			+ "				WHEN bpe.has_client_side_id = true THEN IFNULL(ds.Client_pending_count, 0)\n"
			+ "				ELSE 0\n"
			+ "			END AS clientSidePendingCount,\n"
			+ "			\n"
			+ "			CASE\n"
			+ "				WHEN bpe.has_client_side_id = true THEN IFNULL(ds.Client_Approved_count, 0)\n"
			+ "				ELSE 0\n"
			+ "			END AS clientSideApprovedCount,\n"
			+ "			\n"
			+ "			bpe.reporting_manager_id,\n"
			+ "			bpe.employement_id\n"
			+ "		FROM\n"
			+ "			Base_Project_Employees bpe\n"
			+ "		LEFT JOIN\n"
			+ "			Expected_Days_Summary eds ON bpe.emp_id = eds.emp_id\n"
			+ "		LEFT JOIN\n"
			+ "			Apmosys_Timesheet_Summary ats ON bpe.emp_id = ats.emp_id\n"
			+ "		LEFT JOIN\n"
			+ "			Document_Summary ds ON bpe.emp_id = ds.emp_id\n"
			+ "		WHERE \n"
			+ "			(bpe.approvals_to = 'Reporting Manager' AND bpe.reporting_manager_id = :empId)\n"
			+ "			OR \n"
			+ "			((bpe.approvals_to = 'Manager' OR bpe.approvals_to IS NULL) AND bpe.manager_id = :empId) \n"
			+ ") as derived_table\n"
			+ "GROUP BY emp_id, client_side_id, start_date, team_name, team_id, name, spoc, billable_type, employee_role, dept_name, project_id, project_name, po_no, client_name, \n"
			+ "		 expectedTimesheetFillCount, apmosysTimesheetFilledCount, client_side_not_filled_count, clientSidePendingCount, clientSideApprovedCount,\n"
			+ "		 reporting_manager_id, employement_id \n"
			+ "ORDER BY name")
	public Optional<List<Object[]>> getAllEmployeeDSROfRM(
		    @Param("empId") Long empId,
		    @Param("fromDate") String fromDate,
		    @Param("toDate") String toDate);
	
	@Query("SELECT DISTINCT NEW com.apmosys.employeeportal.dto.ProjectNameAndPrjoectIdDTO(projectId,projectName)\n"
			+ " from Project where active = 'true' ")
	public Optional<List<ProjectNameAndPrjoectIdDTO>> getActiveProjectList();
	
	
	@Query(nativeQuery = true,value="SELECT em.email AS email\n"
			+ "FROM project_manager_mapping pmm\n"
			+ "INNER JOIN employee em \n"
			+ "    ON em.emp_id = pmm.project_manager_id\n"
			+ "WHERE pmm.project_id = :projectId and pmm.active =1\n"
			+ "\n"
			+ "UNION \n"
			+ "\n"
			+ "SELECT eo.email AS email\n"
			+ "FROM project_overhead_mapping pom\n"
			+ "INNER JOIN employee eo \n"
			+ "    ON eo.emp_id = pom.project_overhead_id\n"
			+ "WHERE pom.project_id = :projectId and pom.active =1\n"
			+ "\n"
			+ "UNION\n"
			+ "\n"
			+ "select hod.email from project_department_map pdm\n"
			+ " inner join projects p on p.project_id= pdm.project_id \n"
			+ " inner join department d on pdm.dept_id = d.dept_id\n"
			+ " inner join employee hod on hod.emp_id = d.hod_id\n"
			+ " where pdm.active = 1 and p.project_id = :projectId \n"
			+ "\n"
			+ "\n"
			+ "\n"
			+ "\n"
			+ "\n"
			+ "")
	List<String> findProjectManagerAndProjectoverheadEmails(@Param("projectId") Integer projectId);
	
	
	
	
	 @Query(value = "SELECT count(distinct p.project_id)\n"
	    		+ "FROM projects p\n"
	    		+ "inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
	    		+ " inner JOIN department d ON pd.dept_id = d.dept_id \n"
	    		+ " inner JOIN teams t ON p.project_id = t.project_id \n"
	    		+ " inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
	    		+ " LEFT JOIN clients c ON p.client_id = c.client_id \n"
	    		+ " LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
	    		+ " LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
	    		+ " LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
	    		+ " LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
	    		+ "WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' \n"
	    		+ "     and d.dept_id in (:deptIds)\n"
	    		+ "    and p.internal_project_type is not null\n"
	    		+ "    ;" , nativeQuery = true)
	    Integer getAllInternalProjectsCount(List<Long> deptIds);
	 
	 @Query(value = " WITH RelevantProjects AS (\n"
	 		+ "	select distinct p.project_id,p.project_name, po_no, p.client_id, p.po_project_id, p.active,po_project_type, \n"
	 		+ "     group_concat(distinct e1.name order by e1.name separator ', ') as Project_Manager,\n"
	 		+ "	 c.client_name, clientrm, p.dept_id,apmosysrm, date(po_start_date) po_start_date, date(po_end_date) po_end_date,\n"
	 		+ "	 p.state, p.created_on, p.status PO_project_status,p.project_completion_date,p.project_status Ishine_project_status, p.internal_project_type,\n"
	 		+ "	  CASE \n"
	 		+ "	 WHEN p.is_draft_project = 'true' THEN 'Pending For Approval' \n"
	 		+ "	 WHEN p.is_draft_project = 'false' THEN 'Approved' \n"
	 		+ "	 WHEN p.is_draft_project = 'Rejected' THEN 'Rejected' \n"
	 		+ "	 WHEN p.is_draft_project = 'Completed' THEN 'Completed' \n"
	 		+ "	 WHEN p.is_draft_project is null THEN 'Not Started' \n"
	 		+ "	 ELSE 'Un Mentioned Test Data' \n"
	 		+ "	 END as Approval_status, \n"
	 		+ "	  CASE \n"
	 		+ "	   WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
	 		+ "	   ELSE CAST(p.project_id AS CHAR) \n"
	 		+ "	   END AS projectViewId  \n"
	 		+ "	   FROM projects p\n"
	 		+ "	  left JOIN project_department_map pd ON p.project_id = pd.project_id\n"
	 		+ "	  inner JOIN department d ON pd.dept_id = d.dept_id \n"
	 		+ "	  inner JOIN teams t ON p.project_id = t.project_id \n"
	 		+ "	  inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
	 		+ "	  LEFT JOIN clients c ON p.client_id = c.client_id \n"
	 		+ "	  LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
	 		+ "	  LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n"
	 		+ "      WHERE p.po_project_type = 'TNM'\n"
	 		+ "			AND p.active != 'false'\n"
	 		+ "			AND t.is_active != 'N'\n"
//	 		+ "            --  AND etm.active != 0\n"
	 		+ "	  GROUP BY\n"
	 		+ "	  p.project_id,project_name, po_no,p.client_id, p.po_project_id, p.active, po_project_type, c.client_name, clientrm, p.dept_id, apmosysrm, \n"
	 		+ "	  po_start_date, po_end_date, p.state, p.created_on, p.status,p.project_completion_date,p.project_status, p.internal_project_type \n"
	 		+ "),\n"
	 		+ "\n"
	 		+ "FilledCounts AS (\n"
	 		+ "    SELECT\n"
	 		+ "        etm.resource_overview_id,\n"
	 		+ "        COUNT(DISTINCT etm.emp_id) AS filled_count\n"
	 		+ "    FROM \n"
	 		+ "        employee_team_mapping etm\n"
	 		+ "    INNER JOIN\n"
	 		+ "        teams t ON etm.team_id = t.team_id\n"
	 		+ "    INNER JOIN\n"
	 		+ "        employee e ON etm.emp_id = e.emp_id\n"
	 		+ "     INNER JOIN\n"
	 		+ "        projects p ON p.project_id = t.project_id   \n"
	 		+ "    WHERE 1=1\n"
	 		+ "        -- and etm.active != 0\n"
	 		+ "        AND t.is_active != 'N'\n"
	 		+ "        AND e.employmentstatus != 'InActive'\n"
	 		+ "        and p.po_project_type = 'TNM'\n"
	 		+ "        AND p.active != 'false'\n"
	 		+ "        AND etm.resource_overview_id IS NOT NULL\n"
	 		+ "    GROUP BY \n"
	 		+ "        etm.resource_overview_id\n"
	 		+ "),\n"
	 		+ "\n"
	 		+ "TotalRequirements AS (\n"
	 		+ "	select dept_id, project_id, sum(required_count) as required_count from \n"
	 		+ "	(    SELECT distinct \n"
	 		+ "			d.dept_id, \n"
	 		+ "			rp.project_id,\n"
	 		+ "			rr.count AS required_count,\n"
	 		+ "			role\n"
	 		+ "		FROM\n"
	 		+ "			RelevantProjects rp\n"
	 		+ "		INNER JOIN\n"
	 		+ "			resource_requirement rr ON rp.project_id = rr.project_id \n"
	 		+ "			inner join department d on d.name = rr.department \n"
	 		+ "	) req\n"
	 		+ "		GROUP BY\n"
	 		+ "			dept_id, project_id\n"
	 		+ "),\n"
	 		+ "\n"
	 		+ "UnfilledPositions AS (\n"
	 		+ "    SELECT\n"
	 		+ "        rp.project_id,\n"
	 		+ "        SUM(GREATEST(0, rr.count - IFNULL(fc.filled_count, 0))) AS unfilled_count\n"
	 		+ "    FROM\n"
	 		+ "        RelevantProjects rp\n"
	 		+ "    INNER JOIN\n"
	 		+ "        resource_requirement rr ON rp.project_id = rr.project_id\n"
	 		+ "    LEFT JOIN\n"
	 		+ "        FilledCounts fc ON rr.resource_overview_id = fc.resource_overview_id\n"
	 		+ "    GROUP BY\n"
	 		+ "        rp.project_id\n"
	 		+ ")\n"
	 		+ "\n"
	 		+ "SELECT count(distinct rp.project_id)  \n"
	 		+ "FROM\n"
	 		+ "    RelevantProjects rp \n"
	 		+ "INNER JOIN\n"
	 		+ "    TotalRequirements tr ON rp.project_id = tr.project_id\n"
	 		+ "INNER JOIN \n"
	 		+ "    UnfilledPositions up ON rp.project_id = up.project_id\n"
	 		+ "    where IFNULL(tr.required_count, 0) > IFNULL(up.unfilled_count, 0)\n"
	 		+ "    and rp.dept_id in (:deptIds) \n"
	 		+ "    and up.unfilled_count > 0", 
		        nativeQuery = true)
		Integer getUnfilledPositionsCount(@Param("deptIds") List<Long> deptIds);
	 
	 @Query(value = "WITH RelevantProjects AS (  \n"
	 		+ "	select distinct p.project_id,p.project_name, po_no, p.client_id, p.po_project_id, p.active,po_project_type, \n"
	 		+ "     group_concat(distinct e1.name order by e1.name separator ', ') as Project_Manager,\n"
	 		+ "	 c.client_name, clientrm, p.dept_id,apmosysrm, date(po_start_date) po_start_date, date(po_end_date) po_end_date,\n"
	 		+ "	 p.state, p.created_on, p.status PO_project_status,p.project_completion_date,p.project_status Ishine_project_status, p.internal_project_type,\n"
	 		+ "	  CASE \n"
	 		+ "	 WHEN p.is_draft_project = 'true' THEN 'Pending For Approval' \n"
	 		+ "	 WHEN p.is_draft_project = 'false' THEN 'Approved' \n"
	 		+ "	 WHEN p.is_draft_project = 'Rejected' THEN 'Rejected' \n"
	 		+ "	 WHEN p.is_draft_project = 'Completed' THEN 'Completed' \n"
	 		+ "	 WHEN p.is_draft_project is null THEN 'Not Started' \n"
	 		+ "	 ELSE 'Un Mentioned Test Data' \n"
	 		+ "	 END as Approval_status, \n"
	 		+ "	  CASE \n"
	 		+ "	   WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
	 		+ "	   ELSE CAST(p.project_id AS CHAR) \n"
	 		+ "	   END AS projectViewId,\n"
	 		+ "	GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ', ') AS department_names \n"
	 		+ "	   FROM projects p\n"
	 		+ "	  left JOIN project_department_map pd ON p.project_id = pd.project_id\n"
	 		+ "	  inner JOIN department d ON pd.dept_id = d.dept_id \n"
	 		+ "	  inner JOIN teams t ON p.project_id = t.project_id \n"
	 		+ "	  inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
	 		+ "	  LEFT JOIN clients c ON p.client_id = c.client_id \n"
	 		+ "	  LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
	 		+ "	  LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n"
	 		+ "      WHERE p.po_project_type = 'TNM'\n"
	 		+ "			AND p.active != 'false'\n"
	 		+ "			AND t.is_active != 'N'\n"
	 		+ "            --  AND etm.active != 0\n"
	 		+ "	  GROUP BY\n"
	 		+ "	  p.project_id,project_name, po_no,p.client_id, p.po_project_id, p.active, po_project_type, c.client_name, clientrm, p.dept_id, apmosysrm, \n"
	 		+ "	  po_start_date, po_end_date, p.state, p.created_on, p.status,p.project_completion_date,p.project_status, p.internal_project_type \n"
	 		+ "),\n"
	 		+ "\n"
	 		+ "FilledCounts AS (\n"
	 		+ "    SELECT\n"
	 		+ "        etm.resource_overview_id,\n"
	 		+ "        COUNT(DISTINCT etm.emp_id) AS filled_count\n"
	 		+ "    FROM \n"
	 		+ "        employee_team_mapping etm\n"
	 		+ "    INNER JOIN\n"
	 		+ "        teams t ON etm.team_id = t.team_id\n"
	 		+ "    INNER JOIN\n"
	 		+ "        employee e ON etm.emp_id = e.emp_id\n"
	 		+ "     INNER JOIN\n"
	 		+ "        projects p ON p.project_id = t.project_id   \n"
	 		+ "    WHERE 1=1\n"
	 		+ "        -- and etm.active != 0\n"
	 		+ "        AND t.is_active != 'N'\n"
	 		+ "        AND e.employmentstatus != 'InActive'\n"
	 		+ "        and p.po_project_type = 'TNM'\n"
	 		+ "        AND p.active != 'false'\n"
	 		+ "        AND etm.resource_overview_id IS NOT NULL\n"
	 		+ "    GROUP BY \n"
	 		+ "        etm.resource_overview_id\n"
	 		+ "),\n"
	 		+ "\n"
	 		+ "TotalRequirements AS (\n"
	 		+ "	select dept_id, project_id, sum(required_count) as required_count from \n"
	 		+ "	(    SELECT distinct \n"
	 		+ "			d.dept_id, \n"
	 		+ "			rp.project_id,\n"
	 		+ "			rr.count AS required_count,\n"
	 		+ "			role\n"
	 		+ "		FROM\n"
	 		+ "			RelevantProjects rp\n"
	 		+ "		INNER JOIN\n"
	 		+ "			resource_requirement rr ON rp.project_id = rr.project_id \n"
	 		+ "			inner join department d on d.name = rr.department \n"
	 		+ "	) req\n"
	 		+ "		GROUP BY\n"
	 		+ "			dept_id, project_id\n"
	 		+ "),\n"
	 		+ "\n"
	 		+ "UnfilledPositions AS (\n"
	 		+ "    SELECT\n"
	 		+ "        rp.project_id,\n"
	 		+ "        SUM(GREATEST(0, rr.count - IFNULL(fc.filled_count, 0))) AS unfilled_count\n"
	 		+ "    FROM\n"
	 		+ "        RelevantProjects rp\n"
	 		+ "    INNER JOIN\n"
	 		+ "        resource_requirement rr ON rp.project_id = rr.project_id\n"
	 		+ "    LEFT JOIN\n"
	 		+ "        FilledCounts fc ON rr.resource_overview_id = fc.resource_overview_id\n"
	 		+ "    GROUP BY \n"
	 		+ "        rp.project_id\n"
	 		+ ")\n"
	 		+ "\n"
	 		+ "SELECT distinct rp.* \n"
	 		+ "FROM \n"
	 		+ "    RelevantProjects rp \n"
	 		+ "INNER JOIN\n"
	 		+ "    TotalRequirements tr ON rp.project_id = tr.project_id\n"
	 		+ "INNER JOIN \n"
	 		+ "    UnfilledPositions up ON rp.project_id = up.project_id\n"
	 		+ "    where IFNULL(tr.required_count, 0) > IFNULL(up.unfilled_count, 0)\n"
	 		+ "    and rp.dept_id in (:deptIds) \n"
	 		+ "    and up.unfilled_count > 0 \n"
	 		+ "ORDER BY\n"
	 		+ "    rp.project_name" , nativeQuery = true)
		public List<Object[]> getAllUnfilledPositionList(List<Long> deptIds);

	    @Query(value = "SELECT count(distinct p.project_id)\n"
	    		+ "FROM projects p\n"
	    		+ "inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
	    		+ " inner JOIN department d ON pd.dept_id = d.dept_id \n"
	    		+ " inner JOIN teams t ON p.project_id = t.project_id \n"
	    		+ " inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
	    		+ " LEFT JOIN clients c ON p.client_id = c.client_id \n"
	    		+ " LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
	    		+ " LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
	    		+ " LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
	    		+ " LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
	    		+ "WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' \n"
	    		+ "      and d.dept_id in (:deptIds)\n"
	    		+ "    and p.po_project_type = 'Monitoring';" , nativeQuery = true)
	    Integer getAllMonitoringProjectCount(List<Long> deptIds);

	    
	    @Query(value = "SELECT\n"
	    		+ "			     distinct p.project_id,project_name, po_no, p.client_id, p.po_project_id, p.active,po_project_type,\n"
	    		+ "			     GROUP_CONCAT(DISTINCT e1.name ORDER BY e1.name SEPARATOR ', ') as Project_Manager,\n"
	    		+ "			     c.client_name, clientrm, p.dept_id,apmosysrm, date(po_start_date) po_start_date, date(po_end_date) po_end_date,\n"
	    		+ "			     p.state, p.created_on, p.status PO_project_status,p.project_completion_date,p.project_status Ishine_project_status, p.internal_project_type,\n"
	    		+ "			      CASE \n"
	    		+ "			     WHEN p.is_draft_project = 'true' THEN 'Pending For Approval' \n"
	    		+ "			     WHEN p.is_draft_project = 'false' THEN 'Approved' \n"
	    		+ "			     WHEN p.is_draft_project = 'Rejected' THEN 'Rejected' \n"
	    		+ "			     WHEN p.is_draft_project = 'Completed' THEN 'Completed' \n"
	    		+ "			     WHEN p.is_draft_project = null THEN 'Not Started' \n"
	    		+ "			     ELSE 'Un Mentioned Test Data' \n"
	    		+ "			     END as Approval_status, \n"
	    		+ "			      CASE \n"
	    		+ "			       WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
	    		+ "			       ELSE CAST(p.project_id AS CHAR) \n"
	    		+ "			       END AS projectViewId , \n"
	    		+ "			     GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ', ') AS department_names\n"
	    		+ "			      FROM projects p\n"
	    		+ "			      inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
	    		+ "			      inner JOIN department d ON pd.dept_id = d.dept_id \n"
	    		+ "			      inner JOIN teams t ON p.project_id = t.project_id \n"
	    		+ "			      inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
	    		+ "			      LEFT JOIN clients c ON p.client_id = c.client_id \n"
	    		+ "			      LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
	    		+ "			      LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
	    		+ "			      LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
	    		+ "			      LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
	    		+ "			      WHERE 1=1\n"
	    		+ "                 and etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
	    		+ "			         and d.dept_id in (:deptIds)\n"
	    		+ "					 and p.po_project_type = 'Monitoring'\n"
	    		+ "				 GROUP BY\n"
	    		+ "			     p.project_id,project_name, po_no,p.client_id, p.po_project_id, p.active, po_project_type, c.client_name, clientrm, p.dept_id, apmosysrm, \n"
	    		+ "			     po_start_date, po_end_date, p.state, p.created_on, p.status,p.project_completion_date,p.project_status, \n"
	    		+ "			     p.internal_project_type" , nativeQuery = true)
		public List<Object[]> getAllMonitoringList(List<Long> deptIds);

		
		@Query(value = "SELECT\n"
				+ "			     distinct p.project_id,project_name, po_no, p.client_id, p.po_project_id, p.active,po_project_type,\n"
				+ "			     GROUP_CONCAT(DISTINCT e1.name ORDER BY e1.name SEPARATOR ', ') as Project_Manager,\n"
				+ "			     c.client_name, clientrm, p.dept_id,apmosysrm, date(po_start_date) po_start_date, date(po_end_date) po_end_date,\n"
				+ "			     p.state, p.created_on, p.status PO_project_status,p.project_completion_date,p.project_status Ishine_project_status, p.internal_project_type,\n"
				+ "			      CASE \n"
				+ "			     WHEN p.is_draft_project = 'true' THEN 'Pending For Approval' \n"
				+ "			     WHEN p.is_draft_project = 'false' THEN 'Approved' \n"
				+ "			     WHEN p.is_draft_project = 'Rejected' THEN 'Rejected' \n"
				+ "			     WHEN p.is_draft_project = 'Completed' THEN 'Completed' \n"
				+ "			     WHEN p.is_draft_project = null THEN 'Not Started' \n"
				+ "			     ELSE 'Un Mentioned Test Data' \n"
				+ "			     END as Approval_status, \n"
				+ "			      CASE \n"
				+ "			       WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
				+ "			       ELSE CAST(p.project_id AS CHAR) \n"
				+ "			       END AS projectViewId , \n"
				+ "			     GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ', ') AS department_names\n"
				+ "			      FROM projects p\n"
				+ "			      inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
				+ "			      inner JOIN department d ON pd.dept_id = d.dept_id \n"
				+ "			      inner JOIN teams t ON p.project_id = t.project_id \n"
				+ "			      inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
				+ "			      LEFT JOIN clients c ON p.client_id = c.client_id \n"
				+ "			      LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
				+ "			      LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
				+ "			      LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
				+ "			      LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
				+ "			      WHERE 1=1\n"
				+ "                 and etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
				+ "			         and d.dept_id in (:deptIds)\n"
				+ "					and p.internal_project_type is not null\n"
				+ "				 GROUP BY\n"
				+ "			     p.project_id,project_name, po_no,p.client_id, p.po_project_id, p.active, po_project_type, c.client_name, clientrm, p.dept_id, apmosysrm, \n"
				+ "			     po_start_date, po_end_date, p.state, p.created_on, p.status,p.project_completion_date,p.project_status, \n"
				+ "			     p.internal_project_type" , nativeQuery = true)
		public List<Object[]> getAllInternalList(List<Long> deptIds);
		
		
		@Query(value="WITH\n"
				+ "   All_Applicable_Projects AS (\n"
				+ "        SELECT DISTINCT\n"
				+ "            p.project_id,\n"
				+ "            p.project_name,\n"
				+ "            p.po_no\n"
				+ "        FROM\n"
				+ "            projects p\n"
				+ "        INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n"
				+ "        INNER JOIN department d ON pd.dept_id = d.dept_id\n"
				+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
				+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
				+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
				+ "        INNER JOIN clients c ON p.client_id = c.client_id\n"
				+ "        WHERE\n"
				+ "            etm.active != 0\n"
				+ "            AND t.is_active != 'N'\n"
				+ "            AND p.active != 'false'\n"
				+ "            AND p.has_client_side_id = 1\n"
				+ "            AND DATE(etm.start_date) < CURDATE()\n"
				+ "    ) ,  Authorized_Projects AS (\n"
				+ "        SELECT DISTINCT project_id FROM (\n"
				+ "            SELECT p.project_id\n"
				+ "             FROM projects p\n"
				+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
				+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
				+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
				+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
				+ "        LEFT JOIN employee_client_side_id_mapping ecsm ON ecsm.emp_id = e.emp_id AND ecsm.project_id = p.project_id\n"
				+ "        WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
				+ "          AND p.has_client_side_id = true AND e.employmentstatus != 'InActive'\n"
				+ "            and EXISTS (\n"
				+ "                SELECT 1 FROM employee e\n"
				+ "                JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
				+ "                JOIN department d ON jr.dept_id = d.dept_id\n"
				+ "                WHERE e.emp_id = :emp_id AND (\n"
				+ "            jr.employee_role IN ('SuperAdmin')\n"
				+ "            OR d.name IN ('HR', 'Accounts', 'Resource Management Group')\n"
				+ "      )\n"
				+ "            )\n"
				+ "\n"
				+ "            UNION\n"
				+ "\n"
				+ "            SELECT p.project_id\n"
				+ "            FROM projects p\n"
				+ "            JOIN teams t ON p.project_id = t.project_id\n"
				+ "            JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
				+ "            JOIN employee e ON etm.emp_id = e.emp_id\n"
				+ "            JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
				+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
				+ "            AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
				+ "			AND p.has_client_side_id = true\n"
				+ "\n"
				+ "            UNION\n"
				+ "\n"
				+ "            SELECT p.project_id\n"
				+ "            FROM projects p\n"
				+ "            LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
				+ "            LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
				+ "            LEFT JOIN teams t ON p.project_id = t.project_id\n"
				+ "            LEFT JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
				+ "            WHERE 1=1 and\n"
				+ "            (\n"
				+ "			  pm.project_manager_id = :emp_id\n"
				+ "              OR pom.project_overhead_id = :emp_id\n"
				+ "              OR t.spoc_id = :emp_id\n"
				+ "              OR t.team_lead_id = :emp_id\n"
				+ "              OR etm.emp_id = :emp_id\n"
				+ "			)\n"
				+ "            AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
				+ "			AND p.has_client_side_id = true\n"
				+ "        ) AS projects_list\n"
				+ "    )\n"
				+ "SELECT\n"
				+ "    aap.project_name,\n"
				+ "    aap.po_no,\n"
				+ "    aap.project_id\n"
				+ "FROM\n"
				+ "    All_Applicable_Projects aap\n"
				+ "INNER JOIN\n"
				+ "    Authorized_Projects ap ON aap.project_id = ap.project_id",nativeQuery = true)
		public List<Object[]> getProjectWithCliendSideID(@Param("emp_id") Long emp_id);
		
		@Query(value="WITH\n"
				+ "   All_Applicable_Projects AS (\n"
				+ "        SELECT DISTINCT\n"
				+ "            p.project_id,\n"
				+ "            p.project_name,\n"
				+ "            p.po_no\n"
				+ "        FROM\n"
				+ "            projects p\n"
				+ "        INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n"
				+ "        INNER JOIN department d ON pd.dept_id = d.dept_id\n"
				+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
				+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
				+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
				+ "        INNER JOIN clients c ON p.client_id = c.client_id\n"
				+ "        WHERE\n"
				+ "            etm.active != 0\n"
				+ "            AND t.is_active != 'N'\n"
				+ "            AND p.active != 'false'\n"
				+ "            AND DATE(etm.start_date) < CURDATE()\n"
				+ "    ) ,  Authorized_Projects AS (\n"
				+ "        SELECT DISTINCT project_id FROM (\n"
				+ "            SELECT p.project_id\n"
				+ "             FROM projects p\n"
				+ "        INNER JOIN teams t ON p.project_id = t.project_id\n"
				+ "        INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
				+ "        INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
				+ "        INNER JOIN clients c ON c.client_id = p.client_id\n"
				+ "        LEFT JOIN employee_client_side_id_mapping ecsm ON ecsm.emp_id = e.emp_id AND ecsm.project_id = p.project_id\n"
				+ "        WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
				+ "         AND e.employmentstatus != 'InActive'\n"
				+ "            and EXISTS (\n"
				+ "                SELECT 1 FROM employee e\n"
				+ "                JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
				+ "                JOIN department d ON jr.dept_id = d.dept_id\n"
				+ "                WHERE e.emp_id = :emp_id AND (\n"
				+ "            jr.employee_role IN ('SuperAdmin')\n"
				+ "            OR d.name IN ('HR', 'Accounts', 'Resource Management Group')\n"
				+ "      )\n"
				+ "            )\n"
				+ "\n"
				+ "            UNION\n"
				+ "\n"
				+ "            SELECT p.project_id\n"
				+ "            FROM projects p\n"
				+ "            JOIN teams t ON p.project_id = t.project_id\n"
				+ "            JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
				+ "            JOIN employee e ON etm.emp_id = e.emp_id\n"
				+ "            JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
				+ "            WHERE jr.dept_id IN (SELECT dept_id FROM department WHERE hod_id = :emp_id)\n"
				+ "            AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
				+ "			AND p.has_client_side_id = true\n"
				+ "\n"
				+ "            UNION\n"
				+ "\n"
				+ "            SELECT p.project_id\n"
				+ "            FROM projects p\n"
				+ "            LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
				+ "            LEFT JOIN project_overhead_mapping pom ON p.project_id = pom.project_id\n"
				+ "            LEFT JOIN teams t ON p.project_id = t.project_id\n"
				+ "            LEFT JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
				+ "            WHERE 1=1 and\n"
				+ "            (\n"
				+ "			  pm.project_manager_id = :emp_id\n"
				+ "              OR pom.project_overhead_id = :emp_id\n"
				+ "              OR t.spoc_id = :emp_id\n"
				+ "              OR t.team_lead_id = :emp_id\n"
				+ "              OR etm.emp_id = :emp_id\n"
				+ "			)\n"
				+ "            AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
				+ "        ) AS projects_list\n"
				+ "    )\n"
				+ "SELECT\n"
				+ "    aap.project_name,\n"
				+ "    aap.po_no,\n"
				+ "    aap.project_id\n"
				+ "FROM\n"
				+ "    All_Applicable_Projects aap\n"
				+ "INNER JOIN\n"
				+ "    Authorized_Projects ap ON aap.project_id = ap.project_id",nativeQuery = true)
		public List<Object[]> getAllEmpProjectWithID(@Param("emp_id") Long emp_id);

		@Query(nativeQuery = true, value = " Select distinct p.project_id,p.project_name, po_no, p.client_id, p.po_project_id, p.active,po_project_type,\n"
				+ "			     GROUP_CONCAT(DISTINCT e1.name ORDER BY e1.name SEPARATOR ', ') as Project_Manager,\n"
				+ "			     c.client_name, clientrm, p.dept_id,apmosysrm, date(po_start_date) po_start_date, date(po_end_date) po_end_date,\n"
				+ "			     p.state, p.created_on, p.status PO_project_status,p.project_completion_date,p.project_status Ishine_project_status, p.internal_project_type,\n"
				+ "			      CASE \n"
				+ "			     WHEN p.is_draft_project = 'true' THEN 'Pending For Approval' \n"
				+ "			     WHEN p.is_draft_project = 'false' THEN 'Approved' \n"
				+ "			     WHEN p.is_draft_project = 'Rejected' THEN 'Rejected' \n"
				+ "			     WHEN p.is_draft_project = 'Completed' THEN 'Completed' \n"
				+ "			     WHEN p.is_draft_project = null THEN 'Not Started' \n"
				+ "			     ELSE 'Un Mentioned Test Data' \n"
				+ "			     END as Approval_status, \n"
				+ "			      CASE \n"
				+ "			       WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
				+ "			       ELSE CAST(p.project_id AS CHAR) \n"
				+ "			       END AS projectViewId , \n"
				+ "			     GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ', ') AS department_names\n"
				+ "			      FROM projects p\n"
				+ "			      inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
				+ "			      inner JOIN department d ON pd.dept_id = d.dept_id \n"
				+ "			      inner JOIN teams t ON p.project_id = t.project_id \n"
				+ "			      inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
				+ "			      inner JOIN clients c ON p.client_id = c.client_id \n"
				+ "			      inner JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
				+ "			      LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
				+ "			      LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
				+ "			      LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
				+ "                  left JOIN milestone_updated_logs m1 on m1.project_id = p.project_id\n"
				+ "			      WHERE\n"
				+ "			     po_project_type = 'Fixed Cost'\n"
				+ "                 and etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
				+ "			     -- AND DATE(p.po_end_date) < CURDATE()\n"
				+ "				and d.dept_id in (:deptId) \n"
				+ "                and p.project_id not in (select project_id from milestone_updated_logs)\n"
				+ "				 GROUP BY\n"
				+ "			     p.project_id,project_name, po_no,p.client_id, p.po_project_id, p.active, po_project_type, c.client_name, clientrm, p.dept_id, apmosysrm, \n"
				+ "			     po_start_date, po_end_date, p.state, p.created_on, p.status,p.project_completion_date,p.project_status, \n"
				+ "			     p.internal_project_type")
	    List<Object[]> findOntimeFCList(@Param("deptId") List<Long> deptId);
	    
	    
	    @Query(nativeQuery = true, value = "SELECT\n"
	    		+ "			     distinct p.project_id,project_name, po_no, p.client_id, p.po_project_id, p.active,po_project_type,\n"
	    		+ "			     GROUP_CONCAT(DISTINCT e1.name ORDER BY e1.name SEPARATOR ', ') as Project_Manager,\n"
	    		+ "			     c.client_name, clientrm, p.dept_id,apmosysrm, date(po_start_date) po_start_date, date(po_end_date) po_end_date,\n"
	    		+ "			     p.state, p.created_on, p.status PO_project_status,p.project_completion_date,p.project_status Ishine_project_status, p.internal_project_type,\n"
	    		+ "			      CASE \n"
	    		+ "			     WHEN p.is_draft_project = 'true' THEN 'Pending For Approval' \n"
	    		+ "			     WHEN p.is_draft_project = 'false' THEN 'Approved' \n"
	    		+ "			     WHEN p.is_draft_project = 'Rejected' THEN 'Rejected' \n"
	    		+ "			     WHEN p.is_draft_project = 'Completed' THEN 'Completed' \n"
	    		+ "			     WHEN p.is_draft_project = null THEN 'Not Started' \n"
	    		+ "			     ELSE 'Un Mentioned Test Data' \n"
	    		+ "			     END as Approval_status, \n"
	    		+ "			      CASE \n"
	    		+ "			       WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
	    		+ "			       ELSE CAST(p.project_id AS CHAR) \n"
	    		+ "			       END AS projectViewId , \n"
	    		+ "			     GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ', ') AS department_names\n"
	    		+ "			      FROM projects p\n"
	    		+ "			      inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
	    		+ "			      inner JOIN department d ON pd.dept_id = d.dept_id \n"
	    		+ "			      inner JOIN teams t ON p.project_id = t.project_id \n"
	    		+ "			      inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
	    		+ "			      inner JOIN clients c ON p.client_id = c.client_id \n"
	    		+ "			      inner JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
	    		+ "			      LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
	    		+ "			      LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
	    		+ "			      LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
	    		+ "			      WHERE\n"
	    		+ "			     po_project_type = 'Fixed Cost'\n"
	    		+ "                 and etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
	    		+ "			      AND DATE(p.po_end_date) < CURDATE()\n"
	    		+ "			         and d.dept_id in (:deptId)\n"
	    		+ "				 GROUP BY\n"
	    		+ "			     p.project_id,project_name, po_no,p.client_id, p.po_project_id, p.active, po_project_type, c.client_name, clientrm, p.dept_id, apmosysrm, \n"
	    		+ "			     po_start_date, po_end_date, p.state, p.created_on, p.status,p.project_completion_date,p.project_status, \n"
	    		+ "			     p.internal_project_type")
	    List<Object[]> findExpiredFixedCostProjects(@Param("deptId") List<Long> deptId);
      
      @Query(nativeQuery = true, value = "SELECT\n"
	   		+ "					 distinct p.project_id,project_name, po_no, p.client_id, p.po_project_id, p.active,po_project_type,\n"
	   		+ "					 GROUP_CONCAT(DISTINCT e1.name ORDER BY e1.name SEPARATOR ', ') as Project_Manager,\n"
	   		+ "					 c.client_name, clientrm, p.dept_id,apmosysrm, date(po_start_date) po_start_date, date(po_end_date) po_end_date,\n"
	   		+ "					 p.state, p.created_on, p.status PO_project_status,p.project_completion_date,p.project_status Ishine_project_status, p.internal_project_type,\n"
	   		+ "					  CASE \n"
	   		+ "					 WHEN p.is_draft_project = 'true' THEN 'Pending For Approval' \n"
	   		+ "					 WHEN p.is_draft_project = 'false' THEN 'Approved' \n"
	   		+ "					 WHEN p.is_draft_project = 'Rejected' THEN 'Rejected' \n"
	   		+ "					 WHEN p.is_draft_project = 'Completed' THEN 'Completed' \n"
	   		+ "					 WHEN p.is_draft_project = null THEN 'Not Started' \n"
	   		+ "					 ELSE 'Un Mentioned Test Data' \n"
	   		+ "					 END as Approval_status, \n"
	   		+ "					  CASE \n"
	   		+ "					   WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
	   		+ "					   ELSE CAST(p.project_id AS CHAR) \n"
	   		+ "					   END AS projectViewId , \n"
	   		+ "					 GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ', ') AS department_names\n"
	   		+ "					  FROM projects p\n"
	   		+ "					  inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
	   		+ "					  inner JOIN department d ON pd.dept_id = d.dept_id \n"
	   		+ "					  inner JOIN teams t ON p.project_id = t.project_id \n"
	   		+ "					  inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
	   		+ "					  inner JOIN clients c ON p.client_id = c.client_id \n"
	   		+ "					  inner JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
	   		+ "					  LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
	   		+ "					  LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
	   		+ "					  LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
	   		+ "					  WHERE\n"
	   		+ "					 po_project_type = 'Fixed Cost'\n"
	   		+ "					 and etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
	   		+ "						and d.dept_id in (:deptId) \n"
	   		+ "					 GROUP BY\n"
	   		+ "					 p.project_id,project_name, po_no,p.client_id, p.po_project_id, p.active, po_project_type, c.client_name, clientrm, p.dept_id, apmosysrm, \n"
	   		+ "					 po_start_date, po_end_date, p.state, p.created_on, p.status,p.project_completion_date,p.project_status, \n"
	   		+ "					 p.internal_project_type")
	    List<Object[]> findAllFixedCostProjects(@Param("deptId") List<Long> deptId);
      
	    
	    @Query(nativeQuery = true,value = "select dept_id from department;")
		List<Long> deptIds();
	    
	    
	    @Query(nativeQuery = true, value = "SELECT count(distinct p.project_id)\n"
				+ "FROM projects p\n"
				+ "inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
				+ " inner JOIN department d ON pd.dept_id = d.dept_id \n"
				+ " inner JOIN teams t ON p.project_id = t.project_id \n"
				+ " inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
				+ " inner JOIN clients c ON p.client_id = c.client_id \n"
				+ " inner JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
				+ " LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
				+ " LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
				+ " LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
				+ "WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and po_project_type = 'Fixed Cost'\n"
				+ "and d.dept_id in (:deptId)")
		Long totalFcCount(@Param("deptId") List<Long> deptId);
		
		@Query(nativeQuery = true,value = "SELECT count(distinct p.project_id)\n"
				+ "FROM projects p\n"
				+ "inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
				+ " inner JOIN department d ON pd.dept_id = d.dept_id \n"
				+ " inner JOIN teams t ON p.project_id = t.project_id \n"
				+ " inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
				+ " inner JOIN clients c ON p.client_id = c.client_id \n"
				+ " inner JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
				+ " LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
				+ " LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
				+ " LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
				+ "WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and po_project_type = 'Fixed Cost'\n"
				+ "    AND DATE(p.po_end_date) < CURDATE() and d.dept_id in (:deptId)")
		Long expiredFCcount(@Param("deptId") List<Long> deptId);
    
    @Query(value = "SELECT\n"
	    		+ "			     count(distinct p.project_id)\n"
	    		+ "			      FROM projects p\n"
	    		+ "			      inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
	    		+ "			      inner JOIN department d ON pd.dept_id = d.dept_id \n"
	    		+ "			      inner JOIN teams t ON p.project_id = t.project_id \n"
	    		+ "			      inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
	    		+ "			      inner JOIN clients c ON p.client_id = c.client_id \n"
	    		+ "			      inner JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
	    		+ "			      LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
	    		+ "			      LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
	    		+ "			      LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
	    		+ "                  inner JOIN milestone_updated_logs m1 on m1.project_id = p.project_id\n"
	    		+ "			      WHERE\n"
	    		+ "			     po_project_type = 'Fixed Cost'\n"
	    		+ "                 and etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
	    		+ "			     -- AND DATE(p.po_end_date) < CURDATE()\n"
	    		+ "				 and d.dept_id in (:deptId)" , nativeQuery = true)
		Long getAllDelayedProjectCount(@Param("deptId") List<Long> deptId);
    
        
    @Query(value = "SELECT\n"
    		+ "			     count(distinct p.project_id)\n"
    		+ "			      FROM projects p\n"
    		+ "			      inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
    		+ "			      inner JOIN department d ON pd.dept_id = d.dept_id \n"
    		+ "			      inner JOIN teams t ON p.project_id = t.project_id \n"
    		+ "			      inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
    		+ "			      inner JOIN clients c ON p.client_id = c.client_id \n"
    		+ "			      inner JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
    		+ "			      LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
    		+ "			      LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
    		+ "			      LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
    		+ "                  left join milestone_updated_logs m1 on m1.project_id = p.project_id\n"
    		+ "			      WHERE\n"
    		+ "			     po_project_type = 'Fixed Cost'\n"
    		+ "                 and etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'	\n"
    		+ "                 and p.project_id not in (select project_id from milestone_updated_logs)\n"
    		+ "			     -- AND DATE(p.po_end_date) < CURDATE()\n"
    		+ "					and d.dept_id in (:deptId) \n"
    		+ "" , nativeQuery = true)
	Long getAllOntimeCount(@Param("deptId") List<Long> deptId);
    
    
    
    @Query(nativeQuery = true, value = "SELECT\n"
			+ "			     distinct p.project_id,p.project_name, po_no, p.client_id, p.po_project_id, p.active,po_project_type,\n"
			+ "			     GROUP_CONCAT(DISTINCT e1.name ORDER BY e1.name SEPARATOR ', ') as Project_Manager,\n"
			+ "			     c.client_name, clientrm, p.dept_id,apmosysrm, date(po_start_date) po_start_date, date(po_end_date) po_end_date,\n"
			+ "			     p.state, p.created_on, p.status PO_project_status,p.project_completion_date,p.project_status Ishine_project_status, p.internal_project_type,\n"
			+ "			      CASE \n"
			+ "			     WHEN p.is_draft_project = 'true' THEN 'Pending For Approval' \n"
			+ "			     WHEN p.is_draft_project = 'false' THEN 'Approved' \n"
			+ "			     WHEN p.is_draft_project = 'Rejected' THEN 'Rejected' \n"
			+ "			     WHEN p.is_draft_project = 'Completed' THEN 'Completed' \n"
			+ "			     WHEN p.is_draft_project = null THEN 'Not Started' \n"
			+ "			     ELSE 'Un Mentioned Test Data' \n"
			+ "			     END as Approval_status, \n"
			+ "			      CASE \n"
			+ "			       WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id)\n"
			+ "			       ELSE CAST(p.project_id AS CHAR) \n"
			+ "			       END AS projectViewId , \n"
			+ "			     GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ', ') AS department_names\n"
			+ "			      FROM projects p\n"
			+ "			      inner JOIN project_department_map pd ON p.project_id = pd.project_id\n"
			+ "			      inner JOIN department d ON pd.dept_id = d.dept_id \n"
			+ "			      inner JOIN teams t ON p.project_id = t.project_id \n"
			+ "			      inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n"
			+ "			      inner JOIN clients c ON p.client_id = c.client_id \n"
			+ "			      inner JOIN project_manager_mapping pm on p.project_id = pm.project_id\n"
			+ "			      LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id\n"
			+ "			      LEFT JOIN job_role j1 on j1.job_role_id = e1.job_role_id \n"
			+ "			      LEFT JOIN department d1 on d1.dept_id = j1.dept_id\n"
			+ "                  inner JOIN milestone_updated_logs m1 on m1.project_id = p.project_id\n"
			+ "			      WHERE\n"
			+ "			     po_project_type = 'Fixed Cost'\n"
			+ "                 and etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
			+ "			     -- AND DATE(p.po_end_date) < CURDATE()\n"
			+ "				and d.dept_id in (:deptId)\n"
			+ "				 GROUP BY\n"
			+ "			     p.project_id,project_name, po_no,p.client_id, p.po_project_id, p.active, po_project_type, c.client_name, clientrm, p.dept_id, apmosysrm, \n"
			+ "			     po_start_date, po_end_date, p.state, p.created_on, p.status,p.project_completion_date,p.project_status, \n"
			+ "			     p.internal_project_type")
    List<Object[]> findDelayedFCProject(@Param("deptId") List<Long> deptId);
    
    @Query(value = "select count(distinct p.project_id) from projects p\n"
    		+ "INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n"
    		+ "INNER JOIN department d ON pd.dept_id = d.dept_id\n"
    		+ "INNER JOIN teams t ON p.project_id = t.project_id\n"
    		+ "INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
    		+ "INNER JOIN clients c ON p.client_id = c.client_id\n"
    		+ "WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
    		+ "and d.dept_id in (:deptIds)" , nativeQuery = true)
    Integer getTotalAllActiveProjectCount(@Param("deptIds") List<Long> deptIds);
    
    @Query(value = "select count(distinct p.project_id) from projects p\n"
			+ "INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n"
			+ "INNER JOIN department d ON pd.dept_id = d.dept_id\n"
			+ "INNER JOIN teams t ON p.project_id = t.project_id\n"
			+ "INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ "INNER JOIN clients c ON p.client_id = c.client_id\n"
			+ "WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false'\n"
			+ "and internal_project_type is not null\n"
			+ "and d.dept_id in (:deptIds)" , nativeQuery = true)
	Integer getAllInternalActiveProjectsCount(@Param("deptIds") List<Long> deptIds);
    
    
    @Query(value = "WITH all_client_projects AS (\n"
    		+ "    SELECT d.dept_id, c.client_id, d.name, c.client_name, COUNT(DISTINCT p.project_id) AS total_projects\n"
    		+ "    FROM projects p \n"
    		+ "    INNER JOIN clients c ON p.client_id = c.client_id\n"
    		+ "    INNER JOIN teams t ON t.project_id = p.project_id\n"
    		+ "    INNER JOIN project_department_map pd ON pd.project_id = p.project_id\n"
    		+ "    INNER JOIN department d ON d.dept_id = pd.dept_id\n"
    		+ "    GROUP BY d.dept_id, c.client_id, d.name, c.client_name\n"
    		+ "),\n"
    		+ "active_projects AS (\n"
    		+ "    SELECT d.dept_id, c.client_id, d.name, c.client_name, \n"
    		+ "			COUNT(DISTINCT p.project_id) AS total_active_projects, \n"
    		+ "            COUNT(DISTINCT e.emp_id) AS total_active_resources \n"
    		+ "    FROM projects p\n"
    		+ "    INNER JOIN clients c ON p.client_id = c.client_id\n"
    		+ "    INNER JOIN teams t ON t.project_id = p.project_id \n"
    		+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
    		+ "    INNER JOIN employee e on etm.emp_id = e.emp_id \n"
    		+ "    INNER JOIN project_department_map pd ON pd.project_id = p.project_id\n"
    		+ "    INNER JOIN department d ON d.dept_id = pd.dept_id\n"
    		+ "    WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and e.employmentstatus != 'InActive'\n"
    		+ "    GROUP BY d.dept_id, c.client_id, d.name, c.client_name\n"
    		+ "),\n"
    		+ "in_active_projects AS (\n"
    		+ "    SELECT d.dept_id, c.client_id, d.name, c.client_name, COUNT(DISTINCT p.project_id) AS total_inactive_projects\n"
    		+ "    FROM projects p\n"
    		+ "    INNER JOIN clients c ON p.client_id = c.client_id\n"
    		+ "    INNER JOIN teams t ON t.project_id = p.project_id \n"
    		+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
    		+ "    INNER JOIN project_department_map pd ON pd.project_id = p.project_id\n"
    		+ "    INNER JOIN department d ON d.dept_id = pd.dept_id\n"
    		+ "    WHERE t.is_active = 'N'\n"
    		+ "		  and not exists (select 1 from teams t1 where t.project_id = t1.project_id and t1.is_active != 'N')\n"
    		+ "    GROUP BY d.dept_id, c.client_id, d.name, c.client_name\n"
    		+ ")\n"
    		+ "SELECT acp.dept_id, acp.client_id, acp.name, acp.client_name, \n"
    		+ "       IFNULL(acp.total_projects, 0) AS total_projects, \n"
    		+ "       IFNULL(ap.total_active_projects, 0) AS total_active_projects, \n"
    		+ "       IFNULL(ap.total_active_resources, 0) AS total_active_resources, \n"
    		+ "       IFNULL(iap.total_inactive_projects, 0) AS total_inactive_projects\n"
    		+ "FROM all_client_projects acp\n"
    		+ "LEFT JOIN active_projects ap ON acp.dept_id = ap.dept_id AND acp.client_id = ap.client_id\n"
    		+ "LEFT JOIN in_active_projects iap ON iap.dept_id = acp.dept_id AND iap.client_id = acp.client_id\n"
    		+ "WHERE acp.dept_id in :deptIds\n"
    		+ "ORDER BY acp.name, acp.client_name",
    	    nativeQuery = true)
    	List<Object[]> getClientAndProjectData(@Param("deptIds") List<Long> deptIds);
    	
    	@Query(value ="WITH active_projects AS ( \n"
    			+ "    SELECT d.dept_id, c.client_id, d.name, c.client_name, \n"
    			+ "		   p.project_id, p.project_name, p.po_no, p.po_project_type, \n"
    			+ "           GROUP_CONCAT(distinct e1.name order by e1.emp_id separator ', ') as project_manager, \n"
    			+ "           p.apmosysrm, p.clientrm, p.po_start_date, p.po_end_date, \n"
    			+ "           p.created_on, p.po_project_id, 'active' as project_type,\n"
    			+ "            CASE \n"
    			+ "            WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id) \n"
    			+ "            ELSE CAST(p.project_id AS CHAR) \n"
    			+ "        END AS projectViewId"
    			+ "    FROM projects p\n"
    			+ "    INNER JOIN clients c ON p.client_id = c.client_id\n"
    			+ "    INNER JOIN teams t ON t.project_id = p.project_id \n"
    			+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
    			+ "    INNER JOIN employee e on etm.emp_id = e.emp_id \n"
    			+ "    INNER JOIN project_department_map pd ON pd.project_id = p.project_id \n"
    			+ "    INNER JOIN project_manager_mapping pm on pm.project_id = p.project_id \n"
    			+ "    INNER JOIN employee e1 on e1.emp_id = pm.project_manager_id \n"
    			+ "    INNER JOIN department d ON d.dept_id = pd.dept_id\n"
    			+ "    WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and e.employmentstatus != 'InActive'\n"
    			+ "    GROUP BY d.dept_id, c.client_id, d.name, c.client_name, \n"
    			+ "		   p.project_id, p.project_name, p.po_no, p.po_project_type, \n"
    			+ "           c.client_name, p.apmosysrm, p.clientrm, p.po_start_date, p.po_end_date, \n"
    			+ "           p.created_on, p.po_project_id,projectViewId \n"
    			+ "),\n"
    			+ "in_active_projects AS (\n"
    			+ "    SELECT d.dept_id, c.client_id, d.name, c.client_name, \n"
    			+ "		   p.project_id, p.project_name, p.po_no, p.po_project_type, \n"
    			+ "           GROUP_CONCAT(distinct e1.name order by e1.emp_id separator ', ') as project_manager, \n"
    			+ "           p.apmosysrm, p.clientrm, p.po_start_date, p.po_end_date, \n"
    			+ "           p.created_on, p.po_project_id, 'inactive' as project_type,\n"
    			+ "           CASE WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id) ELSE CAST(p.project_id AS CHAR) END AS projectViewId"
    			+ "    FROM projects p\n"
    			+ "    INNER JOIN clients c ON p.client_id = c.client_id\n"
    			+ "    INNER JOIN teams t ON t.project_id = p.project_id \n"
    			+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
    			+ "    INNER JOIN project_department_map pd ON pd.project_id = p.project_id \n"
    			+ "    INNER JOIN project_manager_mapping pm on pm.project_id = p.project_id \n"
    			+ "    INNER JOIN employee e1 on e1.emp_id = pm.project_manager_id \n"
    			+ "    INNER JOIN department d ON d.dept_id = pd.dept_id\n"
    			+ "    WHERE t.is_active = 'N'\n"
    			+ "		  and not exists (select 1 from teams t1 where t.project_id = t1.project_id and t1.is_active != 'N')\n"
    			+ "    GROUP BY d.dept_id, c.client_id, d.name, c.client_name, \n"
    			+ "		   p.project_id, p.project_name, p.po_no, p.po_project_type, \n"
    			+ "           c.client_name, p.apmosysrm, p.clientrm, p.po_start_date, p.po_end_date, \n"
    			+ "           p.created_on, p.po_project_id,projectViewId \n"
    			+ ")\n"
    			+ "select * from (\n"
    			+ " SELECT *\n"
    			+ " FROM active_projects ap \n"
    			+ " UNION ALL \n"
    			+ " select * from in_active_projects \n"
    			+ ") all_projects \n"
    			+ "where project_type = :projectType\n"
    			+ "and (dept_id in :deptIds) \n"
    			+ "and (client_id in :clientIds)" , nativeQuery = true)
    	List<Object[]> getClientAndProjectDataList(
    		    @Param("projectType") String projectType,
    		    @Param("deptIds") List<Long> deptIds,
    		    @Param("clientIds") List<Long> clientIds);

	@Query(value="SELECT p.po_project_id FROM projects p JOIN project_manager_mapping pmm ON p.project_id = pmm.project_id WHERE pmm.project_manager_id =:projectManagerId AND pmm.active = 1 AND p.po_project_type='Fixed Cost'", nativeQuery = true)
	List<Long> findPoProjectIdsByProjectManagerIdWithJoin(@Param("projectManagerId") Long projectManagerId);
      
	@Query("SELECT rm.email, hod.email " +
		       "FROM Project p " +
		       "LEFT JOIN ProjectManagerMapping pmm ON p.projectId = pmm.projectId " +
		       "LEFT JOIN Employee rm ON pmm.projectManagerId = rm.empId " +
		       "LEFT JOIN Department d ON p.deptId = d.deptId " +
		       "LEFT JOIN Employee hod ON d.hodId = hod.empId " +
		       "WHERE p.poProjectId = :projectId AND pmm.active = 1")
		List<Object[]> findRawRmAndHodEmailsByProjectId(@Param("projectId") Long projectId);

		
		
		 @Query("SELECT DISTINCT e.email FROM Employee e WHERE e.jobRoleId = 53")
		    List<String> findDirectorEmails();

	@Query(value="SELECT distinct p.po_project_id\n"
			+ "	FROM projects p\n"
			+ " inner JOIN teams t ON p.project_id = t.project_id \n"
			+ "	inner JOIN employee_team_mapping etm ON t.team_id = etm.team_id\n"
			+ " WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and po_project_type = 'TNM'", nativeQuery=true)
	public List<Long> getAllTnmProjectsWithActiveTeams();
	
	
	
	@Query("select new com.apmosys.employeeportal.dto.TimeSheetDetailsDto(" +
		       "t.timesheetId, t.projectId, a.teamId, t.empId, t.dayType, t.date, " +
		       "t.officeInTime, t.officeOutTime, t.clientInTime, t.clientOutTime, " +
		       "t.isShadowTimesheet, t.shadowEmpId, tdoc.docId, e.name ,tdoc.clientApprovalStatus) " +
		       "from Timesheet t " +
		       "inner join TimesheetActivityMap tam on tam.timesheetId = t.timesheetId " +
		       "inner join Activity a on a.activityId = tam.activityId " +
		       "left join TimesheetDocumentDetails tdoc on tdoc.timesheetId = t.timesheetId " +
		       "   and tdoc.active = true " +
		       "   and tdoc.docId = (" +
		       "        select max(tdoc2.docId) " +
		       "        from TimesheetDocumentDetails tdoc2 " +
		       "        where tdoc2.timesheetId = t.timesheetId " +
		       "          and tdoc2.active = true" +
		       "   ) " +
		       "left join Employee e on e.empId = t.shadowEmpId " +
		       "where a.teamId = :team_id " +
		       "  and t.empId = :emp_id " +
		       "  and t.date between :startDate and :endDate")
		List<TimeSheetDetailsDto> findByProjectIdAndEmployeeIdAndWorkDateBetween(
		        Long team_id, Long emp_id, LocalDate startDate, LocalDate endDate);

	
	
	@Query("select new com.apmosys.employeeportal.dto.ResourceCountDto(p.poProjectId, count(distinct e.empId)) " +
	       "from Project p " +
	       "inner join Team t on t.projectId = p.projectId " +
	       "inner join  EmployeeTeamMap etm on etm.teamId = t.teamId " +
	       "inner join Employee e on e.empId = etm.empId " +
	       "where p.active = 'true' and t.isActive = 'Y' " +
	       "and etm.active = 1 and e.employmentstatus != 'InActive' " +
	       "and p.poProjectType in ('TNM') " +
	       "AND p.poProjectId in :projectIds " +
	       "group by p.projectId")
	List<ResourceCountDto> getResourceCounts(@Param("projectIds") List<Long> projectIds);
	
	@Modifying
    @Transactional
    @Query("UPDATE Project p " +
           "SET p.active = 'true' " +
           "WHERE p.active = 'false' " +
           "AND p.projectId = :projectId" )
    int updateProjectActiveField(@Param("projectId") Integer projectId);
	
	@Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p " +
		       "INNER JOIN Team t ON t.projectId = p.projectId " +
		       "INNER JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId " +
		       "WHERE p.projectId = :projectId " +
		       "AND p.poProjectType = 'TNM' " +
		       "AND p.poProjectId IS NOT NULL " +
		       "AND t.isActive = 'Y' " +
		       "AND etm.active != 0")
	boolean existsEligibleProject(@Param("projectId") Integer projectId);
	
	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.ProjectFetchDTO(\n"
			+ "		    p.projectId, \n"
			+ "		    p.createdOn, \n"
			+ "		    p.projectName, \n"
			+ "		    p.state, \n"
			+ "		    p.clientId, \n"
			+ "		    p.poProjectId, \n"
			+ "		    p.active, \n"
			+ "		    p.syncProject, \n"
			+ "		    p.createdBy, \n"
			+ "		    p.updatedBy, \n"
			+ "		    p.updatedOn, \n"
			+ "		    p.isDraftProject, \n"
			+ "		    p.poEndDate, \n"
			+ "		    p.poNo, \n"
			+ "		    p.poProjectType, \n"
			+ "		    p.poStartDate, \n"
			+ "		    p.apmosysRM, \n"
			+ "		    p.clientRM, \n"
			+ "		    p.deptId, \n"
			+ "		    p.isRenewable, \n"
			+ "		    p.status, \n"
			+ "		    p.apmosysRmEmail, \n"
			+ "		    p.projectCompletionDate, \n"
			+ "		    p.projectStatus, \n"
			+ "		    p.internalProjectType, \n"
			+ "		    c.clientName, \n"
			+ "		    CASE \n"
			+ "		        WHEN p.isDraftProject = 'true' THEN 'Pending For Approval' \n"
			+ "		        WHEN p.isDraftProject = 'false' THEN 'Approved' \n"
			+ "		        WHEN p.isDraftProject = 'Rejected' THEN 'Rejected' \n"
			+ "		        WHEN p.isDraftProject = 'Completed' THEN 'Completed' \n"
			+ "		        WHEN p.isDraftProject IS NULL THEN 'Not Started' \n"
			+ "		        ELSE 'Un Mentioned Test Data' \n"
			+ "		    END, \n"
			+ "		    CASE \n"
			+ "		        WHEN p.poProjectId IS NOT NULL THEN CONCAT('po', CAST(p.poProjectId AS string)) \n"
			+ "		        ELSE CAST(p.poProjectId AS string) \n"
			+ "		    END AS projectViewId \n"
			+ "		) \n"
			+ "		FROM Project p \n"
			+ "		LEFT JOIN Team t ON p.projectId = t.projectId \n"
			+ "		LEFT JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId \n"
			+ "		LEFT JOIN Employee e ON e.empId = etm.empId \n"
			+ "		LEFT JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId \n"
			+ "		LEFT JOIN Department d ON d.deptId = jr.deptId \n"
			+ "		LEFT JOIN ProjectDepartmentMap pdm ON p.projectId = pdm.projectId \n"
			+ "		LEFT JOIN Client c ON c.clientId = p.clientId \n"
			+ "		WHERE ( pdm.deptId is NULL or pdm.deptId IN (:deptIds))")
		List<ProjectFetchDTO> getAllProjectList(@Param("deptIds") List<Long> deptIds);
	
	@Query(value="SELECT COUNT(DISTINCT p.project_id) \n"
			+ "FROM projects p \n"
			+ "LEFT JOIN teams t on p.project_id = t.project_id \n"
			+ "LEFT JOIN employee_team_mapping etm on etm.team_id = t.team_id \n"
			+ "LEFT JOIN employee e on e.emp_id = etm.emp_id \n"
			+ "LEFT JOIN job_role jr on e.job_role_id = jr.job_role_id \n"
			+ "LEFT JOIN department d on d.dept_id = jr.dept_id  \n"
			+ "LEFT JOIN project_department_map pdm on p.project_id = pdm.project_id \n"
			+ "WHERE ( pdm.dept_id is NULL or pdm.dept_id IN (:deptIds))" , nativeQuery = true)
	Integer getAllProjectCount(List<Long> deptIds);
	
	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.SkippedEmployeeDTO(" +
	       "e.empId, e.employeementId, e.name, d.designationName, " +
	       "r.role, r.department, r.experience, hod.email) " +
	       "FROM EmployeeTeamMap etm " +
	       "INNER JOIN Employee e ON etm.empId = e.empId " +
	       "INNER JOIN Designation d ON e.designationId = d.designationId " +
	       "INNER JOIN JobRole jr ON e.jobRoleId = jr.jobRoleId " +
	       "INNER JOIN Department dept ON jr.deptId = dept.deptId " +
	       "INNER JOIN Employee hod ON dept.hodId = hod.empId " +
	       "INNER JOIN ResourceRequirement r ON etm.resourceOverviewId = r.resourceOverviewId " +
	       "WHERE etm.empId IN :empIds AND etm.resourceOverviewId IN :resourceOverviewIds")
	List<SkippedEmployeeDTO> findAllSkippedEmployees(@Param("empIds") Set<Long> empIds, @Param("resourceOverviewIds") Set<Long> resourceOverviewIds);

}
