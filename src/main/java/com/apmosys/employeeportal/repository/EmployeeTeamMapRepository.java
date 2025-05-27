package com.apmosys.employeeportal.repository;

import java.util.List;
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
	
	@Query(value = "select e.emp_id, e.employeement_id, e.billable, e.billable_type, e.name, " +
            "p.project_id, p.project_name, p.po_project_id, p.po_start_date, p.po_end_date, " +
            "p.apmosysrm, p.clientrm, p.po_project_type, p.po_no, c.client_name, " +
            "t.team_id, t.team_name, t.is_active, etm.employee_role, etm.active " +
            "from employee_team_mapping etm " +
            "RIGHT JOIN employee e on e.emp_id = etm.emp_id " +
            "RIGHT JOIN teams t on t.team_id = etm.team_id " +
            "inner join projects p on p.project_id= t.project_id " +
            "inner join clients c on c.client_id=p.client_id " +
            "inner join job_role jr on jr.job_role_id= e.job_role_id " +
            "inner join department d on d.dept_id = jr.dept_id " +
            "where p.project_id IN :projectIds and etm.active !=0 and t.is_active ='Y'",
    nativeQuery = true)
List<Object[]> findEmployeeProjectTeamDetailsByProjectIds(@Param("projectIds") Set<Integer> projectIds);


}
