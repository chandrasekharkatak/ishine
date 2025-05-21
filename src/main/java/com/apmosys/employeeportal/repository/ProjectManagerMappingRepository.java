package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectManagerMapping;


public interface ProjectManagerMappingRepository extends JpaRepository<ProjectManagerMapping,Long>{

	
	@Query(value = "select pmm.project_manager_id,e.name from project_manager_mapping pmm \n"
			+ "inner join employee e on e.emp_id = pmm.project_manager_id where project_id = :projectId and active = 1 ",nativeQuery = true)
	List<Object[]>findProjectManagersPerProject(@Param("projectId") Integer projectId);
	
	public List<ProjectManagerMapping> findByProjectId(Long projectId);
	
	public ProjectManagerMapping findByProjectIdAndProjectManagerId(Long projectId,Long projectManagerId);
	
}
