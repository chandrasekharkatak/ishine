package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;

public interface EmployeeClientSideIdMappingRepository extends JpaRepository <EmployeeClientSideIdMapping, Long> {

	@Query("SELECT e.clientSideId FROM EmployeeClientSideIdMapping e WHERE e.projectId = :projectId AND e.active = true")
	public Optional<String> findClientSideIdByProjectId(@Param("projectId") Long projectId);
	
	public Optional<EmployeeClientSideIdMapping> findByProjectIdAndActiveAndEmpId(Long projectId, Boolean active, Long empId);
	
	@Query("SELECT e.clientSideId FROM EmployeeClientSideIdMapping e WHERE e.projectId = :projectId AND e.active = true AND e.empId = :empId")
	public Optional<String> getClientSideIdByProjectIdAndEmpId(Long projectId, Long empId);
	
}
