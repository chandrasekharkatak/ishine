package com.apmosys.employeeportal.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectsTempRepository extends JpaRepository<ProjectsTemp, Integer> {
	public ProjectsTemp findByPoProjectId(Long poProjectId);
	
	@Query(nativeQuery=true,value="select DISTINCT po_project_type from projects_temp where po_project_type IS NOT NULL")
	List<String>finddistinctPoProjectType();
}
