package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;

public interface EmployeeClientSideIdMappingRepository extends JpaRepository <EmployeeClientSideIdMapping, Long> {

	@Query("SELECT e.clientSideId FROM EmployeeClientSideIdMapping e WHERE e.projectId = :projectId AND e.active = true")
	public Optional<String> findClientSideIdByProjectId(@Param("projectId") Long projectId);
	
	public Optional<EmployeeClientSideIdMapping> findByProjectIdAndActiveAndEmpId(Long projectId, Boolean active, Long empId);
	
	@Query("SELECT e.clientSideId FROM EmployeeClientSideIdMapping e WHERE e.projectId = :projectId AND e.empId = :empId AND e.active = true")
	public Optional<String> getClientSideIdByProjectIdAndEmpId(Long projectId, Long empId);
	
	List<EmployeeClientSideIdMapping> findByEmpIdInAndProjectIdInAndActive(List<Long> empIds, List<Long> projectIds, Boolean active);
	
}
