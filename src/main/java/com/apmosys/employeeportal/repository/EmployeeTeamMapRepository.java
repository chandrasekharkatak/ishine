package com.apmosys.employeeportal.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmpMappingDTO;
import com.apmosys.employeeportal.dto.EmployeeImpactDTO;
import com.apmosys.employeeportal.dto.GetActiveProjectDetailsIfMultipleDTO;
import com.apmosys.employeeportal.dto.GetClientDetailsByProjectIdAndEmpIdDTO;
import com.apmosys.employeeportal.dto.PoDetailsDto;
import com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO;
import com.apmosys.employeeportal.dto.RMGProjectToEmployeeFlatDTO;
import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.response.TeamTimesheetDetailsResponse;

@Repository
public interface EmployeeTeamMapRepository extends JpaRepository<EmployeeTeamMap, Long> {

	@Transactional
	void deleteAllByTeamId(Long teamId);
	
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
			+ "INNER JOIN client_locations cl ON cl.client_id = c.client_id \n"
			+ "where t.team_id= :teamId  group by et.emp_id")
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
	EmployeeTeamMap findByEmpIdAndTeamIdAndActiveStatus(@Param("empId") Long empId, @Param("teamId") Long teamId);

	
	@Query("SELECT etm from EmployeeTeamMap etm WHERE etm.empId = :empId AND etm.teamId = :teamId")
	EmployeeTeamMap findByEmpIdAndTeamId(Long empId, Long teamId);
		
//	@Query(nativeQuery = true)
//	List<EmployeeTeamMap> findByTeamIdAndActive(Long teamId);
	
	// @Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.teamId = :teamId AND etm.active IN (1, 2)")
	// List<EmployeeTeamMap> findByTeamIdAndActive(Long teamId);
	@Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.teamId = :teamId AND etm.active IN (1, 2)")
	List<EmployeeTeamMap> findByTeamIdAndActive(@Param("teamId") Long teamId);

	
	@Query(nativeQuery = true)
	List<Long> findByActiveAndTeamIdIn(List<Long> teamIds);

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
	 		+ "and etm.active = :active")
		List<Project> findByProjectIdAndActiveForDraftProject(Integer projectId, Long active);

//	 @Query(nativeQuery = true)
//	List<EmployeeTeamMap> findTeammembersByTeamIdAndStatus(Long teamId);
	 
	 @Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.teamId = :teamId AND etm.active = 1")
	 List<EmployeeTeamMap> findTeammembersByTeamIdAndStatus(Long teamId);
	 
	 @Query(nativeQuery = true)
	 List<EmployeeTeamMap> findByTeamIdAndIsActive(Long empId,Long active,Integer projectId);
	 

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
	
	// @Query(value = "SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO(\n"
	//         + "e.empId,e.employeementId,e.billable,e.billableType,e.name,d.name \n" 
	//         + ",p.projectId,p.projectName,p.poProjectId,p.startDate,p.endDate,p.apmosysRM,p.clientRM,p.poProjectType,p.poNo \n"
	//         + ",c.clientName,t.teamId,t.teamName,t.isActive \n"
	//         + ",etm.employeeRole,etm.active,pm.empId,pm.name,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) \n"
	// 		+ "FROM EmployeeTeamMap etm\n"
	// 		+ "RIGHT JOIN Employee e ON e.empId = etm.empId \n"
	// 		+ "RIGHT JOIN Team t ON t.teamId = etm.teamId \n"
	// 		+ "INNER JOIN Project p ON p.projectId = t.projectId \n"
	// 		+ "INNER JOIN Client c ON c.clientId = p.clientId \n"
	// 		+ "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId \n"
	// 		+ "INNER JOIN Department d ON d.deptId = jr.deptId \n"
	// 		+ "LEFT JOIN  ProjectManagerMapping pmm ON pmm.projectId = p.projectId \n"
	// 		+ "LEFT JOIN  Employee pm ON pm.empId = pmm.projectManagerId\n"
	// 		+ "WHERE p.projectId IN :projectIds \n"
	// 		+ "AND etm.active != 0 \n"
	// 		+ "AND t.isActive = 'Y' \n"
	// 		+ "AND e.employmentstatus != 'InActive' \n"
	// 		+ "AND pmm.active = 1 AND e.empId NOT BETWEEN 1 AND 6 ")
	// List<RMGFlatEmployeeProjectTeamDTO>  findEmployeeProjectTeamDetailsByProjectIds(@Param("projectIds") Set<Integer> projectIds);
	
	
	
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



// @Query(value = "SELECT \n"
// 		+ "    e.emp_id, \n"
// 		+ "    e.employeement_id, \n"
// 		+ "    e.billable, \n"
// 		+ "    e.billable_type, \n"
// 		+ "    e.name, \n"
// 		+ "    d.name AS departmentname,\n"
// 		+ "    p.project_id, \n"
// 		+ "    p.project_name, \n"
// 		+ "    p.po_project_id, \n"
// 		+ "    p.start_date, \n"
// 		+ "    p.end_date,\n"
// 		+ "    p.apmosysrm, \n"
// 		+ "    p.clientrm, \n"
// 		+ "    p.po_project_type, \n"
// 		+ "    p.po_no, \n"
// 		+ "    c.client_name,\n"
// 		+ "    t.team_id, \n"
// 		+ "    t.team_name, \n"
// 		+ "    t.is_active, \n"
// 		+ "    etm.employee_role, \n"
// 		+ "    etm.active,  \n"
// 		+ "    pm.emp_id AS project_manager_id, \n"
// 		+ "    pm.name AS project_manager_name,e.is_apmosys_product\n"
// 		+ "FROM \n"
// 		+ "    employee_team_mapping etm\n"
// 		+ "RIGHT JOIN \n"
// 		+ "    employee e ON e.emp_id = etm.emp_id\n"
// 		+ "RIGHT JOIN \n"
// 		+ "    teams t ON t.team_id = etm.team_id\n"
// 		+ "INNER JOIN \n"
// 		+ "    projects p ON p.project_id = t.project_id\n"
// 		+ "LEFT JOIN \n"
// 		+ "    clients c ON c.client_id = p.client_id\n"
// 		+ "INNER JOIN \n"
// 		+ "    job_role jr ON jr.job_role_id = e.job_role_id\n"
// 		+ "INNER JOIN \n"
// 		+ "    department d ON d.dept_id = jr.dept_id\n"
// 		+ "LEFT JOIN \n"
// 		+ "    project_manager_mapping pmm ON pmm.project_id = p.project_id AND pmm.active = 1 \n"
// 		+ "LEFT JOIN \n"
// 		+ "    employee pm ON pm.emp_id = pmm.project_manager_id\n"
// 		+ "LEFT JOIN (\n"
// 		+ "			select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
// 		+ "				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
// 		+ "			from employee e \n"
// 		+ "			left join employee_leave el \n"
// 		+ "				on el.emp_id = e.emp_id \n"
// 		+ "				and leave_status_id in (1,2) \n"
// 		+ "				and manager_approval_status = 'Approved' \n"
// 		+ "				and leave_type_master_id = 5 \n"
// 		+ "				and curdate() between date(el.from_date) and date(el.to_date) \n"
// 		+ "		) eld on eld.emp_id = e.emp_id \n"
// 		+ "WHERE \n"
// 		+ "    p.project_id IN :projectIds\n"
// 		+ "    AND etm.active != 0 \n"
// 		+ "    AND t.is_active = 'Y' \n"
// 		+ "    AND e.employmentstatus != 'InActive' \n"
// 		+ "    AND d.dept_id IN :deptIds AND e.emp_id NOT BETWEEN 1 AND 6 \n"
// 		+ "AND ((:hideMaternityLeaveEmps = true) \n"
// 		+ "			or \n"
// 		+ "		(:hideMaternityLeaveEmps != true and eld.On_Maternity_Leave = 'No')\n"
// 		+ "	)",
// nativeQuery = true)
// List<Object[]> findEmployeeProjectTeamDetailsByProjectIdsAndDepartment(@Param("projectIds") Set<Integer> projectIds,@Param("deptIds")List<Long> deptIds,@Param("hideMaternityLeaveEmps")Boolean hideMaternityLeaveEmps); 







// @Query("SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO(" +
// 	       "e.empId, e.employeementId, e.billable, e.billableType, e.name, d.name, " +
// 	       "p.projectId, p.projectName, p.poProjectId, p.startDate, p.endDate, " +
// 	       "p.apmosysRM, p.clientRM, p.poProjectType, p.poNo, c.clientName, " +
// 	       "t.teamId, t.teamName, t.isActive, etm.employeeRole, etm.active, " +
// 	       "pm.empId, pm.name,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) " +
	       
// 	       "FROM EmployeeTeamMap etm " +
// 	       "RIGHT JOIN Employee e ON e.empId = etm.empId " +
// 	       "RIGHT JOIN Team t ON t.teamId = etm.teamId " +
// 	       "INNER JOIN Project p ON p.projectId = t.projectId " +
// 	       "INNER JOIN Client c ON c.clientId = p.clientId " +
// 	       "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId " +
// 	       "INNER JOIN Department d ON d.deptId = jr.deptId " +
// 	       "LEFT JOIN ProjectManagerMapping pmm ON pmm.projectId = p.projectId " +
// 	       "LEFT JOIN Employee pm ON pm.empId = pmm.projectManagerId " +
	       
// 	       "WHERE e.empId IN ( " +
// 	           "SELECT e1.empId " +
// 	           "FROM EmployeeTeamMap etm1 " +
// 	           "JOIN Employee e1 ON e1.empId = etm1.empId " +
// 	           "JOIN Team t1 ON t1.teamId = etm1.teamId " +
// 	           "JOIN Project p1 ON p1.projectId = t1.projectId " +
// 	           "JOIN ProjectManagerMapping pmm1 ON pmm1.projectId = p1.projectId " +
// 	           "WHERE etm1.active != 0 AND t1.isActive = 'Y' AND e1.employmentstatus != 'InActive' " +
// 	           "AND pmm1.active = 1 AND p1.projectId IN :projectIds " +
// 	           "GROUP BY e1.empId " +
// 	           "HAVING COUNT(CASE WHEN p1.poProjectId IS NULL THEN 1 END) > 0 " +
// 	           "AND COUNT(CASE WHEN p1.poProjectId IS NOT NULL THEN 1 END) > 0" +
// 	       ") " +
	       
// 	       "AND etm.active != 0 " +
// 	       "AND t.isActive = 'Y' " +
// 	       "AND e.employmentstatus != 'InActive' " +
// 	       "AND pmm.active = 1 " +
// 	       "AND e.empId NOT BETWEEN 1 AND 6")
// 	List<RMGFlatEmployeeProjectTeamDTO> findEmployeeProjectTeamDetailsMatchedBothProjects(@Param("projectIds") Set<Integer> projectIds);





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
	 
	 
	 
	 
	 @Query(nativeQuery = true,value ="select distinct p.project_id,p.project_name,p.apmosysrm, p.clientrm,p.start_date,\n"
	 		+ "p.end_date,p.po_no,p.po_project_type,c.client_name client_name,pm.emp_id project_manager_id,pm.name project_manager,t.team_id,t.team_name,e.emp_id,e.name,jr.name job_role,d.name department,\n"
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

	//  @Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.RMGProjectToEmployeeFlatDTO(" +
	// 	       "p.projectId, p.projectName, p.apmosysRM, p.clientRM, " +
	// 	       "p.startDate, p.endDate, p.poNo, p.poProjectType, " +
	// 	       "c.clientName, " +                         
	// 	       "pm.empId, pm.name, " +
	// 	       "t.teamId, t.teamName, " +
	// 	       "e.empId, e.name, " +
	// 	       "jr.name, d.name, e.mobileNo, e.email, " +
	// 	       "e.billable, e.billableType, " +
	// 	       "etm.startDate, " +
	// 	       "e.employeementId" +
	// 	       ") " +
	// 	       "FROM Project p " +
	// 	       "INNER JOIN Team t ON t.projectId = p.projectId " +
	// 	       "INNER JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId " +
	// 	       "LEFT JOIN ProjectManagerMapping pmm ON p.projectId = pmm.projectId " +
	// 	       "INNER JOIN Employee pm ON pm.empId = pmm.projectManagerId " +
	// 	       "INNER JOIN Employee e ON e.empId = etm.empId " +
	// 	       "INNER JOIN Client c ON c.clientId = p.clientId " +
	// 	       "INNER JOIN JobRole jr ON jr.jobRoleId = e.jobRoleId " +
	// 	       "INNER JOIN Department d ON jr.deptId = d.deptId " +
	// 	       "WHERE p.projectId NOT IN (" +
	// 	       "  SELECT p2.projectId FROM Timesheet et " +
	// 	       "  INNER JOIN TimesheetActivityMap etam ON et.timesheetId = etam.timesheetId " +
	// 	       "  INNER JOIN Activity a ON a.activityId = etam.activityId " +
	// 	       "  RIGHT JOIN Team t2 ON t2.teamId = a.teamId " +
	// 	       "  INNER JOIN Project p2 ON p2.projectId = t2.projectId " +
	// 	       "  WHERE et.date >= :fromDate AND et.date <= :toDate" +
	// 	       ") " +
	// 	       "AND p.active = 'true' " +
	// 	       "AND p.projectId IN :projectIds " +
	// 	       "AND t.isActive = 'Y' " +
	// 	       "AND etm.active != 0 " +
	// 	       "AND e.employmentstatus != 'InActive'")
	// 	List<RMGProjectToEmployeeFlatDTO> findNonComplianceProjects(
	// 	    @Param("projectIds") Set<Integer> projectIds,
	// 	    @Param("fromDate") LocalDate fromDate,
	// 	    @Param("toDate") LocalDate toDate
	// 	);
	 
		// @Query(value = "SELECT new com.apmosys.employeeportal.response.TeamTimesheetDetailsResponse(ete.empId, ete.employeementId, " +
		// 		"ete.name, te.deptIds, tm.employeeRole, te.teamName, te.teamId, " +
		// 		"etl.name, etm.name, " +
		// 		"p.projectId, p.projectName, etpm.name, tm.active) " +
		// 		"FROM EmployeeTeamMap tm " +
		// 		"LEFT JOIN Team te ON tm.teamId = te.teamId " +
		// 		"LEFT JOIN Employee etl ON te.teamLeadId = etl.empId " +
		// 		"LEFT JOIN Employee ete ON tm.empId = ete.empId " +
		// 		"LEFT JOIN JobRole jr ON ete.jobRoleId = jr.jobRoleId " +
		// 		"LEFT JOIN Employee etm ON ete.managerId = etm.empId " +
		// 		"LEFT JOIN Project p ON te.projectId = p.projectId " +
		// 		"LEFT JOIN ProjectPoDetails ppd on ppd.poProjectId = p.poProjectId and ppd.poId = tm.poId and ppd.active = 1 "+
		// 		"left join ProjectManagerMapping pmm on pmm.projectId =p.projectId and pmm.active = 1 " +
		// 		"LEFT JOIN Employee etpm ON etpm.empId = pmm.projectManagerId " +
		// 		"WHERE ppd.poId =:id ")
		// List<TeamTimesheetDetailsResponse> getTeamAndTimeSheetDetails(Long id);

		@Query(value = "SELECT new com.apmosys.employeeportal.response.TeamTimesheetDetailsResponse(ete.empId, ete.employeementId, " +
				"ete.name, te.deptIds, tm.employeeRole, te.teamName, te.teamId, " +
				"etl.name, etm.name, " +
				"p.projectId, p.projectName, etpm.name, "
				+ "tm.startDate,tm.endDate) " +
				"FROM EmployeeTeamMap tm " +
				"LEFT JOIN Team te ON tm.teamId = te.teamId " +
				"LEFT JOIN Employee etl ON te.teamLeadId = etl.empId " +
				"LEFT JOIN Employee ete ON tm.empId = ete.empId " +
				"LEFT JOIN Employee etm ON ete.managerId = etm.empId " +
				"LEFT JOIN Project p ON te.projectId = p.projectId " +
				"left join ProjectManagerMapping pmm on pmm.projectId =p.projectId and pmm.active = 1 " +
				"LEFT JOIN Employee etpm ON etpm.empId = pmm.projectManagerId " +
				"WHERE ete.employeementId =:empId and (tm.startDate <=:endDate AND (tm.endDate is null or tm.endDate >=:startDate)) "+
				" and te.isActive ='Y'  ")
		List<TeamTimesheetDetailsResponse> getProjectDetailsByEmpIdAndDateRange(Long empId, LocalDateTime startDate,
				LocalDateTime endDate);


	@Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.teamId in :teamIds and etm.active != 0")
	 List<EmployeeTeamMap> activeAndPendingEmployeesByTeamIds(List<Long> teamIds);
	 
	 @Query("SELECT DISTINCT etm.empId FROM EmployeeTeamMap etm " +
		       "JOIN Team t ON etm.teamId = t.teamId " +
		       "WHERE etm.teamId IN :teamIds AND etm.active != 0 AND t.isActive = 'Y'")
	List<Long> findActiveEmpIdsByTeamIds(@Param("teamIds") List<Long> teamIds);
	 
	 
	 
	 
	 
	 @Query(value="SELECT ppo.po_id ,JSON_ARRAYAGG(e.name) "
	 			+ "	AS employee_names FROM  projects p "
	 			+ "	INNER JOIN teams t ON p.project_id = t.project_id "
	 			+ "	INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id "
	 			+ "	INNER JOIN employee e ON e.emp_id = etm.emp_id \n"
	 			+ " INNER JOIN project_po_details ppo on ppo.project_id = p.project_id "
	 			+ " INNER JOIN po_details pd on pd.po_id = ppo.po_id "
	 			+ "	WHERE p.active = 'true' AND t.is_active != 'N' "
	 			+ "	   AND etm.active != 0 AND ppo.active=1 "
	 			+ "    AND CURDATE() BETWEEN pd.po_start_date AND pd.po_end_date "
	 			+ "	GROUP BY ppo.po_id ",nativeQuery = true)
				List<Object> getAllApprovedPoWithTimesheet(); 
	 
	 
	 
	 
	 
	 
	 
	 @Query(
			  value = "SELECT ete.name AS employee_name, " +
			          "       tm.start_date AS onboarding_date, " +
			          "       ete.billable_type, " +
			          "       ete.billable, "+
			          "       d.name AS department_name, " +
			          "       p.po_project_id "     +
			          "FROM employee_team_mapping tm " +
			          "INNER JOIN teams te ON tm.team_id = te.team_id " +
			          "INNER JOIN employee ete ON tm.emp_id = ete.emp_id " +
			          "INNER JOIN job_role jr ON ete.job_role_id = jr.job_role_id " +
			          "INNER JOIN department d ON jr.dept_id = d.dept_id " +
			          "INNER JOIN projects p ON te.project_id = p.project_id " +
			          "WHERE p.po_project_id IN (:projectIds) " + 
			          "  AND te.is_active != 'N' " +
			          "  AND tm.active != 0 " +
			          "  AND p.active = 'true'",
			  nativeQuery = true
			)
			List<Object[]> getActiveTeamAndTimeSheetWithForRm(@Param("projectIds") List<Long> projectIds);
			
		@Modifying
	    @Transactional
	    @Query("UPDATE EmployeeTeamMap etm " +
	           "SET etm.active = 2 " +
	           "WHERE etm.active = 1 " +
	           "AND etm.teamId IN (" +
	           "   SELECT t.teamId FROM Team t WHERE t.projectId = :projectId" +
	           ")")
	    int updateActiveFrom1To2ByProjectId(@Param("projectId") Integer projectId);
		
		@Query("select distinct e.employeementId from EmployeeTeamMap etm \n"
				+ " inner join Employee e on etm.empId = e.empId \n"
				+ " where etm.active != 0 and etm.resourceOverviewId in :resourceOverviewId")
		List<Long> checkActiveAndPendingEmployeeMappingWithResourceOverViewId(@Param("resourceOverviewId") List<Long> resourceOverviewId);
		
		public List<EmployeeTeamMap> findByTeamIdAndActiveNot(Long teamId, Integer active);

		public boolean existsByTeamIdAndActiveNot(Long teamId, Integer active);
		
		@Query("SELECT etm FROM EmployeeTeamMap etm " +
		       "INNER JOIN Employee e ON etm.empId = e.empId " +
		       "WHERE etm.teamId IN :teamIds " +
		       "AND e.employmentstatus != 'InActive' " +
		       "AND etm.updatedOn = (" +
		       "   SELECT MAX(etm2.updatedOn) " +
		       "   FROM EmployeeTeamMap etm2 " +
		       "   WHERE etm2.teamId = etm.teamId" +
		       ")")
		List<EmployeeTeamMap> findLatestByTeamIds(@Param("teamIds") List<Long> teamIds);

	@Query(nativeQuery = true)
	List<Object[]> getEmployeePersonaForProject(Long employeeId,Long projectId);
	
	List<EmployeeTeamMap> findByEmpIdAndActive(Long empId, Long active);
	
	

	@Query(value="SELECT etm \n"
			+ "FROM EmployeeTeamMap etm \n"
			+ "INNER JOIN Team tms ON tms.teamId = etm.teamId \n"
			+ "WHERE tms.projectId=:projectId AND etm.empId=:employeeId AND etm.active=1")
	List<EmployeeTeamMap> getAllTeamMembersForProject(Long employeeId,Integer projectId);
	
		@Query(value = "SELECT distinct new com.apmosys.employeeportal.dto.GetClientDetailsByProjectIdAndEmpIdDTO( c.clientId, "
				+ "c.clientName, cl.clientLocationId, cl.clientLocation, t.projectId, p.projectName, t.teamName, t.teamId )\n"
				+ "FROM Team t \n"
				+ "INNER JOIN Project p ON p.projectId = t.projectId \n"
				+ "INNER JOIN Client c ON c.clientId = p.clientId \n"
				+ "INNER JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId \n"
				+ "INNER JOIN ClientLocation cl ON cl.clientId = c.clientId\n"
				+ "where p.projectId = :project_id AND etm.empId = :empId")
		public List<GetClientDetailsByProjectIdAndEmpIdDTO> getClientDetailsByProjectIdAndEmpId(@Param("project_id")Integer projectId, 
				@Param("empId")Long empId);
		
		@Query(value="SELECT etm \n"
				+ "FROM EmployeeTeamMap etm \n"
				+ "WHERE etm.empId=:empId AND etm.active!=0")
		List<EmployeeTeamMap> findByEmpIdAndActiveStatus(Long empId);
		 
		
		@Query(
			    "select count(e) > 0 " +
			    "from EmployeeTeamMap e " +
			    "where e.empId = :empId " +
			    "and e.teamId = :teamId " +
			    "and e.active in (1,0) " +
			    "and e.startDate <= :endDate " +
			    "and (e.endDate is null or e.endDate >= :startDate)"
			)
			boolean existsEmployeeTeamMappingForDate(
			        @Param("empId") Long empId,
			        @Param("teamId") Long teamId,
			        @Param("startDate") LocalDateTime startDate,
			        @Param("endDate") LocalDateTime endDate);

		

		@Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.empId IN :empIds AND etm.teamId = :teamId AND etm.active != 0")
	List<EmployeeTeamMap> findByEmpIdInAndTeamIdAndActiveStatus(List<Long> empIds, @Param("teamId") Long teamId);


		// --------------------------------------------------------------- Below this are the queries that are joined with project po details tabnle
		
		@Query(value = "SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO( \n"
		+ "e.empId,e.employeementId,e.billable,e.billableType,e.name,d.name \n" 
		+ ",p.projectId,p.projectName,p.poProjectId,ppd.poStartDate,ppd.poEndDate,ppd.apmosysRM,ppd.clientRm,p.poProjectType,ppd.poNo \n"
		+ ",c.clientName,t.teamId,t.teamName,t.isActive \n"
		+ ",etm.employeeRole,etm.active,pm.empId,pm.name,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) \n"
		+ "FROM EmployeeTeamMap etm\n"
		+ "RIGHT JOIN Employee e ON e.empId = etm.empId \n"
		+ "RIGHT JOIN Team t ON t.teamId = etm.teamId \n"
		+ "INNER JOIN Project p ON p.projectId = t.projectId \n"
		+ "LEFT JOIN ProjectPoDetails ppd ON ppd.projectId = p.projectId AND ppd.poStartDate <= CURRENT_TIMESTAMP \n"
		+ "AND (ppd.poEndDate IS NULL OR ppd.poEndDate >= CURRENT_TIMESTAMP ) \n"  
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

		@Query(value = "SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO( \n"
		+ "e.empId,e.employeementId,e.billable,e.billableType,e.name,d.name \n" 
		+ ",p.projectId,p.projectName,p.poProjectId, p.startDate, p.endDate, p.apmosysRM, p.clientRM,p.poProjectType, p.poNo \n"
		+ ",c.clientName,t.teamId,t.teamName,t.isActive \n"
		+ ",etm.employeeRole,etm.active,pm.empId,pm.name,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) \n"
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
		List<RMGFlatEmployeeProjectTeamDTO>  findEmployeeProjectTeamDetailsByInternalProjectIds(@Param("projectIds") Set<Integer> projectIds);


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
		+ "    GROUP_CONCAT(DISTINCT DATE(ppd.po_start_date) ORDER BY p.start_date SEPARATOR ', '), \n"
		+ "    GROUP_CONCAT(DISTINCT DATE(ppd.po_end_date) ORDER BY p.start_date SEPARATOR ', '),\n"
		+ "    GROUP_CONCAT(DISTINCT ppd.apmosys_rm ORDER BY p.start_date SEPARATOR ', ') AS apmosysrm,\n" 
		+ "    GROUP_CONCAT(DISTINCT ppd.client_rm ORDER BY p.start_date SEPARATOR ', ') AS clientrm,\n" 
		+ "    p.po_project_type, \n"
		+ "    GROUP_CONCAT(DISTINCT ppd.po_no ORDER BY p.start_date   SEPARATOR ', ') AS po_no,\n"
		+ "    c.client_name,\n"
		+ "    t.team_id, \n"
		+ "    t.team_name, \n"
		+ "    t.is_active, \n"
		+ "    etm.employee_role, \n"
		+ "    etm.active,  \n"
		+ "    pm.emp_id AS project_manager_id, \n"
		+ "    pm.name AS project_manager_name,e.is_apmosys_product\n"
		+ "FROM \n"
		+ "    employee_team_mapping etm\n"
		+ "RIGHT JOIN \n"
		+ "    employee e ON e.emp_id = etm.emp_id\n"
		+ "RIGHT JOIN \n"
		+ "    teams t ON t.team_id = etm.team_id\n"
		+ "INNER JOIN \n"
		+ "    projects p ON p.project_id = t.project_id\n"
		+ "LEFT JOIN project_po_details ppd \n"
		+ "ON ppd.project_id = p.project_id \n"
		+ "AND ppd.po_start_date <= CURRENT_TIMESTAMP \n" 
		+ "AND (ppd.po_end_date IS NULL OR ppd.po_end_date >= CURRENT_TIMESTAMP ) \n" 
		+ "LEFT JOIN \n"
		+ "    clients c ON c.client_id = p.client_id\n"
		+ "INNER JOIN \n"
		+ "    job_role jr ON jr.job_role_id = e.job_role_id\n"
		+ "INNER JOIN \n"
		+ "    department d ON d.dept_id = jr.dept_id\n"
		+ "LEFT JOIN \n"
		+ "    project_manager_mapping pmm ON pmm.project_id = p.project_id AND pmm.active = 1 \n"
		+ "LEFT JOIN \n"
		+ "    employee pm ON pm.emp_id = pmm.project_manager_id\n"
		+ "LEFT JOIN (\n"
		+ "			select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
		+ "				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
		+ "			from employee e \n"
		+ "			left join employee_leave el \n"
		+ "				on el.emp_id = e.emp_id \n"
		+ "				and leave_status_id in (1,2) \n"
		+ "				and manager_approval_status = 'Approved' \n"
		+ "				and leave_type_master_id = 5 \n"
		+ "				and curdate() between date(el.from_date) and date(el.to_date) \n"
		+ "		) eld on eld.emp_id = e.emp_id \n"
		+ "WHERE \n"
		+ "    p.project_id IN :projectIds\n"
		+ "    AND etm.active != 0 \n"
		+ "    AND t.is_active = 'Y' \n"
		+ "    AND e.employmentstatus != 'InActive' \n"
		+ "    AND d.dept_id IN :deptIds AND e.emp_id NOT BETWEEN 1 AND 6 \n"
		+ "AND ((:hideMaternityLeaveEmps = true) \n"
		+ "			or \n"
		+ "		(:hideMaternityLeaveEmps != true and eld.On_Maternity_Leave = 'No')\n"
		+ "	)\n"
		+" GROUP BY\n"
		+ "    e.emp_id,\n"
		+ "    e.employeement_id,\n"
		+ "    e.billable,\n"
		+ "    e.billable_type,\n"
		+ "    e.name,\n"
		+ "    d.name,\n"
		+ "    p.project_id,\n"
		+ "    p.project_name,\n"
		+ "    p.po_project_id,\n"
		+ "    p.po_project_type,\n"
		+ "    c.client_name,\n"
		+ "    t.team_id,\n"
		+ "    t.team_name,\n"
		+ "    t.is_active,\n"
		+ "    etm.employee_role,\n"
		+ "    etm.active,\n"
		+ "    pm.emp_id,\n"
		+ "    pm.name,\n"
		+ "    e.is_apmosys_product\n",
nativeQuery = true)
List<Object[]> findEmployeeProjectTeamDetailsByProjectIdsAndDepartment(@Param("projectIds") Set<Integer> projectIds,@Param("deptIds")List<Long> deptIds,@Param("hideMaternityLeaveEmps")Boolean hideMaternityLeaveEmps); 


@Query("SELECT new com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO(" +
	       "e.empId, e.employeementId, e.billable, e.billableType, e.name, d.name, " +
	       "p.projectId, p.projectName, p.poProjectId,ppd.poStartDate,ppd.poEndDate, " +
	       "ppd.apmosysRM, ppd.clientRm, p.poProjectType, ppd.poNo, c.clientName, " +
	       "t.teamId, t.teamName, t.isActive, etm.employeeRole, etm.active, " +
	       "pm.empId, pm.name,e.isConsultant,e.isApprenticeship,e.isApmosysProduct) " +
	       
	       "FROM EmployeeTeamMap etm " +
	       "RIGHT JOIN Employee e ON e.empId = etm.empId " +
	       "RIGHT JOIN Team t ON t.teamId = etm.teamId " +
	       "INNER JOIN Project p ON p.projectId = t.projectId " +
		   "LEFT JOIN ProjectPoDetails ppd ON ppd.projectId = p.projectId AND ppd.poStartDate <= CURRENT_TIMESTAMP \n" +
		   "AND (ppd.poEndDate IS NULL OR ppd.poEndDate >= CURRENT_TIMESTAMP ) \n" +
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

	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.RMGProjectToEmployeeFlatDTO(" +
			"p.projectId, p.projectName, p.apmosysRM, p.clientRM, " +
			"ppd.poStartDate,ppd.poEndDate, p.poNo, p.poProjectType, " +
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
			"LEFT JOIN ProjectPoDetails ppd ON ppd.projectId = p.projectId AND ppd.poStartDate <= CURRENT_TIMESTAMP \n" +
			"AND (ppd.poEndDate IS NULL OR ppd.poEndDate >= CURRENT_TIMESTAMP ) \n"  +
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
			@Param("toDate") LocalDate toDate);
	
	@Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.EmpMappingDTO"
			+ "(etm.empId,etm.startDate,etm.endDate,etm.isShadow,etm.poId) " +
			"FROM EmployeeTeamMap etm " +
			"LEFT JOIN PoRequirementMapping prm "+
			"on prm.roleId = etm.roleId and prm.poId = etm.poId "+	
			"WHERE etm.active!=2  AND prm.poId = :poId ")
	List<EmpMappingDTO> getActiveEmpDetails(Long poId);
	
	// @Query(value = "Select new com.apmosys.employeeportal.dto.PoDetailsDto(prm.poRequirementMappingId \n"
	// 		+ ",COUNT(DISTINCT CASE WHEN etm.active  = 1 THEN etm.empId END)  \n"
	// 		+ ",COUNT(DISTINCT CASE WHEN etm.active  = 2 THEN etm.empId END) \n"
	// 		+ ") \n"
	// 		+ "FROM Project p \n"
	// 		+ "INNER JOIN ProjectPoDetails ppd ON p.projectId = ppd.projectId AND DATE(ppd.poStartDate) <= CURRENT_DATE AND DATE(ppd.poEndDate) >= CURRENT_DATE \n"
	// 		+ "INNER JOIN PoRequirementMapping prm ON ppd.poId = prm.poId \n"
	// 		+ "INNER JOIN Team t ON t.projectId = p.projectId \n"
	// 		+ "INNER JOIN EmployeeTeamMap etm ON t.teamId = etm.teamId AND etm.active IN (1, 2) \n"
	// 		+ "where p.projectId =:projectId \n"
	// 		+ "GROUP BY prm.poRequirementMappingId")
	// List<PoDetailsDto> getAssigedAndApprovedEmployeeCountByProjectId(Integer projectId);

	@Query(value = "Select new com.apmosys.employeeportal.dto.PoDetailsDto(prm.poRequirementMappingId \n"
			+ ",COUNT(DISTINCT CASE WHEN etm.active  = 1 THEN etm.empId END)  \n"
			+ ",COUNT(DISTINCT CASE WHEN etm.active  = 2 THEN etm.empId END) \n"
			+ ") \n"
			+ "FROM EmployeeTeamMap etm \n"
			+ "INNER JOIN RoleDetails rd on rd.roleId = etm.roleId \n"
			+ "INNER JOIN PoRequirementMapping prm ON etm.poId = prm.poId and etm.roleId = prm.roleId  \n"
			+ "INNER JOIN ProjectPoDetails ppd ON prm.poId = ppd.poId AND (DATE(ppd.poStartDate) <= CURRENT_DATE OR :currentActivePO = false) AND DATE(ppd.poEndDate) >= CURRENT_DATE \n"
			+ "where ppd.projectId =:projectId \n"
			+ "GROUP BY prm.poRequirementMappingId")
	List<PoDetailsDto> getAssigedAndApprovedEmployeeCountByProjectId(Integer projectId, boolean currentActivePO);

	@Query("SELECT etm FROM EmployeeTeamMap etm WHERE etm.teamId in :teamIds and etm.empId in :empIds and etm.active != 0 ")
	 List<EmployeeTeamMap> activeAndPendingEmployeesByTeamIdsAndEmpIds(List<Long> teamIds,List<Long> empIds);
	  
	
	@Query(value ="SELECT new com.apmosys.employeeportal.dto.EmployeeImpactDTO(e.name, t.teamName)\n"
			+ "FROM EmployeeTeamMap etm\n"
			+ "LEFT JOIN Employee e ON etm.empId = e.empId\n"
			+ "LEFT JOIN Team t ON etm.teamId = t.teamId\n"
			+ "WHERE etm.poId = :poId\n"
			+ "AND etm.roleId = :roleId\n"
			+ "AND etm.active != 0")
	List<EmployeeImpactDTO> findActiveEmployeesByPoAndRole(Long poId, Long roleId);
	
	
	@Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END\n"
			+ "FROM EmployeeTeamMap e\n"
			+ "WHERE e.poId = :poId\n"
			+ "AND e.active != 0")
	boolean existsActiveTeams(Long poId);
	
	@Query(value = "SELECT MIN(e.startDate)\n"
			+ "FROM EmployeeTeamMap e\n"
			+ "WHERE e.teamId IN :teamIds")
	LocalDateTime findMinEmployeeStartDateByTeamIds(
	        @Param("teamIds") List<Long> teamIds
	);
	
	@Query("SELECT DISTINCT etm.empId\n"
			+ " FROM EmployeeTeamMap etm\n"
			+ " INNER JOIN Team t ON t.teamId = etm.teamId\n"
			+ " INNER JOIN Project p ON p.projectId = t.projectId\n"
			+ " WHERE p.projectId = :projectId\n"
			+ "	AND etm.active != 0 \n"
			+ "	AND t.isActive = 'Y' \n"
			+ "	AND p.active = 'true' ")
	List<Long> findDistinctEmpIdsByProjectId(@Param("projectId") Long projectId);

	
	@Query(value = "Select etm from EmployeeTeamMap etm where etm.poId =:previousPoId and etm.roleId =:roleId and etm.active !=0")
	List<EmployeeTeamMap>findActiveEmployeesForRole(Long previousPoId, Long roleId);


	@Query(value = "SELECT new com.apmosys.employeeportal.response.TeamTimesheetDetailsResponse(ete.empId, ete.employeementId, " +
	"ete.name, te.deptIds, prm.role, te.teamName, te.teamId, " +
	"etl.name, etm.name, " +
	"p.projectId, p.projectName, etpm.name, tm.active,ppd.poId) " +
	"FROM EmployeeTeamMap tm " +
	"LEFT JOIN Team te ON tm.teamId = te.teamId " + 
	"LEFT JOIN PoRequirementMapping prm on prm.roleId = tm.roleId and prm.poId = tm.poId " +
	"LEFT JOIN Employee etl ON te.teamLeadId = etl.empId " +
	"LEFT JOIN Employee ete ON tm.empId = ete.empId " +
	"LEFT JOIN JobRole jr ON ete.jobRoleId = jr.jobRoleId " +
	"LEFT JOIN Employee etm ON ete.managerId = etm.empId " +
	"LEFT JOIN Project p ON te.projectId = p.projectId " +
	"LEFT JOIN ProjectPoDetails ppd on ppd.poProjectId = p.poProjectId and ppd.poId = tm.poId and ppd.active = 1 "+
	"left join ProjectManagerMapping pmm on pmm.projectId =p.projectId and pmm.active = 1 " +
	"LEFT JOIN Employee etpm ON etpm.empId = pmm.projectManagerId " +
	"WHERE ppd.poId =:poId And ppd.poProjectId = :poProjectId and ( tm.startDate <= :endDate And  (tm.endDate Is Null OR  tm.endDate >= :startDate) )")
List<TeamTimesheetDetailsResponse> getTeamAndTimeSheetDetails(Long poId,Long poProjectId,LocalDateTime startDate,LocalDateTime endDate);

@Query(value = "SELECT new com.apmosys.employeeportal.response.TeamTimesheetDetailsResponse(ete.empId, ete.employeementId, " +
"ete.name, te.deptIds, prm.role, te.teamName, te.teamId, " +
"etl.name, etm.name, " +
"p.projectId, p.projectName, etpm.name, tm.active,ppd.poId) " +
"FROM EmployeeTeamMap tm " +
"LEFT JOIN Team te ON tm.teamId = te.teamId " + 
"LEFT JOIN PoRequirementMapping prm on prm.roleId = tm.roleId and prm.poId = tm.poId " +
"LEFT JOIN Employee etl ON te.teamLeadId = etl.empId " +
"LEFT JOIN Employee ete ON tm.empId = ete.empId " +
"LEFT JOIN JobRole jr ON ete.jobRoleId = jr.jobRoleId " +
"LEFT JOIN Employee etm ON ete.managerId = etm.empId " +
"LEFT JOIN Project p ON te.projectId = p.projectId " +
"LEFT JOIN ProjectPoDetails ppd on ppd.poProjectId = p.poProjectId and ppd.poId = tm.poId and ppd.active = 1 "+
"left join ProjectManagerMapping pmm on pmm.projectId =p.projectId and pmm.active = 1 " +
"LEFT JOIN Employee etpm ON etpm.empId = pmm.projectManagerId " +
"WHERE ppd.poId =:poId And p.projectName = :projectName and ( tm.startDate <= :endDate And  (tm.endDate Is Null OR  tm.endDate >= :startDate) )")
List<TeamTimesheetDetailsResponse> getTeamAndTimeSheetDetails2(Long poId,String projectName,LocalDateTime startDate,LocalDateTime endDate);
}
