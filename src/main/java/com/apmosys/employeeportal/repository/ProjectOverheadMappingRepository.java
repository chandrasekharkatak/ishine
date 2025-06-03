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
	
}
