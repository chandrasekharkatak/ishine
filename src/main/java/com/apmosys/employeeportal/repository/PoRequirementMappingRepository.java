package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.PoRequirementDataDTO;
import com.apmosys.employeeportal.dto.PoTeamAndMemberDetailsDto;
import com.apmosys.employeeportal.dto.ProjectRequirementsDTO;
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
			+ "WHERE prm.poId =:poId  \n")
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

	@Modifying
	@Query("DELETE FROM PoRequirementMapping")
	void deleteAllRecords();

	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.RmgResourceRequirementDto(" +
			"prm.poRequirementMappingId, pd.poId," +
			"prm.role, prm.experience, prm.department, " +
			"prm.active, prm.count) " +
			"FROM PoRequirementMapping prm " +
			"INNER JOIN ProjectPoDetails pd ON pd.poId = prm.poId " +
			"WHERE prm.active = true " +
			"AND prm.poId = :poId")
	List<RmgResourceRequirementDto> getPoRequirementDataByPoId(@Param("poId") Long poId);

	@Query("SELECT DISTINCT new com.apmosys.employeeportal.dto.RmgResourceRequirementDto(" +
			"prm.poRequirementMappingId, prm.poId," +
			"prm.role, prm.experience, prm.department, " +
			"prm.active, prm.count) " +
			"FROM PoRequirementMapping prm " +
			"LEFT JOIN EmployeeTeamMap etm ON etm.poRequirementMappingId = prm.poRequirementMappingId " +
			"LEFT JOIN Team t ON etm.teamId = t.teamId " +
			"WHERE prm.active = true " +
			"AND t.teamId = :teamId ")
	List<RmgResourceRequirementDto> getPoRequirementDataByTeamId(@Param("teamId") Long teamId);

	@Query(value = "Select sum(prm.count) from PoRequirementMapping prm where prm.poId=:poId and prm.active = true")
	public Long getTotalActiveRequiredCountByPoId(Long poId);

	@Query(value = "Select prm.poId,sum(prm.count) from PoRequirementMapping prm where prm.poId IN :poIds and prm.active = true")
	public List<Object[]> getPoIdAndTotalActiveRequiredCountByPoIdIn(List<Long> poIds);

	@Query(value = "Select new com.apmosys.employeeportal.dto.RmgResourceRequirementDto(prm.poId \n"
			+ ",COUNT(DISTINCT CASE WHEN etm.active = 2 THEN etm.empId END) \n"
			+ ",COUNT(DISTINCT CASE WHEN etm.active = 1 THEN etm.empId END)  \n"
			+ ") \n"
			+ "FROM PoRequirementMapping prm \n"
			+ "LEFT JOIN Team t ON t.poId = prm.poId AND t.isActive = 'Y'  \n"
			+ "LEFT JOIN EmployeeTeamMap etm ON t.teamId = etm.teamId AND etm.active IN (1, 2)\n"
			+ "where prm.poId IN :poIds \n"
			+ "GROUP BY prm.poId ")
	public List<RmgResourceRequirementDto> getPoIdAndRequiredCountByPoIdIn(List<Long> poIds);

	List<PoRequirementMapping> findByPoIdAndActiveTrue(Long poId);
	
}