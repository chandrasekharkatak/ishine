package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.hibernate.query.NativeQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.GetProjectDetailsForBulkDefaultUpdateProjectDTO;
import com.apmosys.employeeportal.dto.GetProjectDetailsForBulkDefaultUpdateTeamDTO;
import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.dto.SummaryChartDTO;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectManagerMapping;

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
	
	@Query(nativeQuery = true)
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
    
	 
	    
	   
	 
	 
	 @Query(value = "select count(distinct etm.empId) " +
             "from Project p " +
             "left join Team t on t.projectId = p.projectId and p.active != 'false' " +
             "left join EmployeeTeamMap etm on etm.teamId = t.teamId and etm.active != 0 and t.isActive != 'N' " +
             "where p.poProjectId = :id")
		public int getAssignedEmployeesCountInProject(Long id);
	 
	 
	 
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
//			+ "AND  e.employmentstatus != 'InActive' \n"
//			+ "AND d.dept_id IN (:deptIds) \n"
//			+ "AND (:deptIds IS NULL OR d.dept_id IN (:deptIds)) \n"
			+ "AND (:isProjectId IS FALSE OR p.projectId IN (:projectId) ) \n"
			+ "AND (:approvalCheck is false OR p.isDraftProject = :approvalStatus ) \n"
			+ "AND (:projectStatus IS NULL OR p.projectStatus = :projectStatus ) \n"
			+ "AND (:status IS NULL OR p.status = :status )")
	List<ProjectFetchDTO> getAllActiveProjectList(@Param("projectStatus")String projectStatus, @Param("status")String status, @Param("approvalStatus")String approvalStatus, Set<Integer> projectId, boolean isProjectId,boolean approvalCheck);
	
	
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
			+ "END ) \n"
			+ "			FROM Project p\n"
			+ "			LEFT JOIN Client c ON c.clientId = p.clientId\n"
			+ "			inner join ProjectDepartmentMap pdm on pdm.projectId = p.projectId\n"
			+ "			WHERE p.active = 'true'\n"
			+ "			AND p.isDraftProject is null \n"
			+ "			and (p.status != 'Completed' or p.projectStatus = 'Not Started') and pdm.deptId IN :deptIds")
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
	
	
	@Query(value = "SELECT COUNT(DISTINCT p.projectId)\n"
			+ "FROM Project p \n"
			+ "LEFT JOIN Team t on p.projectId = t.projectId \n"
			+ "LEFT JOIN EmployeeTeamMap etm on etm.teamId = t.teamId \n"
			+ "LEFT JOIN Employee e on e.empId = etm.empId \n"
			+ "LEFT JOIN JobRole jr on e.jobRoleId = jr.jobRoleId \n"
			+ "LEFT JOIN Department d on d.deptId = jr.deptId \n" 
			+ "WHERE p.active = 'true' \n"
//			+ "AND t.isActive != 'N' \n"
//			+ "AND etm.active != 0 \n" 
//			+ "AND e.employmentstatus != 'InActive' \n"
//			+ "AND (:isDeptFilter = false OR d.deptId IN :deptIds) \n"
//			+ "AND (:deptIds IS NULL OR d.deptId IN :deptIds)\n"
			+ "AND (:approvalStatus IS NULL OR p.isDraftProject =:approvalStatus)\n"
			+ "AND (:projectFilter = false OR p.projectId IN :projectIds)")
	Integer getAllActiveProjecCountstList(@Param("approvalStatus")String approvalStatus, @Param("projectIds")Set<Integer> projectIds,@Param("projectFilter") Boolean projectFilter);

	@Query(value="select  count( Distinct p.projectId) from Project p \n"
			+ " inner join ProjectDepartmentMap pdm on pdm.projectId = p.projectId \n"
			+ " where p.active= 'true' and p.isDraftProject is null and (p.status != 'Completed' or p.projectStatus = 'Not Started') and pdm.deptId IN :deptIds")
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
			+ "END ) \n"
			+ "			FROM Project p\n"
			+ "			LEFT JOIN Client c ON c.clientId = p.clientId\n"
			+ "			inner join ProjectDepartmentMap pdm on pdm.projectId = p.projectId\n"
			+ "			WHERE p.active = 'true'\n"
			+ "			AND p.projectStatus = 'Completed' and pdm.deptId IN :deptIds")
	List<ProjectFetchDTO> getAllCompletedProjectListInIshine(@Param("deptIds") List<Long> deptIds);
	
	
	@Query(value="select  count( Distinct p.projectId) from Project p \n"
			+ "			 inner join ProjectDepartmentMap pdm on pdm.projectId = p.projectId \n"
			+ "			 where p.active= 'true' and  p.projectStatus = 'Completed' and pdm.deptId IN :deptIds")
	Integer getAllCompletedProjectCountInIshine(@Param("deptIds") List<Long> deptIds);
	
	@Transactional
	@Modifying
    @Query(value = "CALL sp_SyncProjectsFromTemp", nativeQuery = true)
    void callSyncProjectsSP();
}
