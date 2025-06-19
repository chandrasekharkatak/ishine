package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ProjectManagerMapping;
import com.apmosys.employeeportal.model.ProjectOverheadMapping;


public interface ProjectOverheadMappingRepository extends JpaRepository<ProjectOverheadMapping,Long> {
	
	public List<ProjectOverheadMapping> findByProjectId(Long projectId);
	
	public ProjectOverheadMapping findByProjectIdAndProjectOverheadId(Long projectId,Long projectOverheadId);
	
	@Query(nativeQuery = true)
	public List<Object[]>findProjectOverheadsPerProject(@Param("projectId") Long projectId);
	
	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM project_overhead_mapping pom " +
            "JOIN projects p ON p.project_id = pom.project_id " +
            "WHERE p.active = 'true' AND p.po_project_id IS NULL AND pom.project_overhead_id = :empId", nativeQuery = true)
boolean isUserProjectOverheadOfAnyActiveInternalProject(@Param("empId") Long empId);
	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM ProjectOverheadMapping pom " +
            "JOIN Project p ON p.projectId = pom.projectId " +
            "WHERE p.active = 'true' AND pom.projectOverheadId = :empId")
boolean isUserProjectOverheadOfAnyActiveInternalAndExternalProject(@Param("empId") Long empId);
	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM project_overhead_mapping pom " +
            "JOIN projects p ON p.project_id = pom.project_id " +
            "WHERE p.active = 'true' AND pom.project_overhead_id = :empId", nativeQuery = true)
List<Integer> isUserProjectOverheadOfAnyActiveInternalAndExternalProjectList(@Param("empId") Long empId);
	
}
