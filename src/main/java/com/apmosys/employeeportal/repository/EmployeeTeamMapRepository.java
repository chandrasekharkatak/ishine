package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.EmployeeTeamMap;

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

	@Query(nativeQuery = true)
	List<EmployeeTeamMap> findFirstByEmpIdAndTeamIdAndActive(Long empId, Long teamId);

	List<EmployeeTeamMap> findByTeamId(Long teamId);

	EmployeeTeamMap findByEmpIdAndTeamIdAndActive(Long empId, Long teamId, long l);

	@Query(nativeQuery = true)
	EmployeeTeamMap findByEmpIdAndTeamIdAndActiveStatus(Long empId, Long teamId);
	
	@Query(nativeQuery = true)
	EmployeeTeamMap findByEmpIdAndTeamId(Long empId, Long teamId);
	
	@Query(nativeQuery = true)
	List<EmployeeTeamMap> findByTeamIdAndActive(Long teamId);

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
	
	 @Query(nativeQuery = true)
List<EmployeeTeamMap> findByProjectIdAndActive(Integer projectId,Long active);

	 @Query(nativeQuery = true)
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
			+ "    AND pmm.active = 1",
    nativeQuery = true)
List<Object[]> findEmployeeProjectTeamDetailsByProjectIds(@Param("projectIds") Set<Integer> projectIds);

@Query(value ="SELECT \n"
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
		+ "RIGHT JOIN employee e ON e.emp_id = etm.emp_id\n"
		+ "RIGHT JOIN teams t ON t.team_id = etm.team_id\n"
		+ "INNER JOIN projects p ON p.project_id = t.project_id\n"
		+ "INNER JOIN clients c ON c.client_id = p.client_id\n"
		+ "INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
		+ "INNER JOIN department d ON d.dept_id = jr.dept_id\n"
		+ "LEFT JOIN project_manager_mapping pmm ON pmm.project_id = p.project_id\n"
		+ "LEFT JOIN employee pm ON pm.emp_id = pmm.project_manager_id\n"
		+ "WHERE \n"
		+ "    e.emp_id IN (\n"
		+ "        SELECT e1.emp_id\n"
		+ "        FROM employee_team_mapping etm1\n"
		+ "        JOIN employee e1 ON e1.emp_id = etm1.emp_id\n"
		+ "        JOIN teams t1 ON t1.team_id = etm1.team_id\n"
		+ "        JOIN projects p1 ON p1.project_id = t1.project_id\n"
		+ "        JOIN project_manager_mapping pmm1 ON pmm1.project_id = p1.project_id\n"
		+ "        WHERE etm1.active != 0 AND t1.is_active = 'Y' AND e1.employmentstatus != 'InActive' AND pmm1.active = 1   AND p1.project_id IN :projectIds\n"
		+ "        GROUP BY e1.emp_id\n"
		+ "        HAVING \n"
		+ "            COUNT(CASE WHEN p1.po_project_id IS NULL THEN 1 END) > 0 AND\n"
		+ "            COUNT(CASE WHEN p1.po_project_id IS NOT NULL THEN 1 END) > 0\n"
		+ "    )\n"
		+ "    AND etm.active != 0 \n"
		+ "    AND t.is_active = 'Y' \n"
		+ "    AND e.employmentstatus != 'InActive' \n"
		+ "    AND pmm.active = 1",nativeQuery = true)
List<Object[]> findEmployeeProjectTeamDetailsMatchedBothProjects(@Param("projectIds") Set<Integer> projectIds);

 @Query(nativeQuery = true,value ="select etm.emp_id from employee_team_mapping etm inner join teams t on t.team_id = etm.team_id inner join  projects p on p.project_id = t.project_id where etm.is_shadow = 1 and etm.active !=0 and t.is_active = 'Y' and p.project_id = :projectId and etm.emp_id IN :empIds")
 List<Long> findShadowMembersByEmpIdsAndProjectId(@Param("empIds") List<Long> empIds, @Param("projectId") Integer projectId);

	 @Query(nativeQuery = true)
	 List<Map<String, Object>> getActiveProjectIdAndProjectNameByEmpId(Long empId, Integer currentProjectId);
}
