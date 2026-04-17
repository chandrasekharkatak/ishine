package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.EmployeeSpecializationMap;

public interface EmployeeSpecializationMapRepository extends JpaRepository<EmployeeSpecializationMap, Long> {

	List<EmployeeSpecializationMap> findByEmpId(Long empId);

	EmployeeSpecializationMap findByEmpIdAndSpecializationId(Long empId, Long specializationId);

	@Query(nativeQuery = true)
	List<Object[]> getEmployeeDomainInfo(Long empId);

}
