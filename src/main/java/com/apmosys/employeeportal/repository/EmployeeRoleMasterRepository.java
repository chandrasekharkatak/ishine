package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.EmployeeRole;

public interface EmployeeRoleMasterRepository extends JpaRepository<EmployeeRole, Integer> {
	
	public List<EmployeeRole> findByEmployeeRoleAndPermission(String employeeRole,String permission);

}
