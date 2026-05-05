package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.ProjectManagerIdAndNameDTO;
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

	List<ProjectManagerMapping> findByProjectManagerIdAndActive(Long projectManagerId, Integer active);

	
//	@Query(value = "select p.* from project_manager_mapping pm\n"
//			+ "inner join projects p on pm.project_id = p.project_id\n"
//			+ "where pm.project_manager_id= :project_manager_id and pm.active =1")
//	public Optional<List<ProjectManagerMapping>> findProjectsOfProjectManager(Long empId);
	
	List<ProjectManagerMapping> findByProjectIdAndActive(Long projectId, Integer active);

	 @Query("SELECT new com.apmosys.employeeportal.dto.ProjectManagerIdAndNameDTO(e.empId , e.name) from Employee e, ProjectManagerMapping pmm where e.empId = pmm.projectManagerId and pmm.projectId = :projectId ")
    List<ProjectManagerIdAndNameDTO> findProjectManagerIdAndName(@Param("projectId") Long projectId);

	@Query("SELECT DISTINCT e.empId from Employee e INNER JOIN ProjectManagerMapping pmm ON e.empId = pmm.projectManagerId where pmm.projectId = :projectId ")
	public List<Long> getAllProjectManagerId(@Param("projectId") Long projectId);

	@Query("SELECT DISTINCT pmm.projectManagerId from ProjectManagerMapping pmm where pmm.projectId = :projectId and pmm.active=:active")
	List<Long> findProjectManagerIdByProjectIdAndActive(Long projectId, Integer active);
	
	@Query("SELECT DISTINCT e.email " +
		       "FROM ProjectManagerMapping pm " +
		       "JOIN Employee e ON pm.projectManagerId = e.empId " +
		       "WHERE pm.projectId = :projectId " +
		       "AND pm.active = 1")
		List<String> findProjectManagerEmails(Long projectId);

}
