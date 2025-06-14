package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Set;

import org.hibernate.query.NativeQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.ProjectFetchDTO;
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
	
	@Query(nativeQuery = true,value ="select p.project_id, p.project_name, p.is_draft_project,(case when exists (select 1 from employee_team_mapping etm where etm.team_id in \n"
			+ "(select team_id from teams where project_id=p.project_id) and etm.active=2) then 2 else 1 end), p.project_status from projects p  \n"
			+ "where p.is_draft_project IN ('false','true','Rejected') and p.project_id IN :projectIds")
	public List<Object[]> findAllProjectByIsDraftAndIsActiveOfProjectIds(@Param("projectIds") Set<Integer> projectIds);
	

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
	
	
	 @Query(nativeQuery = true,value ="select project_id from projects where active = 'true' and po_project_id IS NULL")
	 Set<Integer> findAllActiveInternalProjectIds();
	 
	 @Query(nativeQuery = true,value ="select project_id from projects where active = 'true' and po_project_id IS NOT NULL")
	 Set<Integer> findAllActiveShankhProjectIds();
	 
	 @Query(nativeQuery = true,value ="select project_id from projects where active = 'true'")
	 Set<Integer> findAllActiveShankhInternalProjectIds();
	 
	@Query(nativeQuery = true)
	public int getAssignedEmployeesCountInProject(Long id);
	
	@Query(nativeQuery = true)
	public List<Object[]> getEmployeeInformation(Long empId);

	@Query(nativeQuery = true)
	public List<Object[]> getExceptionEmployeeReport();
	
	@Query(nativeQuery = true)
	public List<Object[]> getExceptionEmployeeReportInDepartments(List<Long> deptIds);
	
	
	@Query(nativeQuery = true)
	List <Object[]> getExceptionEmployeeReportInDepartment(Long deptId);
	
	@Query(value = "SELECT \n"
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
			+ "LEFT JOIN project_temp pt ON p.po_project_id = pt.po_project_id\n"
			+ "LEFT JOIN clients c on p.client_id = c.client_id \n"
			+ "INNER JOIN teams t on p.project_id = t.project_id\n"
			+ "INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id\n"
			+ "INNER JOIN employee e on e.emp_id = etm.emp_id \n"
			+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
			+ "INNER JOIN department d on d.dept_id = jr.dept_id\n"
			+ "WHERE p.active = 'true'  \n"
			+ "AND  e.employmentstatus != 'InActive' \n"
			+ "AND d.dept_id IN (:deptIds) \n"
//			+ "AND (:deptIds IS NULL OR d.dept_id IN (:deptIds)) \n"
			+ "AND (:approvalStatus IS NULL OR p.is_draft_project = :approvalStatus ) \n"
			+ "AND (:projectStatus IS NULL OR p.project_status = :projectStatus )"
			+ "AND (:status IS NULL OR p.status = :status )",
	       nativeQuery = true)
	List<Object[]> getAllActiveProjectList(@Param("deptIds") List<Long> deptIds,@Param("projectStatus")String projectStatus, @Param("status")String status, @Param("approvalStatus")String approvalStatus);
	
	
	@Query(value = "SELECT \n"
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
			+ "LEFT JOIN project_temp pt ON p.po_project_id = pt.po_project_id\n"
			+ "LEFT JOIN clients c on p.client_id = c.client_id \n"
			+ "INNER JOIN teams t on p.project_id = t.project_id\n"
			+ "INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id\n"
			+ "INNER JOIN employee e on e.emp_id = etm.emp_id \n"
			+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
			+ "INNER JOIN department d on d.dept_id = jr.dept_id\n"
			+ "WHERE p.active = 'true' \n"
			+ " AND e.employmentstatus != 'InActive' \n"
			+ "AND d.dept_id IN (:deptIds) \n"
//			+ "AND (:deptIds IS NULL OR d.dept_id IN (:deptIds)) \n"
			+ "AND (:approvalStatus IS NULL OR p.is_draft_project = :approvalStatus ) \n"
			+ "AND (:projectStatus IS NULL OR p.project_status = :projectStatus )"
			+ "AND (:status IS NULL OR p.status = :status )",
	       nativeQuery = true)
	List<Object[]> getAllActiveCompletedProjectList(@Param("deptIds") List<Long> deptIds,@Param("projectStatus")String projectStatus, @Param("status")String status, @Param("approvalStatus")String approvalStatus);
	
	@Query(value = "SELECT COUNT(DISTINCT p.project_id) AS distinct_project_count\n"
			+ "FROM projects p\n"
			+ "LEFT JOIN project_temp pt ON p.po_project_id = pt.po_project_id\n"
			+ "INNER JOIN teams t on p.project_id = t.project_id\n"
			+ "INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id\n"
			+ "INNER JOIN employee e on e.emp_id = etm.emp_id \n"
			+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
			+ "INNER JOIN department d on d.dept_id = jr.dept_id\n"
			+ "WHERE p.active = 'true'  \n"
			+ "AND e.employmentstatus != 'InActive' \n"
			+ "AND d.dept_id IN (:deptIds)\n"
			+ "AND (:approvalStatus IS NULL OR p.is_draft_project =:approvalStatus)",
	       nativeQuery = true)
	Integer getAllActiveProjecCountstList(@Param("deptIds") List<Long> deptIds,@Param("approvalStatus")String approvalStatus);


	@Query(value = "SELECT COUNT(DISTINCT p.project_id) AS distinct_project_count\n"
			+ "FROM projects p\n"
			+ "LEFT JOIN project_temp pt ON p.po_project_id = pt.po_project_id\n"
			+ "INNER JOIN teams t on p.project_id = t.project_id\n"
			+ "INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id\n"
			+ "INNER JOIN employee e on e.emp_id = etm.emp_id \n"
			+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
			+ "INNER JOIN department d on d.dept_id = jr.dept_id\n"
			+ "WHERE p.active = 'true'  \n"
			+ "AND e.employmentstatus != 'InActive' \n"
			+ "AND d.dept_id IN (:deptIds) \n"
//			+ "AND (:deptIds IS NULL OR d.dept_id IN (:deptIds)) \n"
			+ "AND (:projectStatus IS NULL OR p.project_status = :projectStatus )",
	       nativeQuery = true)
	Integer getAllCompleteProjectInIshineCountstList(@Param("deptIds") List<Long> deptIds,@Param("projectStatus")String projectStatus);

	@Query(value = "SELECT COUNT(DISTINCT p.project_id) AS distinct_project_count\n"
			+ "FROM projects p\n"
			+ "LEFT JOIN project_temp pt ON p.po_project_id = pt.po_project_id\n"
			+ "INNER JOIN teams t on p.project_id = t.project_id\n"
			+ "INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id\n"
			+ "INNER JOIN employee e on e.emp_id = etm.emp_id \n"
			+ "INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
			+ "INNER JOIN department d on d.dept_id = jr.dept_id\n"
			+ "WHERE p.active = 'true' \n"
			+ "AND e.employmentstatus != 'InActive' \n"
			+ "AND d.dept_id IN (:deptIds) \n"
//			+ "AND (:deptIds IS NULL OR d.dept_id IN (:deptIds)) \n"
			+ "AND p.status = 'Completed' ",
	       nativeQuery = true)
	Integer getAllCompleteProjectInShankhCountstList(@Param("deptIds") List<Long> deptIds);
	
	@Query(value="SELECT COUNT(DISTINCT p.project_id) AS distinct_project_count\n"
			+ "			FROM projects p\n"
			+ "			LEFT JOIN project_temp pt ON p.po_project_id = pt.po_project_id\n"
			+ "			INNER JOIN teams t on p.project_id = t.project_id\n"
			+ "			INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id\n"
			+ "			INNER JOIN employee e on e.emp_id = etm.emp_id \n"
			+ "			INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
			+ "			INNER JOIN department d on d.dept_id = jr.dept_id\n"
			+ "			WHERE p.active = 'true' \n"
			+ "            AND t.is_active != 'N' \n"
			+ "			AND etm.active != 0 \n"
			+ "            AND e.employmentstatus != 'InActive' \n"
			+ "			AND d.dept_id IN (:deptIds) \n"
			+ "			AND  p.status = 'Completed' ", nativeQuery=true)
	Integer completedInSankhButTeamMapped(@Param("deptIds") List<Long> deptIds);
	
	
	@Query(value="SELECT \n"
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
			+ "			FROM projects p\n"
			+ "			LEFT JOIN project_temp pt ON p.po_project_id = pt.po_project_id\n"
			+ "			LEFT JOIN clients c on p.client_id = c.client_id \n"
			+ "			INNER JOIN teams t on p.project_id = t.project_id\n"
			+ "			INNER JOIN employee_team_mapping etm on etm.team_id = t.team_id\n"
			+ "			INNER JOIN employee e on e.emp_id = etm.emp_id \n"
			+ "			INNER JOIN job_role jr on e.job_role_id = jr.job_role_id\n"
			+ "			INNER JOIN department d on d.dept_id = jr.dept_id\n"
			+ "			WHERE p.active = 'true' \n"
			+ "            AND t.is_active != 'N' \n"
			+ "			AND etm.active != 0 \n"
			+ "            AND e.employmentstatus != 'InActive' \n"
			+ "			AND d.dept_id IN (:deptIds) \n"
			+ "			AND  p.status = 'Completed' ", nativeQuery=true)
	List<Object[]> completedInSankhButTeamMappedList(@Param("deptIds") List<Long> deptIds);
	
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
	
	@Query(nativeQuery = true)
	public List<Object[]> getProjectDetailsForBulkDefaultUpdateBench();
	
	@Query(nativeQuery = true)
	public List<Object[]> getProjectDetailsForBulkDefaultUpdateOther();
	
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
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NOT NULL AND pm.project_manager_id = 14\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN project_overhead_mapping po ON p.project_id = po.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NOT NULL AND po.project_overhead_id = 14\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN teams t ON p.project_id = t.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NOT NULL\n"
			+ "          AND t.is_active = 'Y' AND t.spoc_id = 14\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN teams t ON p.project_id = t.project_id\n"
			+ "        WHERE p.active = 'true' AND p.po_project_id IS NOT NULL\n"
			+ "          AND t.is_active = 'Y' AND t.team_lead_id = 14\n"
			+ "    )")
	Set<Integer> findShankhProjectsByManagerOverheadOrSpocOrTeamLead(@Param("empId") Long empId);
	
	@Query(nativeQuery = true,value="(\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN project_manager_mapping pm ON p.project_id = pm.project_id\n"
			+ "        WHERE p.active = 'true'  AND pm.project_manager_id = 14\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN project_overhead_mapping po ON p.project_id = po.project_id\n"
			+ "        WHERE p.active = 'true' AND po.project_overhead_id = 14\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (\n"
			+ "        SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN teams t ON p.project_id = t.project_id\n"
			+ "        WHERE p.active = 'true' \n"
			+ "          AND t.is_active = 'Y' AND t.spoc_id = 14\n"
			+ "    )\n"
			+ "    UNION\n"
			+ "    (SELECT p.project_id\n"
			+ "        FROM projects p\n"
			+ "        JOIN teams t ON p.project_id = t.project_id\n"
			+ "        WHERE p.active = 'true'\n"
			+ "          AND t.is_active = 'Y' AND t.team_lead_id = 14\n"
			+ "    )")
	Set<Integer> findAllShankhInternalProjectsByManagerOverheadOrSpocOrTeamLead(@Param("empId") Long empId);
	
	
	
	
	@Query(value = "SELECT DISTINCT p.project_name, COUNT(DISTINCT et.timesheet_id) AS total_timesheets_filled " +
            "FROM employee e " +
            "INNER JOIN employee_team_mapping etm USING (emp_id) " +
            "INNER JOIN teams t USING (team_id) " +
            "INNER JOIN projects p USING (project_id) " +
            "INNER JOIN employee_timesheets et ON et.emp_id = e.emp_id " +
            "INNER JOIN employee_timesheet_activities_mapping etam ON etam.timesheet_id = et.timesheet_id " +
            "INNER JOIN activities a ON etam.activity_id = a.activity_id AND t.team_id = a.team_id " +
            "WHERE e.emp_id = :empId " +
            "GROUP BY e.emp_id, p.project_name", 
    nativeQuery = true)
	public List<Object[]> getProjectTimesheetSummaryByEmpId(@Param("empId") Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getBenchEmployeeMoreThan30DaysInDeptIds(List<Long> deptIds);
	
	@Query(nativeQuery = true)
	public List<Object[]> getBenchEmployeeMoreThan30DaysInDeptId(Long deptId);
}
