package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO;
import com.apmosys.employeeportal.model.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>{

	@Query(nativeQuery = true)
	public List<Object[]> projectTeamsByProjectId(Integer projectId);

	public Team findByTeamName(String teamName);

	public Team findByTeamNameAndProjectId(String teamName, Integer projectId);

	public Team findByPoTeamId(Long poTeamId);

//	public Team findByTeamNameAndTeamIdAndProjectId(String teamName, Long teamId, Integer projectId);

	public List<Team> findByProjectId(Integer projectId);

	public Long countByProjectId(Integer projectId);

	public List<Team> findByProjectIdAndIsActive(Integer projectId, String string);

	public List<Team> findByTeamIdNotInAndProjectId(List<Long> allTeam, Integer projectId);

	public Team findByTeamIdAndProjectId(Long teamId, Integer projectId);

	public Team findByTeamNameAndTeamIdAndProjectIdAndIsActive(String teamName, Long teamId, Integer projectId,
			String string);

	public Team findByTeamNameAndProjectIdAndIsActive(String teamName, Integer projectId, String string);

	@Query(nativeQuery = true)
	public List<Object[]> getAllTeams();

//	@Query(nativeQuery = true)
//	public List<Team> findTeamByProjectId(Integer projectId);
	
	// @Query("SELECT t FROM Team t WHERE t.projectId = :projectId")
//	@Query(nativeQuery = true)
//	public List<Team> findTeamByProjectId(Integer projectId);
	
	@Query(value="select t from Team t where t.projectId=:projectId")
	public List<Team> findTeamByProjectId(Integer projectId);
	

	public Team findTeamByTeamId(Long teamId);
	
	public List<Team> findTeamListByProjectId (Integer projectId);

	
	public Team findByTeamId(Long teamId);
	
	List<Team> findByIsActiveNot(String status);
	
	@Query(value = "SELECT etm.emp_id FROM employee_team_mapping etm WHERE etm.team_id = :teamId AND etm.active != 0 ", nativeQuery = true)
    List<Long> findEmployeeIdsByTeamId(@Param("teamId") Long teamId);
	
	@Modifying
	@Query(nativeQuery = true)
	public void updateTeamProjectByPoProjectId(Long teamId, Long poProjectId);
	
	@Modifying
	@Query(nativeQuery = true)
	public void updateTeamName(Long teamId, String newTeamName);
	
	@Query(value = "SELECT\r\n"
			+ "    t.team_id,\r\n"
			+ "    t.team_name,\r\n"
			+ "    e.emp_id AS spoc_id,\r\n"
			+ "    e.name AS spoc_name,\r\n"
			+ "    t.dept_ids\r\n"
			+ "FROM\r\n"
			+ "    teams t\r\n"
			+ "LEFT JOIN\r\n"
			+ "    employee e ON t.spoc_id = e.emp_id\r\n"
			+ "WHERE\r\n"
			+ "    t.project_id = :projectId",nativeQuery = true)
	List<Object[]> findTeamsAndSpocsByProjectId(@Param("projectId") Integer projectId);
	
	 boolean existsBySpocId(Long spocId);
	 
	 
	 @Query(value = "select DISTINCT t.projectId from Team t inner join Project p on p.projectId = t.projectId where t.spocId = :spocId and t.isActive = 'Y' and p.active = 'true' and p.poProjectId IS NULL")
	 Set<Integer>findActiveInternalProjectIdsBySpocId(@Param("spocId") Long spocId);
	 
	 @Query(value = "select DISTINCT t.projectId from Team t inner join Project p on p.projectId = t.projectId where t.spocId = :spocId and t.isActive = 'Y' and p.active = 'true' and p.poProjectId IS NOT NULL")
	 Set<Integer>findActiveShankhProjectIdsBySpocId(@Param("spocId") Long spocId);
	
	 @Query(value = "select DISTINCT t.projectId from Team t inner join Project p on p.projectId = t.projectId where t.spocId = :spocId and t.isActive = 'Y' and p.active = 'true'")
	 Set<Integer>findActiveShankhInternalProjectIdsBySpocId(@Param("spocId") Long spocId);
	 
	 @Query(value = "select DISTINCT t.projectId from Team t inner join Project p on p.projectId = t.projectId where t.spocId = :spocId and t.isActive = 'Y' and p.active = 'true'")
	 List<Integer>findActiveShankhInternalProjectIdsBySpocIdList(@Param("spocId") Long spocId);
	
	@Query(value = "select t from Team t inner join Project p on p.projectId = t.projectId where  t.isActive = 'Y' and p.active = 'true' and p.poProjectId IS NULL")
	List<Team> findAllActiveTeamsOfInternalProjects();
	
	@Query(value = "select t from Team t inner join Project p on p.projectId = t.projectId where  t.isActive = 'Y' and p.active = 'true' and p.poProjectId IS NOT NULL")
	List<Team> findAllActiveTeamsOfShankhProjects();
	
	
	  @Query(value ="select t from Team t where t.isActive = 'Y' and t.projectId = :projectId")
	  List<Team> findActiveTeamsByProjectId(@Param("projectId") Integer projectId);
	
	  @Query(value = "select t from Team t inner join Project p on p.projectId = t.projectId where  t.isActive = 'Y' and p.active = 'true'")
		List<Team> findAllActiveTeamsOfShankhInternalProjects();
		
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO(e.empId, e.name,  \n " + 
			"CASE  \n " + 
			"  WHEN isConsultant = 'true' THEN CONCAT('CS-', e.employeementId)  \n " + 
			"  ELSE CONCAT('A-', e.employeementId)  \n " + 
			"END)  \n " + 
			"FROM Employee e where e.employmentstatus != 'InActive' and e.empId = :empId and e.empId not between 1 and 6")
    public GetEmployeeByNameAndEmpldDTO getSpocDetils(Long empId);
	
	@Query("SELECT t FROM Team t WHERE t.projectId IN :projectIds")
	List<Team> findByProjectIdIn(@Param("projectIds") List<Integer> projectIds);

	public List<Team> findByTeamLeadIdAndIsActive(Long teamLeadId, String isActive);

	public List<Team> findBySpocIdAndIsActive(Long spocId, String string);
	
	@Query(value ="select t from Team t where t.isActive = 'Y' and t.teamId in (:teamIds)")
	  List<Team> findActiveTeamsByTeamIds(@Param("teamIds") List<Long> teamIds);
}
