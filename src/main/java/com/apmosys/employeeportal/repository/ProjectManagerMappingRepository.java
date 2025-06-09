package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectManagerMapping;


public interface ProjectManagerMappingRepository extends JpaRepository<ProjectManagerMapping,Long>{

	
	@Query(nativeQuery = true)
	public List<Object[]>findProjectManagersPerProject(@Param("projectId") Long projectId);
	
	public List<ProjectManagerMapping> findByProjectId(Long projectId);
	
	public ProjectManagerMapping findByProjectIdAndProjectManagerId(Long projectId,Long projectManagerId);
	
	@Query(nativeQuery = true)
	public List<Object[]>findProjectManagersByPoProjectId(Long id);
	
	@Modifying
	@Query(nativeQuery = true)
	public void deactivateByProjectId(@Param("projectId") Long projectId);
	
	@Query(nativeQuery = true, value="SELECT pmm.project_manager_id, e.name FROM project_manager_mapping pmm\n"
			+ "INNER JOIN employee e on pmm.project_manager_id = e.emp_id where pmm.project_id = :projectId and pmm.active=1")
	List<Object[]> getAllProjectManagerListWithName(@Param("projectId") Long projectId);

}
