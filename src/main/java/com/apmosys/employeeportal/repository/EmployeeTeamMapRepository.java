package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTeamMap;

@Repository
public interface EmployeeTeamMapRepository extends JpaRepository<EmployeeTeamMap, Long> {

	@Transactional
	void deleteAllByTeamId(Long teamId);

	@Query(nativeQuery = true)
	public List<Object[]> getTeamMembersByTeamId(Long teamId);

	List<EmployeeTeamMap> findByEmpId(Long empId);
	
	@Query(nativeQuery = true)
	public List<Object[]> findProjectsByTeamId(Long empId);

	Long countByTeamId(Long teamId);

	EmployeeTeamMap findByEmployeeTeamMapId(Long employeeTeamMapId);

	List<EmployeeTeamMap> findByEmpIdNotInAndTeamId(List<Long> teamMemberList, Long teamId);

	List<EmployeeTeamMap> findFirstByEmpIdAndTeamId(Long empId, Long teamId);

	@Query(nativeQuery = true)
	List<EmployeeTeamMap> findFirstByEmpIdAndTeamIdAndActive(Long empId, Long teamId);

	List<EmployeeTeamMap> findByTeamId(Long teamId);

	EmployeeTeamMap findByEmpIdAndTeamIdAndActive(Long empId, Long teamId, long l);

	@Query(nativeQuery = true)
	EmployeeTeamMap findByEmpIdAndTeamIdAndActiveStatus(Long empId, Long teamId);
	
	@Query(nativeQuery = true)
	List<EmployeeTeamMap> findByTeamIdAndActive(Long teamId);

	@Query(nativeQuery = true)
	List<Object[]> getAllProjectByEmpId(Long empId);

	//added by rahul SIngh
	@Query(nativeQuery = true)
	List<Object[]> findEmployeeByTeamId(Long team_id);
	@Query(nativeQuery = true)
	List<EmployeeTeamMap> findTeamListByTeamIdAndStatus(Long teamId);
	
	@Query(nativeQuery = true)
	EmployeeTeamMap findEmployeeByTeamIdAndEmpId(Long empId , Long teamId);
	
	EmployeeTeamMap findEmployeeByEmpIdAndActive(Long empId , long l);

	List<EmployeeTeamMap> findEmployeeByEmpId(Long empId);
	
//	@Query(nativeQuery = true)
//	List<Object[]> findEmployeeByProjectId(Integer projectId );
	
	 @Query(nativeQuery = true)
List<EmployeeTeamMap> findByProjectIdAndActive(Integer projectId,Long active);

	 @Query(nativeQuery = true)
	List<EmployeeTeamMap> findTeammembersByTeamIdAndStatus(Long teamId);

//	 @Query(nativeQuery = true , value = "SELECT * FROM employee_team_mapping etm WHERE etm.team_id = :teamId")
//	List<EmployeeTeamMap> findTeammembersByTeamId(Long teamId);
	 
	 @Query(nativeQuery = true)
	 List<Object[]> findTeammembersByTeamId(Long teamId);

	 @Query(nativeQuery = true)
	List<Object[]> getAllProjectsAndTeamsDetails(Long empId);

	 @Query(nativeQuery = true)
	List<Object[]> findTeammembersByTeamIdAndManagerId(Long teamId, Long managerId);

	 @Query(nativeQuery = true)
	List<Object[]> getAllProjectsTeamsInfo(Long empId);

	@Modifying
	@Transactional
	@Query(nativeQuery = true)
	void updateActiveFieldToZero(Long key);

}
