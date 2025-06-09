package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ResourceRequirement;

public interface ResourceRequirementRepository extends JpaRepository<ResourceRequirement, Long> {

	@Query(nativeQuery = true,value ="select  MAX(resource_overview_id) from resource_requirement where project_id = :projectId and department LIKE CONCAT('%', :departmentName, '%')")
	Long findByProjectIdAndDepartmentName(@Param("departmentName")String departmentName,@Param("projectId")Integer projectId);
}
