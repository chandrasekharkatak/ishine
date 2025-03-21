package com.apmosys.employeeportal.repository;

import java.util.List;

import org.hibernate.query.NativeQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

	public List<Project> findAllByProjectManagerId(Long projectManagerId);
	
	public List<Project> findByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getActivitiesByTeamIdAndEmployeeId(Long teamId, Long empId);

	public Project findByProjectName(String projectName);

	@Query(nativeQuery = true)
	public List<Object[]> getAllProject();

	public Project findByPoProjectId(Long poProjectId);

	public boolean existsProjectByProjectName(String projectName);

	public List<Project> findBySyncProject(String sync);

	@Query(nativeQuery = true)
	public List<Object[]> getAllMyProjectByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> findProjectByIsDraftProject();

	@Query(nativeQuery = true)
	public List<Object[]> getAllInternalProject();

	public Project findByProjectId(Integer projectId);

	// added by anurag
	@Query(nativeQuery = true)
	public List<Object[]> findAllProjectByIsDraftAndIsActive();
	

	public List<Project> findProjectByDepartmentName(String name);

	public Project findByProjectIdAndProjectManagerId(Integer projectId, Long managerId);

	@Query(nativeQuery=true,value="select DISTINCT po_project_type from projects where po_project_type IS NOT NULL")
	List<String>finddistinctPoProjectType();
	
}
