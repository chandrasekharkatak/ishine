package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.JobRole;

@Repository
public interface JobRoleRepository extends JpaRepository<JobRole, Long> {
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllJobRoles();
	
	public Long countByDeptId(Long deptId);

	public JobRole findByjobRoleId(Long jobRoleId);

	public List<JobRole> findByDeptId(Long oldDeptId);
	
	public boolean existsByJobRoleId(Long jobRoleId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getHoDByJobRoleId(Long jobRoleId);

	public List<JobRole> findByEmployeeRole(String role);

	public List<JobRole> findByEmployeeRoleNotIn(List<String> jobRoles);

	public JobRole findByName(String name);

	@Query(nativeQuery = true, value = "SELECT count(*) FROM job_role WHERE name = :name AND dept_id = :deptId")
	public Integer findByNameAndDeptId(@Param("name") String name, @Param("deptId") Long deptId);

	
	@Query("SELECT j.jobRoleId FROM JobRole j WHERE j.deptId = :deptId")
	List<Long> findJobRoleIdsByDeptId(@Param("deptId") Long deptId);

	@Query("SELECT j.deptId FROM JobRole j WHERE j.jobRoleId = :jobRoleId")
	Long findDeptIdByJobRoleId(@Param("jobRoleId") Long jobRoleId);
	
	@Query(nativeQuery = true, value = "select employee_role from job_role where  job_role_id = roleId;")
	public String getEmployeeRole(Long roleId);
	
	
}
