package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Set;

import javax.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Activity;

@Repository
public interface ActivitiesRepository extends JpaRepository<Activity, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getAllActivitiesByProjectIdAndTeamId(Integer projectId, Long teamId);

	public List<Activity> findByTeamId(Long teamId);

	public List<Activity> findByTeamIdAndEmployeeRoleIn(Long teamId, String[] employeeRole);

	@Query(nativeQuery = true)
	public List<Activity> getActivityByTeamIdAndDepts(String departmentId, Long teamId);

	List<Activity> findByDeptIdsAndEmployeeRoleAndTeamId(String deptIds, String employeeRole, Long teamId);

	@Query("SELECT DISTINCT a.employeeRole FROM Activity a WHERE a.teamId = :teamId")
	List<String> findUniqueEmployeeRolesByTeamId(@Param("teamId") Long teamId);

	List<Activity> findByTeamIdIn(List<Long> teamIds);

	boolean existsByActivityIdAndTeamId(Long activityId, Long teamId);

	public List<Activity> findByTeamIdAndEmployeeRole(Long teamId, String employeeRole);

	@Query("SELECT DISTINCT a FROM Activity a WHERE a.teamId IN :teamIds and a.deptIds IN :deptIds and a.employeeRole IN :roles ")
	List<Activity> findByDeptIdsAndEmployeeRoleAndTeamIdIn(Set<String> deptIds, Set<String> roles , Set<Long> teamIds);

	@Modifying
	@Query(value = "DELETE a\n"
			+ "	FROM activities a\n"
			+ "	JOIN teams t \n"
			+ "	    ON t.team_id = a.team_id\n"
			+ "	JOIN employee_team_mapping etm \n"
			+ "	    ON etm.team_id = t.team_id \n"
			+ "	    AND etm.employee_role = a.employee_role\n"
			+ "	WHERE etm.emp_id = :empId\n"
			+ "	AND t.team_id = :teamId\n"
			+ "	AND t.project_id = :projectId\n"
			+ "	AND FIND_IN_SET(etm.emp_team_department_id, a.dept_ids)\n"
			+ "	AND NOT EXISTS (\n"
			+ "	    SELECT 1\n"
			+ "	    FROM employee_timesheet_activities_mapping_new etam\n"
			+ "	    JOIN employee_timesheets_new etn \n"
			+ "	        ON etn.timesheet_id = etam.timesheet_id\n"
			+ "	    WHERE etam.activity_id = a.activity_id\n"
			+ "	    AND etn.emp_id = :empId\n"
			+ "	)", nativeQuery = true)
	public void deleteActivitiesForRejectedEmployee(Long empId, Long teamId, Integer projectId);
}
