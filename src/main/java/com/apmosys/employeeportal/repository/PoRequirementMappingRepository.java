package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.PoRequirementDataDTO;
import com.apmosys.employeeportal.dto.PoTeamAndMemberDetailsDto;
import com.apmosys.employeeportal.dto.RmgResourceRequirementDto;
import com.apmosys.employeeportal.model.PoRequirementMapping;

@Repository
public interface PoRequirementMappingRepository extends JpaRepository<PoRequirementMapping, Long> {

	@Query(value = "SELECT DISTINCT new com.apmosys.employeeportal.dto.PoTeamAndMemberDetailsDto( \n"
			+ "  prm.poId, prm.id, t.teamId, t.teamName, t.isActive, t.deptIds, es.empId, es.name \n"
			+ ", prm.role, prm.experience, prm.department, prm.active \n"
			+ ", e.name, etm.employeeRole, etm.startDate, etm.endDate, etm.isShadow, etm.active \n"
			+ ", CASE WHEN eppm.id IS NOT NULL THEN true ELSE false END , e.empId)  \n"
			+ "FROM PoRequirementMapping prm \n"
			+ "INNER JOIN EmployeeTeamMap etm ON etm.poRequirementMappingId = prm.id  \n"
			+ "LEFT JOIN Employee e ON e.empId = etm.empId \n"
			+ "LEFT JOIN EmpPrimaryProjectMapping eppm ON eppm.empId = e.empId \n"
			+ "INNER JOIN Team t ON t.teamId = etm.teamId \n"
			+ "LEFT JOIN Employee es ON es.empId = t.spocId \n"
			+ "WHERE prm.poId =:poId and prm.active = true  \n")
	List<PoTeamAndMemberDetailsDto> getAllTeamAndMemberDetailsDtoByPoId(Long poId);

	PoRequirementMapping findByPoIdAndDepartment(Long poId, String department);

	@Query("select distinct e.employeementId from EmployeeTeamMap etm " +
			"inner join Employee e on etm.empId = e.empId " +
			"where etm.active != 0 and etm.poRequirementMappingId in :poRequirementMappingId")
	List<Long> checkActiveAndPendingEmployeeMappingWithPoRequirementId(
			@Param("poRequirementMappingId") List<Long> poRequirementMappingId);

	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.PoRequirementDataDTO(" +
			"po.poRequirementMappingId, " +
			"po.poId, po.role, po.experience, po.department, " +
			"t.teamId, p.projectId, p.projectName, " +
			"pd.poNo, pd.poStartDate, pd.poEndDate) " +
			"FROM PoRequirementMapping po " +
			"LEFT JOIN EmployeeTeamMap etm ON etm.poRequirementMappingId = po.poRequirementMappingId " +
			"LEFT JOIN Team t ON etm.teamId = t.teamId " +
			"LEFT JOIN ProjectPoDetails pd ON pd.poId = po.poId " +
			"LEFT JOIN Project p ON t.projectId = p.projectId " +
			"WHERE po.active = true " +
			"AND t.teamId = :teamId " +
			"AND po.poId = :poId")
	List<PoRequirementDataDTO> getPoRequirementDataByTeamAndPoId(
			@Param("teamId") Long teamId,
			@Param("poId") Long poId);

	@Query("SELECT prm FROM PoRequirementMapping prm " +
			"WHERE prm.poId = :poId AND prm.active = true")
	List<PoRequirementMapping> findByPoId(@Param("poId") Long poId);

	
	@Transactional
	@Modifying
	@Query("DELETE FROM PoRequirementMapping")
	void deleteAllRecords();

	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.RmgResourceRequirementDto("
			+ " prm.poRequirementMappingId, prm.poId "
			+ ", rd.roleId, rd.role, rd.experience, rd.department "
			+ ", prm.count, prm.lineItemStartDate, prm.lineItemEndDate) "
			+ "FROM RoleDetails rd \n"
			+ "LEFT JOIN PoRequirementMapping prm ON prm.roleId = rd.roleId and prm.poId =:poId and prm.active = true  \n"
			+ "LEFT JOIN ProjectPoDetails ppd ON ppd.poId = prm.poId AND ppd.active = true \n"
			+ "WHERE prm.active = true and prm.poId =:poId")
	List<RmgResourceRequirementDto> getPoRequirementDataByPoId(@Param("poId") Long poId);
	
	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.RmgResourceRequirementDto("
			+ "prm.poRequirementMappingId, prm.poId "
			+ ", rd.roleId, rd.role, rd.experience, rd.department, prm.active, prm.count) "
			+ "FROM Team t \n"
			+ "LEFT JOIN EmployeeTeamMap etm ON etm.teamId = t.teamId \n"
			+ "LEFT JOIN RoleDetails rd ON etm.roleId = rd.roleId \n"
			+ "LEFT JOIN PoRequirementMapping prm ON prm.roleId = rd.roleId and prm.poId = etm.poId and prm.active = true  \n"
			+ "LEFT JOIN ProjectPoDetails ppd ON ppd.poId = prm.poId AND ppd.active = true \n"
			+ "WHERE prm.active = true AND t.teamId = :teamId ")
	List<RmgResourceRequirementDto> getPoRequirementDataByTeamId(@Param("teamId") Long teamId);

	@Query(value = "Select sum(prm.count) from PoRequirementMapping prm \n"
			+ "INNER JOIN ProjectPoDetails ppd on ppd.poId = prm.poId where prm.poId=:poId and prm.active = true and ppd.projectId=:projectId")
	public Long getTotalActiveRequiredCountByPoIdAndProjectId(Long poId, Integer projectId);

	@Query(value = "Select prm.poId,sum(prm.count) from PoRequirementMapping prm \n"
			+ "INNER JOIN ProjectPoDetails ppd on ppd.poId = prm.poId where prm.poId IN :poIds and prm.active = true and ppd.projectId=:projectId group by prm.poId")
	public List<Object[]> getPoIdAndTotalActiveRequiredCountByPoIdInAndProjectId(List<Long> poIds, Integer projectId);

	@Query(value = "Select prm.poRequirementMappingId,sum(prm.count) from PoRequirementMapping prm \n"
			+ "INNER JOIN ProjectPoDetails ppd on ppd.poId = prm.poId where prm.poId IN :poIds and prm.active = true and ppd.projectId=:projectId group by prm.poRequirementMappingId")
	public List<Object[]> getPrmIdAndTotalActiveRequiredCountByPoIdInAndProjectId(List<Long> poIds, Integer projectId);

	@Query(value = "Select new com.apmosys.employeeportal.dto.RmgResourceRequirementDto(prm.poRequirementMappingId \n"
			+ ",COUNT(DISTINCT CASE WHEN etm.active = 2 THEN etm.empId END) \n"
			+ ",COUNT(DISTINCT CASE WHEN etm.active = 1 THEN etm.empId END)  \n"
			+ ") \n"
			+ "FROM PoRequirementMapping prm \n"
			+ "INNER JOIN ProjectPoDetails ppd on ppd.poId = prm.poId \n"
			+ "LEFT JOIN Team t ON t.poId = prm.poId AND t.isActive = 'Y'  \n"
			+ "LEFT JOIN EmployeeTeamMap etm ON t.teamId = etm.teamId AND etm.active IN (1, 2)\n"
			+ "where prm.poId IN :poIds and ppd.projectId=:projectId and prm.active = true  \n"
			+ "GROUP BY prm.poId ")
	public List<RmgResourceRequirementDto> getPoIdAndRequiredCountByPoIdInAndProjectId(List<Long> poIds, Integer projectId);

	List<PoRequirementMapping> findByPoIdAndActiveTrue(Long poId);

	@Query(value = "Select sum(prm.count) from PoRequirementMapping prm \n"
			+ " INNER JOIN ProjectPoDetails ppd on ppd.poId = prm.poId AND ppd.active = true AND DATE(ppd.poStartDate) <= CURDATE() AND (ppd.poEndDate IS NULL OR DATE(ppd.poEndDate) >= CURDATE()) \n"
			+ " WHERE 1=1 \n"
			+ " AND ppd.projectId=:projectId and prm.active = true \n"
			+ " AND DATE(prm.lineItemStartDate) <= CURDATE() AND (prm.lineItemEndDate IS NULL OR DATE(prm.lineItemEndDate) >= CURDATE()) ")
	public Long getTotalActiveRequiredCountByProjectId(Integer projectId);

	
	@Query(value ="SELECT distinct p.roleId \n"
			+ "FROM PoRequirementMapping p\n"
			+ "WHERE p.poId = :poId")
	List<Long> findRoleIdsByPoId(Long poId);

	Optional<PoRequirementMapping> findByPoIdAndRoleId(Long poId, Long roleId);
	
	
}