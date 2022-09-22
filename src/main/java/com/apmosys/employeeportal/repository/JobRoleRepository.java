package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.JobRole;

@Repository
public interface JobRoleRepository extends JpaRepository<JobRole, Long> {
	
	@Query(nativeQuery = true)
	public List<Object[]> getAllJobRoles();
	
	public Long countByDeptId(Long deptId);

	public JobRole findByjobRoleId(Long jobRoleId);

	public List<JobRole> findByDeptId(Long oldDeptId);

	public List<JobRole> findByEmployeeRole(String role);

	public List<JobRole> findByEmployeeRoleNotIn(List<String> jobRoles);
	
}
