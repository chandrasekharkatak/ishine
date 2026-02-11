package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO;
import com.apmosys.employeeportal.dto.PoTeamAndMemberDetailsDto;
import com.apmosys.employeeportal.dto.ProjectManagerIdAndNameDTO;
import com.apmosys.employeeportal.dto.RmgResourceRequirementDto;
import com.apmosys.employeeportal.dto.RmgTeamDto;
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
	@Transactional
	@Query(value="UPDATE Team t SET t.projectId = (SELECT p.projectId FROM Project p WHERE p.poProjectId =:poProjectId) WHERE t.teamId =:teamId")
	public void updateTeamProjectByPoProjectId(Long teamId, Long poProjectId);
	
	@Modifying
	@Transactional
	@Query(value="UPDATE Team t SET t.teamName =:newTeamName WHERE t.teamId =:teamId")
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
	
	@Query(nativeQuery = true, value = "select * from teams t inner join projects p on p.project_id = t.project_id where p.po_project_id = :id and t.is_active = \"Y\"")
	public List<Team> findTeamandIsActive(@Param("id") Long id);
	
	@Query(value ="select t from Team t where t.isActive = 'Y' and t.teamId in (:teamIds)")
	public List<Team> findActiveTeamsByTeamIds(@Param("teamIds") List<Long> teamIds);
	
	public List<Team> findByProjectIdAndIsActive(Long projectId, String isActive);
	
	@Query("SELECT t FROM Team t WHERE t.updatedOn = (SELECT MAX(t2.updatedOn) FROM Team t2 WHERE t2.projectId = :projectId)")
	List<Team> findLatestTeamsPerProject(@Param("projectId") Integer projectId);



	@Query("SELECT new com.apmosys.employeeportal.dto.ProjectManagerIdAndNameDTO(e.empId , e.name) from Employee e, Team t where e.empId = t.teamLeadId and t.projectId = :projectId")
	List<ProjectManagerIdAndNameDTO> findAllTeamLeadByProjectId(@Param("projectId") Integer projectId);
	



	@Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.RmgTeamDto( \n"
			+ "  t.teamId, t.teamName, t.poId, t.isActive, es.empId, es.name, t.deptIds \n"
			+ ", t.teamLeadId, tl.name)  \n"
			+ "FROM Team t \n"
			+ "LEFT JOIN Employee es ON es.empId = t.spocId \n"
			+ "LEFT JOIN Employee tl ON tl.empId = t.teamLeadId \n"
			+ "WHERE t.poId =:poId  \n")
	List<RmgTeamDto> getAllTeamsByPoId(Long poId);

	@Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.PoTeamAndMemberDetailsDto( \n"
			+ " prm.poId, prm.id, prm.role, prm.experience, prm.department, prm.active \n"
			+ ", e.empId, e.name, etm.employeeRole, etm.active, etm.isShadow, CASE WHEN eppm.id IS NOT NULL THEN true ELSE false END \n"
			+ ", etm.startDate, etm.endDate)  \n"
			+ "FROM Team t \n"
			+ "INNER JOIN EmployeeTeamMap etm ON t.teamId = etm.teamId \n"
			+ "LEFT JOIN PoRequirementMapping prm ON etm.poRequirementMappingId = prm.id  \n"
			+ "LEFT JOIN Employee e ON e.empId = etm.empId \n"
			+ "LEFT JOIN EmpPrimaryProjectMapping eppm ON eppm.empId = e.empId AND eppm.isMapped = 'Y' AND 	eppm.primaryProjectId=:projectId \n"
			+ "WHERE t.teamId =:teamId  \n")
	List<PoTeamAndMemberDetailsDto> getAllTeamMemberDetailsDtoByPoId(Long teamId,Long projectId);

	@Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.PoTeamAndMemberDetailsDto( \n"
			+ "  prm.poId, prm.id, prm.role, prm.experience, prm.department, prm.active \n"
			+ ", e.empId, e.name, etm.employeeRole, etm.active, etm.isShadow, CASE WHEN eppm.id IS NOT NULL THEN true ELSE false END \n"
			+ ", etm.startDate, etm.endDate)  \n"
			+ "FROM Team t \n"
			+ "INNER JOIN EmployeeTeamMap etm ON t.teamId = etm.teamId \n"
			+ "LEFT JOIN PoRequirementMapping prm ON etm.poRequirementMappingId = prm.id  \n"
			+ "LEFT JOIN Employee e ON e.empId = etm.empId \n"
			+ "LEFT JOIN EmpPrimaryProjectMapping eppm ON eppm.empId = e.empId \n"
			+ "WHERE t.teamId IN :teamIds  \n")
	List<PoTeamAndMemberDetailsDto> getAllTeamMemberDetailsDtoByPoIdIn(List<Long> teamIds);

	@Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.RmgTeamDto( \n"
			+ "  t.teamId, t.teamName, t.poId, t.isActive, es.empId, es.name, t.deptIds \n"
			+ ", t.teamLeadId, tl.name)  \n"
			+ "FROM Team t \n"
			+ "LEFT JOIN Employee es ON es.empId = t.spocId \n"
			+ "LEFT JOIN Employee tl ON tl.empId = t.teamLeadId \n"
			+ "WHERE t.poId =:poId AND t.isActive = 'Y' \n")
	List<RmgTeamDto> getActiveTeamDetailsByPoId(Long poId);

	@Query(value ="select t.teamName from Team t where t.isActive = 'Y' and t.teamId in (:teamIds)")
	public List<String> findActiveTeamNameByTeamIds(@Param("teamIds") List<Long> teamIds);
	public boolean existsByPoIdAndIsActive(Long poId, String string);

	@Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.RmgTeamDto( \n"
			+ "  t.teamId, t.teamName, t.poId, t.isActive, es.empId, es.name, t.deptIds \n"
			+ ", t.teamLeadId, tl.name)  \n"
			+ "FROM Team t \n"
			+ "LEFT JOIN Employee es ON es.empId = t.spocId \n"
			+ "LEFT JOIN Employee tl ON tl.empId = t.teamLeadId \n"
			+ "WHERE t.projectId =:projectId AND t.isActive = 'Y' \n")
	List<RmgTeamDto> getActiveTeamDetailsByProjectId(Integer projectId);

	@Query(value = "Select new com.apmosys.employeeportal.dto.RmgResourceRequirementDto(t.teamId \n"
			+ ",COUNT(DISTINCT CASE WHEN etm.active = 2 THEN etm.empId END) \n"
			+ ",COUNT(DISTINCT CASE WHEN etm.active = 1 THEN etm.empId END)  \n"
			+ ") \n"
			+ "FROM Team t \n"
			+ "LEFT JOIN EmployeeTeamMap etm ON t.teamId = etm.teamId AND etm.active IN (1, 2)\n"
			+ "where t.teamId IN :teamIds AND t.isActive = 'Y'  \n"
			+ "GROUP BY t.teamId ")
	public List<RmgResourceRequirementDto> getTeamIdAndRequiredCountByTeamIdIn(List<Long> teamIds);
	
}
