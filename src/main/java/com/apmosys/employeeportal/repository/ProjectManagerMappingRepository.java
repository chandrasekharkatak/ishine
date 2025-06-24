package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.ProjectManagersDTO;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectManagerMapping;


public interface ProjectManagerMappingRepository extends JpaRepository<ProjectManagerMapping,Long>{

	
	@Query(nativeQuery = true)
	public List<Object[]>findProjectManagersPerProject(Long projectId);
	
	public List<ProjectManagerMapping> findByProjectId(Long projectId);
	
	public ProjectManagerMapping findByProjectIdAndProjectManagerId(Long projectId,Long projectManagerId);
	
	@Query(nativeQuery = true)
	public List<Object[]>findProjectManagersByPoProjectId(Long id);
	
	
	
	@Modifying
	@Transactional
	@Query(value="UPDATE ProjectManagerMapping p SET p.active = 0 WHERE p.projectId =:projectId")
	public void deactivateByProjectId(@Param("projectId") Long projectId);
	
//	@Query(nativeQuery = true, value="SELECT pmm.project_manager_id, e.name FROM project_manager_mapping pmm\n"
//			+ "INNER JOIN employee e on pmm.project_manager_id = e.emp_id where pmm.project_id = :projectId and pmm.active=1")
//	List<Object[]> getAllProjectManagerListWithName(@Param("projectId") Long projectId);
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.ProjectManagersDTO(pmm.projectManagerId, e.name)FROM ProjectManagerMapping pmm \n"
			+ "INNER JOIN Employee e on pmm.projectManagerId = e.empId where pmm.projectId =:projectId and pmm.active=1")
	List<ProjectManagersDTO> getAllProjectManagerListWithName(@Param("projectId") Long projectId);
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.ProjectManagersDTO(pmm.projectId,pmm.projectManagerId, e.name)FROM ProjectManagerMapping pmm \n"
			+ "INNER JOIN Employee e on pmm.projectManagerId = e.empId where pmm.projectId  IN :projectId and pmm.active=1")
	List<ProjectManagersDTO> getAllProjectManagerListWithNameThroughPids(@Param("projectId") List<Long> projectId);
	
	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM project_manager_mapping pm " +
            "JOIN projects p ON p.project_id = pm.project_id " +
            "WHERE p.active = 'true' AND p.po_project_id IS NULL AND pm.project_manager_id = :empId", nativeQuery = true)
boolean isUserProjectManagerOfAnyActiveInternalProject(@Param("empId") Long empId);
	@Query(value = "SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
		       "FROM ProjectManagerMapping pm " +
		       "JOIN Project p ON pm.projectId = p.projectId " +
		       "WHERE p.active = 'true' AND pm.projectManagerId = :empId")
boolean isUserProjectManagerOfAnyActiveInternalAndExternalProject(@Param("empId") Long empId);
	@Query(value = "SELECT Distinct p.project_id " +
            "FROM project_manager_mapping pm " +
            "JOIN projects p ON p.project_id = pm.project_id " +
            "WHERE p.active = 'true' AND pm.project_manager_id = :empId", nativeQuery = true)
List<Integer> isUserProjectManagerOfAnyActiveInternalProjectList(@Param("empId") Long empId);

	Optional<List<ProjectManagerMapping>> findByProjectManagerIdAndActive(Long projectManagerId, Integer active);
	
	

}
