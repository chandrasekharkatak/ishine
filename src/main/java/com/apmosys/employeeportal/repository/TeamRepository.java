package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

	@Query(nativeQuery = true)
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
}
