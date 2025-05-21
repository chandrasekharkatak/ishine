package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectManagerMapping;


public interface ProjectManagerMappingRepository extends JpaRepository<ProjectManagerMapping,Long>{

	
	@Query(nativeQuery = true)
	public List<Object[]>findProjectManagersPerProject(@Param("projectId") Long projectId);
	
	public List<ProjectManagerMapping> findByProjectId(Long projectId);
	
	public ProjectManagerMapping findByProjectIdAndProjectManagerId(Long projectId,Long projectManagerId);
	
}
