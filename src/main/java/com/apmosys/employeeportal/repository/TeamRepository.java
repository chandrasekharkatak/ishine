package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>{

	@Query(nativeQuery = true)
	public List<Object[]> projectTeamsByProjectId(Integer projectId);

	public Team findByTeamName(String teamName);

	public Team findByTeamNameAndProjectId(String teamName, Integer projectId);

	public Team findByPoTeamId(Long poTeamId);

	public Team findByTeamNameAndTeamIdAndProjectId(String teamName, Long teamId, Integer projectId);

	public List<Team> findByProjectId(Integer projectId);

	public Long countByProjectId(Integer projectId);

	public List<Team> findByProjectIdAndIsActive(Integer projectId, String string);

	public List<Team> findByTeamIdNotInAndProjectId(List<Long> allTeam, Integer projectId);

	public Team findByTeamIdAndProjectId(Long teamId, Integer projectId);

}
