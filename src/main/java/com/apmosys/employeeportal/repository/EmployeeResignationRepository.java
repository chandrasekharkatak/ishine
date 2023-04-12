package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.EmployeeResignation;

public interface EmployeeResignationRepository extends JpaRepository<EmployeeResignation, Long> {

	@Query(nativeQuery = true)
	List<Object[]> getEmployeeResignationDetail(Long empId);

	@Query(nativeQuery = true)
	List<Object[]> getAllResignationApplication();

	EmployeeResignation findByEmployeeResignationId(Long employeeResignationId);

	EmployeeResignation findByEmpIdAndResignationStatus(Long empId, String employmentStatus);

}
