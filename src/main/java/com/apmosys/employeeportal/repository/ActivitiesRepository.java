package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Set;

import javax.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
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

}
