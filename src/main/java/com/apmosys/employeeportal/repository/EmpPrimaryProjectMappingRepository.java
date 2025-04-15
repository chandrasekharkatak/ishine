package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.EmpPrimaryProjectMapping;

public interface EmpPrimaryProjectMappingRepository extends JpaRepository<EmpPrimaryProjectMapping, Long>{

	EmpPrimaryProjectMapping findByEmpId(Long empId);
}
