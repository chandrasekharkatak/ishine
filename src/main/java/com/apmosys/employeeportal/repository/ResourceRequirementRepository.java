package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ResourceRequirement;

public interface ResourceRequirementRepository extends JpaRepository<ResourceRequirement, Long> {

	@Query(nativeQuery = true,value ="select  MAX(resource_overview_id) from resource_requirement where project_id = :projectId and department LIKE CONCAT('%', :departmentName, '%')")
	Long findByProjectIdAndDepartmentName(@Param("departmentName")String departmentName,@Param("projectId")Integer projectId);
	
//	@Query(nativeQuery = true, value="select distinct department from resource_requirement where project_id=:projectId ")
//	List<String> getAllDepartmentsFromProjectId(@Param("projectId") Integer projectId);
	
	@Query(value="select distinct rr.department from ResourceRequirement rr where rr.projectId=:projectId ")
	List<String> getAllDepartmentsFromProjectId(@Param("projectId") Integer projectId);
	
	@Query(nativeQuery = true, value="select role, count,experience, department, resource_overview_id ,project_id from resource_requirement where project_id=:projectId")
	List<Object[]> findByProjectId(@Param("projectId")Integer projectId);
	
	@Query(nativeQuery = true, value="select distinct d.name from projects p\n"
			+ "inner join project_department_map pdm on pdm.project_id = p.project_id\n"
			+ "inner join department d on pdm.dept_id = d.dept_id\n"
			+ "where p.project_id = :projectId ")
	List<String> getAllDepartmentsFromProjectIdInternal(@Param("projectId") Integer projectId);
	
}
