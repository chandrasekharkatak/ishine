package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.GetActiveProjectDetailsIfMultipleDTO;
import com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO;
import com.apmosys.employeeportal.dto.RMGProjectToEmployeeFlatDTO;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.Project;

@Repository
public interface EmployeeTeamMapRepository extends JpaRepository<EmployeeTeamMap, Long> {

	@Transactional
	void deleteAllByTeamId(Long teamId);
	//added by rahul SIngh
		@Query(nativeQuery = true)
		List<Object[]> findEmployeeByTeamId(Long team_id);

	@Query(nativeQuery = true)
	public List<Object[]> getTeamMembersByTeamId(Long teamId);
	
	@Query(nativeQuery = true, value="select et.emp_id,a.team_id,t.team_name,p.client_id,c.client_name,\n"
			+ "cl.client_location,a.activity,a.eta ,etam.completion_time, em.name\n"
			+ "from employee_timesheets et \n"
			+ "inner join employee_timesheet_activities_mapping etam on etam.timesheet_id = et.timesheet_id\n"
			+ "inner join activities a on etam.activity_id = a.activity_id\n"
			+ "inner join teams t on t.team_id = a.team_id\n"
			+ "inner join projects p on p.project_id = t.project_id\n"
			+ "inner join clients c on c.client_id = p.client_id\n"
			+ "Inner join employee as em on et.emp_id=em.emp_id\n"
			+ "inner join client_locations cl on cl.client_id = p.client_id\n"
			+ "where t.team_id=?  group by et.emp_id")
	public List<Object[]> getTeamMembersByTeamIdBioMax(Long teamId);

	List<EmployeeTeamMap> findByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> findProjectsByTeamId(Long empId);

	Long countByTeamId(Long teamId);

	EmployeeTeamMap findByEmployeeTeamMapId(Long employeeTeamMapId);

	List<EmployeeTeamMap> findByEmpIdNotInAndTeamId(List<Long> teamMemberList, Long teamId);

	List<EmployeeTeamMap> findFirstByEmpIdAndTeamId(Long empId, Long teamId);

	@Query(value="select etm from EmployeeTeamMap etm where etm.empId=:empId and etm.teamId=:teamId and etm.active IN (1,2)")
	List<EmployeeTeamMap> findFirstByEmpIdAndTeamIdAndActive(Long empId, Long teamId);

	List<EmployeeTeamMap> findByTeamId(Long teamId);

	EmployeeTeamMap findByEmpIdAndTeamIdAndActive(Long empId, Long teamId, long l);

//	@Query(nativeQuery = true)
//	EmployeeTeamMap findByEmpIdAndTeamIdAndActiveStatus(Long empId, Long teamId);
	@Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.empId = :empId AND etm.teamId = :teamId AND etm.active != 0")
	EmployeeTeamMap findByEmpIdAndTeamIdAndActiveStatus(@Param("empId") Long empId, 
	                                                          @Param("teamId") Long teamId);

	
	@Query(nativeQuery = true)
	EmployeeTeamMap findByEmpIdAndTeamId(Long empId, Long teamId);
	
//	@Query(nativeQuery = true)
//	List<EmployeeTeamMap> findByTeamIdAndActive(Long teamId);
	
	// @Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.teamId = :teamId AND etm.active IN (1, 2)")
	// List<EmployeeTeamMap> findByTeamIdAndActive(Long teamId);
	@Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.teamId = :teamId AND etm.active IN (1, 2)")
	List<EmployeeTeamMap> findByTeamIdAndActive(@Param("teamId") Long teamId);


	@Query(nativeQuery = true)
	List<Object[]> getAllProjectByEmpId(Long empId);

	@Query(nativeQuery = true)
	List<EmployeeTeamMap> findTeamListByTeamIdAndStatus(Long teamId);
	
	@Query(nativeQuery = true)
	EmployeeTeamMap findEmployeeByTeamIdAndEmpId(Long empId , Long teamId);
	
	EmployeeTeamMap findEmployeeByEmpIdAndActive(Long empId , long l);

	List<EmployeeTeamMap> findEmployeeByEmpId(Long empId);
	
//	@Query(nativeQuery = true)
//	List<Object[]> findEmployeeByProjectId(Integer projectId );
	
//	 @Query(nativeQuery = true)
//List<EmployeeTeamMap> findByProjectIdAndActive(Integer projectId,Long active);
	 
	 @Query("SELECT etm FROM EmployeeTeamMap etm \n"
	 		+ "INNER JOIN Team t ON t.teamId = etm.teamId \n"
	 		+ "WHERE t.projectId = :projectId AND etm.active = :active")
	 List<EmployeeTeamMap> findByProjectIdAndActive(Integer projectId,Long active);
	 
	 
	 @Query("select p from Project p \n"
	 		+ "inner join Team t on t.projectId = p.projectId\n"
	 		+ "inner join EmployeeTeamMap etm on etm.teamId = t.teamId\n"
	 		+ "where p.projectId = :projectId\n"
	 		+ "and p.active = :active")
		List<Project> findByProjectIdAndActiveForDraftProject(Integer projectId, Long active);

//	 @Query(nativeQuery = true)
//	List<EmployeeTeamMap> findTeammembersByTeamIdAndStatus(Long teamId);
	 
	 @Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.teamId = :teamId AND etm.active = 1")
	 List<EmployeeTeamMap> findTeammembersByTeamIdAndStatus(Long teamId);

//	 @Query(nativeQuery = true , value = "SELECT * FROM employee_team_mapping etm WHERE etm.team_id = :teamId")
//	List<EmployeeTeamMap> findTeammembersByTeamId(Long teamId);
	 
	 @Query(nativeQuery = true)
	 List<Object[]> findTeammembersByTeamId(Long teamId);

	 @Query(nativeQuery = true)
	List<Object[]> getAllProjectsAndTeamsDetails(Long empId);

	 @Query(nativeQuery = true)
	List<Object[]> findTeammembersByTeamIdAndManagerId(Long teamId, Long managerId);

	 @Query(nativeQuery = true)
	List<Object[]> getAllProjectsTeamsInfo(Long empId);

	@Modifying
	@Transactional
	@Query(nativeQuery = true)
	void updateActiveFieldToZero(Long key);
	
	
	@Query(value ="SELECT DISTINCT t.project_id\n"
			+ "        FROM employee_team_mapping etm\n"
			+ "        JOIN teams t ON etm.team_id = t.team_id\n"
			+ "        WHERE etm.emp_id = :empId AND etm.active = 1 AND t.is_active = 'Y'",nativeQuery = true)
	List<Long> findDistinctActiveProjectIdsByEmpId(@Param("empId") Long empId);
	
	@Query(value = "SELECT project_name FROM projects WHERE project_id = :projectId", nativeQuery = true)
	String getProjectNameById(@Param("projectId") Long projectId);
	
	
	@Query(value ="select etm.* from employee_team_mapping etm where etm.team_id =:teamId and active != 0",nativeQuery = true)
	 List<EmployeeTeamMap> findByTeamIdWhereEmployeesAreActive(@Param("teamId") Long teamId);
	
	@Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.teamId IN :teamIds AND etm.active !=0")
	List<EmployeeTeamMap> findActiveByTeamIds(@Param("teamIds") List<Long> teamIds);
	
	@Query(value = "SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO(\n"
	        + "e.empId,e.employeementId,e.billable,e.billableType,e.name,d.name \n" 
	        + ",p.projectId,p.projectName,p.poProjectId,p.poStartDate,p.poEndDate,p.apmosysRM,p.clientRM,p.poProjectType,p.poNo \n"
	        + ",c.clientName,t.teamId,t.teamName,t.isActive \n"
	        + ",etm.employeeRole,etm.active,pm.empId,pm.name ) \n"
			+ "FROM EmployeeTeamMap etm\n"
			+ "RIGHT JOIN Employee e ON e.empId = etm.empId \n"
			+ "RIGHT JOIN Team t ON t.teamId = etm.teamId \n"
			+ "INNER JOIN Project p ON p.projectId = t.projectId \n"
			+ "INNER JOIN Client c ON c.clientId = p.clientId \n"
			+ "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId \n"
			+ "INNER JOIN Department d ON d.deptId = jr.deptId \n"
			+ "LEFT JOIN  ProjectManagerMapping pmm ON pmm.projectId = p.projectId \n"
			+ "LEFT JOIN  Employee pm ON pm.empId = pmm.projectManagerId\n"
			+ "WHERE p.projectId IN :projectIds \n"
			+ "AND etm.active != 0 \n"
			+ "AND t.isActive = 'Y' \n"
			+ "AND e.employmentstatus != 'InActive' \n"
			+ "AND pmm.active = 1 AND e.empId NOT BETWEEN 1 AND 6 ")
	List<RMGFlatEmployeeProjectTeamDTO>  findEmployeeProjectTeamDetailsByProjectIds(@Param("projectIds") Set<Integer> projectIds);
	
	
	
	@Query("SELECT p.projectId FROM Project p JOIN ProjectManagerMapping pm ON p.projectId = pm.projectId WHERE p.active = 'true' AND pm.projectManagerId = :empId")
	Set<Integer> findProjectsByProjectManager(@Param("empId") Long empId);
	
	@Query("SELECT p.projectId FROM Project p JOIN ProjectManagerMapping pm ON p.projectId = pm.projectId WHERE p.active = 'true' AND pm.projectManagerId = :empId AND p.poProjectId IS NULL")
	Set<Integer> findInternalProjectsByProjectManager(@Param("empId") Long empId);
	
	@Query("SELECT p.projectId FROM Project p JOIN ProjectManagerMapping pm ON p.projectId = pm.projectId WHERE p.active = 'true' AND pm.projectManagerId = :empId AND p.poProjectId IS NOT NULL")
	Set<Integer> findShankhProjectsByProjectManager(@Param("empId") Long empId);
	
	
	@Query("SELECT p.projectId FROM Project p JOIN ProjectOverheadMapping po ON p.projectId = po.projectId WHERE p.active = 'true' AND po.projectOverheadId = :empId")
	Set<Integer> findProjectsByOverhead(@Param("empId") Long empId);
	
	@Query("SELECT p.projectId FROM Project p JOIN ProjectOverheadMapping po ON p.projectId = po.projectId WHERE p.active = 'true' AND po.projectOverheadId = :empId AND p.poProjectId IS NULL")
	Set<Integer> findInternalProjectsByOverhead(@Param("empId") Long empId);
	
	@Query("SELECT p.projectId FROM Project p JOIN ProjectOverheadMapping po ON p.projectId = po.projectId WHERE p.active = 'true' AND po.projectOverheadId = :empId AND p.poProjectId IS NOT NULL")
	Set<Integer> findShankhProjectsByOverhead(@Param("empId") Long empId);

	
	@Query("SELECT p.projectId FROM Project p JOIN Team t ON p.projectId = t.projectId WHERE p.active = 'true' AND t.isActive = 'Y' AND (t.spocId = :empId OR t.teamLeadId = :empId)")
	Set<Integer> findProjectsBySpocOrTeamLead(@Param("empId") Long empId);
	
	@Query("SELECT p.projectId FROM Project p JOIN Team t ON p.projectId = t.projectId WHERE p.active = 'true' AND t.isActive = 'Y' AND (t.spocId = :empId OR t.teamLeadId = :empId) AND p.poProjectId IS NULL")
	Set<Integer> findInternalProjectsBySpocOrTeamLead(@Param("empId") Long empId);
	
	@Query("SELECT p.projectId FROM Project p JOIN Team t ON p.projectId = t.projectId WHERE p.active = 'true' AND t.isActive = 'Y' AND (t.spocId = :empId OR t.teamLeadId = :empId) AND p.poProjectId IS NOT NULL")
	Set<Integer> findShankhProjectsBySpocOrTeamLead(@Param("empId") Long empId);



@Query(value = "SELECT \n"
		+ "    e.emp_id, \n"
		+ "    e.employeement_id, \n"
		+ "    e.billable, \n"
		+ "    e.billable_type, \n"
		+ "    e.name, \n"
		+ "    d.name AS departmentname,\n"
		+ "    p.project_id, \n"
		+ "    p.project_name, \n"
		+ "    p.po_project_id, \n"
		+ "    p.po_start_date, \n"
		+ "    p.po_end_date,\n"
		+ "    p.apmosysrm, \n"
		+ "    p.clientrm, \n"
		+ "    p.po_project_type, \n"
		+ "    p.po_no, \n"
		+ "    c.client_name,\n"
		+ "    t.team_id, \n"
		+ "    t.team_name, \n"
		+ "    t.is_active, \n"
		+ "    etm.employee_role, \n"
		+ "    etm.active,  \n"
		+ "    pm.emp_id AS project_manager_id, \n"
		+ "    pm.name AS project_manager_name\n"
		+ "FROM \n"
		+ "    employee_team_mapping etm\n"
		+ "RIGHT JOIN \n"
		+ "    employee e ON e.emp_id = etm.emp_id\n"
		+ "RIGHT JOIN \n"
		+ "    teams t ON t.team_id = etm.team_id\n"
		+ "INNER JOIN \n"
		+ "    projects p ON p.project_id = t.project_id\n"
		+ "INNER JOIN \n"
		+ "    clients c ON c.client_id = p.client_id\n"
		+ "INNER JOIN \n"
		+ "    job_role jr ON jr.job_role_id = e.job_role_id\n"
		+ "INNER JOIN \n"
		+ "    department d ON d.dept_id = jr.dept_id\n"
		+ "LEFT JOIN \n"
		+ "    project_manager_mapping pmm ON pmm.project_id = p.project_id\n"
		+ "LEFT JOIN \n"
		+ "    employee pm ON pm.emp_id = pmm.project_manager_id\n"
		+ "WHERE \n"
		+ "    p.project_id IN :projectIds\n"
		+ "    AND etm.active != 0 \n"
		+ "    AND t.is_active = 'Y' \n"
		+ "    AND e.employmentstatus != 'InActive' \n"
		+ "    AND pmm.active = 1 AND d.dept_id IN :deptIds AND e.emp_id NOT BETWEEN 1 AND 6",
nativeQuery = true)
List<Object[]> findEmployeeProjectTeamDetailsByProjectIdsAndDepartment(@Param("projectIds") Set<Integer> projectIds,@Param("deptIds")List<Long> deptIds);







@Query("SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO(" +
	       "e.empId, e.employeementId, e.billable, e.billableType, e.name, d.name, " +
	       "p.projectId, p.projectName, p.poProjectId, p.poStartDate, p.poEndDate, " +
	       "p.apmosysRM, p.clientRM, p.poProjectType, p.poNo, c.clientName, " +
	       "t.teamId, t.teamName, t.isActive, etm.employeeRole, etm.active, " +
	       "pm.empId, pm.name) " +
	       
	       "FROM EmployeeTeamMap etm " +
	       "RIGHT JOIN Employee e ON e.empId = etm.empId " +
	       "RIGHT JOIN Team t ON t.teamId = etm.teamId " +
	       "INNER JOIN Project p ON p.projectId = t.projectId " +
	       "INNER JOIN Client c ON c.clientId = p.clientId " +
	       "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId " +
	       "INNER JOIN Department d ON d.deptId = jr.deptId " +
	       "LEFT JOIN ProjectManagerMapping pmm ON pmm.projectId = p.projectId " +
	       "LEFT JOIN Employee pm ON pm.empId = pmm.projectManagerId " +
	       
	       "WHERE e.empId IN ( " +
	           "SELECT e1.empId " +
	           "FROM EmployeeTeamMap etm1 " +
	           "JOIN Employee e1 ON e1.empId = etm1.empId " +
	           "JOIN Team t1 ON t1.teamId = etm1.teamId " +
	           "JOIN Project p1 ON p1.projectId = t1.projectId " +
	           "JOIN ProjectManagerMapping pmm1 ON pmm1.projectId = p1.projectId " +
	           "WHERE etm1.active != 0 AND t1.isActive = 'Y' AND e1.employmentstatus != 'InActive' " +
	           "AND pmm1.active = 1 AND p1.projectId IN :projectIds " +
	           "GROUP BY e1.empId " +
	           "HAVING COUNT(CASE WHEN p1.poProjectId IS NULL THEN 1 END) > 0 " +
	           "AND COUNT(CASE WHEN p1.poProjectId IS NOT NULL THEN 1 END) > 0" +
	       ") " +
	       
	       "AND etm.active != 0 " +
	       "AND t.isActive = 'Y' " +
	       "AND e.employmentstatus != 'InActive' " +
	       "AND pmm.active = 1 " +
	       "AND e.empId NOT BETWEEN 1 AND 6")
	List<RMGFlatEmployeeProjectTeamDTO> findEmployeeProjectTeamDetailsMatchedBothProjects(@Param("projectIds") Set<Integer> projectIds);





// @Query(nativeQuery = true,value ="select etm.emp_id from employee_team_mapping etm inner join teams t on t.team_id = etm.team_id inner join  projects p on p.project_id = t.project_id where etm.is_shadow = 1 and etm.active !=0 and t.is_active = 'Y' and p.project_id = :projectId and etm.emp_id IN :empIds")
// List<Long> findShadowMembersByEmpIdsAndProjectId(@Param("empIds") List<Long> empIds, @Param("projectId") Integer projectId);


@Query(value ="select etm.empId from EmployeeTeamMap etm inner join Team t on t.teamId = etm.teamId inner join Project p on p.projectId = t.projectId where etm.isShadow = 1 and etm.active !=0 and t.isActive = 'Y' and p.projectId =:projectId and etm.empId IN :empIds")
List<Long> findShadowMembersByEmpIdsAndProjectId(@Param("empIds") List<Long> empIds, @Param("projectId") Integer projectId);

 
 
 

	 @Query("SELECT new com.apmosys.employeeportal.dto.GetActiveProjectDetailsIfMultipleDTO(p.projectId, p.projectName) " +
		       "FROM Project p " +
		       "inner join Team t on t.projectId = p.projectId " +
		       "inner join EmployeeTeamMap etm on t.teamId = etm.teamId " +
		       "WHERE etm.active != 0 " +
		       "AND t.isActive != 'N' " +
		       "AND p.active != 'false' " +
		       "AND etm.empId = :empId " +
		       "AND p.projectId != :currentProjectId")
	 List<GetActiveProjectDetailsIfMultipleDTO> getActiveProjectIdAndProjectNameByEmpId(Long empId, Integer currentProjectId);
	//  @Query(nativeQuery = true)
	//  List<Map<String, Object>> getActiveProjectIdAndProjectNameByEmpId(Long empId, Integer currentProjectId);
	 
	 
	 
	 
	 @Query(nativeQuery = true,value ="select distinct p.project_id,p.project_name,p.apmosysrm, p.clientrm,p.po_start_date,\n"
	 		+ "p.po_end_date,p.po_no,p.po_project_type,c.client_name client_name,pm.emp_id project_manager_id,pm.name project_manager,t.team_id,t.team_name,e.emp_id,e.name,jr.name job_role,d.name department,\n"
	 		+ "e.mobile_no,e.email,e.billable,e.billable_type,etm.start_date effective_start_date,e.employeement_id\n"
	 		+ "from projects p \n"
	 		+ "inner join teams t on t.project_id = p.project_id\n"
	 		+ "inner join employee_team_mapping etm on t.team_id = etm.team_id\n"
	 		+ "left join project_manager_mapping pmm on p.project_id = pmm.project_id\n"
	 		+ "inner join employee pm on pmm.project_manager_id = pm.emp_id\n"
	 		+ "inner join employee e on e.emp_id = etm.emp_id\n"
	 		+ "inner join clients c on c.client_id = p.client_id\n"
	 		+ "inner join job_role jr on jr.job_role_id = e.job_role_id\n"
	 		+ "inner join department d on jr.dept_id = d.dept_id\n"
	 		+ "where p.project_id not in (\n"
	 		+ "select p.project_id from employee_timesheets et \n"
	 		+ "inner join employee_timesheet_activities_mapping etam on et.timesheet_id = etam.timesheet_id\n"
	 		+ "inner join activities a on a.activity_id = etam.activity_id\n"
	 		+ "inner join teams t on t.team_id = a.team_id \n"
	 		+ "inner join projects p on p.project_id = t.project_id\n"
	 		+ "WHERE (\n"
	 		+ "        ( et.date >= :fromDate)\n"
	 		+ "        AND\n"
	 		+ "        ( et.date <= :toDate)\n"
	 		+ "    )\n"
	 		+ ")\n"
	 		+ "and p.active = 'true' and p.project_id IN :projectIds\n"
	 		+ "and t.is_active = 'Y'\n"
	 		+ "and etm.active != 0\n"
	 		+ "and e.employmentstatus != 'InActive'")
	 List<Object[]> findNonComplianceProjectss(@Param("projectIds") Set<Integer> projectIds,@Param("fromDate")LocalDate fromDate,@Param("toDate")LocalDate toDate);
	 
	 
//	 @Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.RMGProjectToEmployeeFlatDTO(" +
//		       "p.projectId, p.projectName, p.apmosysRM, p.clientRM, p.poStartDate, p.poEndDate, " +
//		       "p.poNo, p.poProjectType, c.clientName, pm.empId, pm.name, t.teamId, t.teamName, " +
//		       "e.empId, e.name, jr.name, d.name, e.mobileNo, e.email, e.billable, e.billableType, " +
//		       "etm.startDate, e.employeementId) " +
//		       "FROM Project p " +
//		       "INNER JOIN Team t ON t.projectId = p.projectId " +
//		       "INNER JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId " +
//		       "LEFT JOIN ProjectManagerMapping pmm ON p.projectId = pmm.projectId " +
//		       "INNER JOIN Employee pm ON pm.empId = pmm.projectManagerId " +
//		       "INNER JOIN Employee e ON e.empId = etm.empId " +
//		       "INNER JOIN Client c ON c.clientId = p.clientId " +
//		       "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId " +
//		       "INNER JOIN Department d ON jr.deptId = d.deptId " +
//		       "WHERE p.projectId NOT IN (" +
//		       "  SELECT p2.projectId FROM Timesheet et " +
//		       "  INNER JOIN TimesheetActivityMap etam ON et.timesheetId = etam.timesheetId " +
//		       "  INNER JOIN Activity a ON a.activityId = etam.activityId " +
//		       "  INNER JOIN Team t2 ON t2.teamId = a.teamId " +
//		       "  INNER JOIN Project p2 ON p2.projectId = t2.projectId " +
//		       "  WHERE et.date >= :fromDate AND et.date <= :toDate" +
//		       ") " +
//		       "AND p.active = 'true' " +
//		       "AND p.projectId IN :projectIds " +
//		       "AND t.isActive = 'Y' " +
//		       "AND etm.active != 0 " +
//		       "AND e.employmentstatus != 'InActive'")
//		List<RMGProjectToEmployeeFlatDTO> findNonComplianceProjects(@Param("projectIds") Set<Integer> projectIds,@Param("fromDate") LocalDate fromDate,@Param("toDate") LocalDate toDate);

	 @Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.RMGProjectToEmployeeFlatDTO(" +
		       "p.projectId, p.projectName, p.apmosysRM, p.clientRM, " +
		       "p.poStartDate, p.poEndDate, p.poNo, p.poProjectType, " +
		       "c.clientName, " +                         
		       "pm.empId, pm.name, " +
		       "t.teamId, t.teamName, " +
		       "e.empId, e.name, " +
		       "jr.name, d.name, e.mobileNo, e.email, " +
		       "e.billable, e.billableType, " +
		       "etm.startDate, " +
		       "e.employeementId" +
		       ") " +
		       "FROM Project p " +
		       "INNER JOIN Team t ON t.projectId = p.projectId " +
		       "INNER JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId " +
		       "LEFT JOIN ProjectManagerMapping pmm ON p.projectId = pmm.projectId " +
		       "INNER JOIN Employee pm ON pm.empId = pmm.projectManagerId " +
		       "INNER JOIN Employee e ON e.empId = etm.empId " +
		       "INNER JOIN Client c ON c.clientId = p.clientId " +
		       "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId " +
		       "INNER JOIN Department d ON jr.deptId = d.deptId " +
		       "WHERE p.projectId NOT IN (" +
		       "  SELECT p2.projectId FROM Timesheet et " +
		       "  INNER JOIN TimesheetActivityMap etam ON et.timesheetId = etam.timesheetId " +
		       "  INNER JOIN Activity a ON a.activityId = etam.activityId " +
		       "  RIGHT JOIN Team t2 ON t2.teamId = a.teamId " +
		       "  INNER JOIN Project p2 ON p2.projectId = t2.projectId " +
		       "  WHERE et.date >= :fromDate AND et.date <= :toDate" +
		       ") " +
		       "AND p.active = 'true' " +
		       "AND p.projectId IN :projectIds " +
		       "AND t.isActive = 'Y' " +
		       "AND etm.active != 0 " +
		       "AND e.employmentstatus != 'InActive'")
		List<RMGProjectToEmployeeFlatDTO> findNonComplianceProjects(
		    @Param("projectIds") Set<Integer> projectIds,
		    @Param("fromDate") LocalDate fromDate,
		    @Param("toDate") LocalDate toDate
		);
	 
}
