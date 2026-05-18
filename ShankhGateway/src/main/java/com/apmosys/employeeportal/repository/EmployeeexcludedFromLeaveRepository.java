package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.EmployeeexcludedFromLeave;

public interface EmployeeexcludedFromLeaveRepository extends JpaRepository<EmployeeexcludedFromLeave,Long>{
	
    List<EmployeeexcludedFromLeave> findByEmpIdInAndIsExcludedTrue(List<Long> empIds);
    List<EmployeeexcludedFromLeave> findByEmpIdInAndIsExcludedFalse(List<Long> empIds);
    List<EmployeeexcludedFromLeave> findByEmpIdIn(List<Long> empIds);



}
