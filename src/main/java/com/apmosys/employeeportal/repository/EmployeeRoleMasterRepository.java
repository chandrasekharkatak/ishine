package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.EmployeeRole;

public interface EmployeeRoleMasterRepository extends JpaRepository<EmployeeRole, Integer> {
	
	public List<EmployeeRole> findByEmployeeRoleAndPermission(String employeeRole,String permission);
	
	public List<EmployeeRole> findBySubFeatureMasterIdAndPermission(Long subFeatureMasterId,String permission);

	@Query(nativeQuery = true)
	public List<Object[]> getAllSubFeatureList();

	@Query(nativeQuery = true)
	public List<Object[]> getMappedSubFeatureByJobRoleId(Long jobRoleId);
	
	@Query(nativeQuery = true)
	public List<EmployeeRole> findBySubFeatureMasterId(Long subFeatureMasterId);

}
