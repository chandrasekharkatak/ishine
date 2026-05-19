package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.ProjectOverheadsDTO;
import com.apmosys.employeeportal.model.ProjectManagerMapping;
import com.apmosys.employeeportal.model.ProjectOverheadMapping;


public interface ProjectOverheadMappingRepository extends JpaRepository<ProjectOverheadMapping,Long> {
	
	public List<ProjectOverheadMapping> findByProjectId(Long projectId);
	
	public ProjectOverheadMapping findByProjectIdAndProjectOverheadId(Long projectId,Long projectOverheadId);
	
	@Query(value=" select new com.apmosys.employeeportal.dto.ProjectOverheadsDTO(pom.projectOverheadId,e.name) from ProjectOverheadMapping pom \n"+
			"INNER JOIN Employee e ON e.empId = pom.projectOverheadId where pom.projectId =:projectId and pom.active = 1 \n")
	public List<ProjectOverheadsDTO>findProjectOverheadsPerProject(@Param("projectId") Long projectId);
	
	@Query(value=" select new com.apmosys.employeeportal.dto.ProjectOverheadsDTO(pom.projectId,pom.projectOverheadId,e.name) from ProjectOverheadMapping pom \n"+
			"INNER JOIN Employee e ON e.empId = pom.projectOverheadId where pom.projectId IN :projectId and pom.active = 1 \n")
	public List<ProjectOverheadsDTO>findProjectOverheadsPerProjectThroughPidList(@Param("projectId") List<Long> projectId);
	
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
	
	
	List<ProjectOverheadMapping> findByProjectOverheadIdAndActive(Long projectOverheadMappingId, Integer active);
	
	List<ProjectOverheadMapping> findByProjectIdAndActive(Long projectId, Integer active);
	
	@Modifying
	@Transactional
	@Query(value="UPDATE ProjectOverheadMapping p SET p.active = 0 WHERE p.projectId =:projectId")
	public void deactivateByProjectId(@Param("projectId") Long projectId);

	@Query(value=" select DISTINCT pom.projectOverheadId from ProjectOverheadMapping pom \n"+
			"INNER JOIN Employee e ON e.empId = pom.projectOverheadId where pom.projectId =:projectId \n")
    public List<Long> getAllProjectOverheadId(Long projectId);
	
	@Query("SELECT DISTINCT pom.projectOverheadId from ProjectOverheadMapping pom where pom.projectId = :projectId and pom.active=:active")
	List<Long> findProjectOverheadIdByProjectIdAndActive(Long projectId, Integer active);
	
	
	@Query("SELECT DISTINCT e.email " +
		       "FROM ProjectOverheadMapping po " +
		       "JOIN Employee e ON po.projectOverheadId = e.empId " +
		       "WHERE po.projectId = :projectId " +
		       "AND po.active = 1")
		List<String> findProjectOverheadEmails(Long projectId);
	
}
