package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.EmployeeLeavesMap;

public interface EmployeeLeavesMapRepository extends JpaRepository<EmployeeLeavesMap, Short> {
	
	
	public EmployeeLeavesMap findByEmpId(Long empId);

}
