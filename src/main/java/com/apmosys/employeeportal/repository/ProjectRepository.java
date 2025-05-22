package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Set;

import org.hibernate.query.NativeQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
	
	@Query(nativeQuery = true,value ="select p.project_id, p.project_name, p.is_draft_project,(case when exists (select 1 from employee_team_mapping etm where etm.team_id in \n"
			+ "(select team_id from teams where project_id=p.project_id) and etm.active=2) then 2 else 1 end) from projects p  \n"
			+ "where p.is_draft_project IN ('false','true','Rejected') and p.project_id IN :projectIds")
	public List<Object[]> findAllProjectByIsDraftAndIsActiveOfProjectIds(@Param("projectIds") Set<Integer> projectIds);
	

	public List<Project> findProjectByDepartmentName(String name);

	public Project findByProjectIdAndProjectManagerId(Integer projectId, Long managerId);

	// @Query(nativeQuery=true,value="select DISTINCT po_project_type from projects where po_project_type IS NOT NULL")
	// List<String>finddistinctPoProjectType();

	@Query(nativeQuery=true,value="SELECT * FROM projects p inner join teams t on p.project_id = t.project_id WHERE STR_TO_DATE(p.po_end_date, '%Y-%m-%d') < CURDATE() ")
	public List<Project> getExpiredPolist();
	
	@Query(nativeQuery = true)
	public List<Object[]> getProjectInfo(Integer projectId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getPoProjectInfo(Integer poProjectId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getTeamInfo(Integer projectId);
	
	@Query(nativeQuery=true,value="select DISTINCT po_project_type from projects where po_project_type IS NOT NULL")
	List<String>finddistinctPoProjectType();
	
	@Query(nativeQuery=true)
	public List<Object[]> getPoProjectDetailsForPoProjects();
	
	
	@Query(nativeQuery = true)
	public List<Object[]> getPoProjectDetailsBOthPOAndInternal();
	
	@Query(nativeQuery = true)
	List<Object[]> getExpiredPoProjects();
	
	@Query(nativeQuery = true)
	List<String> getEmployeeEmailsByProjectId(@Param("projectId") Integer projectId);

	@Query(nativeQuery = true)
	List<Object[]> getExpiredPoProjectsWithoutInterval();
	
	@Query(nativeQuery = true)
	List<Object[]> poProjectTimesheetSync(Set<Long> poProjectIdList);
	
	@Query(nativeQuery = true)
	public List<Object[]> getTeamIdsForPoProjectId(Long poProjectId);
	
	@Query(nativeQuery = true)
	public List<Object[]> getEmpIdAndEmployeeRoleForLinkedPoTeams(Long teamId);

	@Query(nativeQuery = true)
	public List<Object[]> getEmployeesByPoProjectId(String poProjectId);
	
	
	 @Query(nativeQuery = true,value ="select project_id from projects where active = 'true' and po_project_type IS NULL")
	 Set<Integer> findAllActiveInternalProjectIds();
	 
	 @Query(nativeQuery = true,value ="select project_id from projects where active = 'true' and po_project_type IS NOT NULL")
	 Set<Integer> findAllActiveShankhProjectIds();
	 
	 @Query(nativeQuery = true,value ="select project_id from projects where active = 'true'")
	 Set<Integer> findAllActiveShankhInternalProjectIds();
	 
	@Query(nativeQuery = true)
	public int getAssignedEmployeesCountInProject(Long id);
	
}
