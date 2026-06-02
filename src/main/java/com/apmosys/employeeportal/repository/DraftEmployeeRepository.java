package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;

import javax.transaction.Transactional;

@Repository
public interface DraftEmployeeRepository extends JpaRepository<DraftEmployee,Long>{

	public DraftEmployee findByEmployeementId(Long employeementId);
	
	@Query("SELECT d FROM DraftEmployee d " +
		       "WHERE d.employeementId = :employeementId " +
		       "AND ( " +
		       "     (:employeeType = 'Apmosys Product Consultant' AND d.isApmosysProduct = 'true' AND d.isConsultant = 'true') " +
		       "  OR (:employeeType = 'Apmosys Product' AND d.isApmosysProduct = 'true' AND (d.isConsultant IS NULL OR d.isConsultant = 'false')) " +
		       "  OR (:employeeType = 'Apprentice' AND d.isApprenticeship = 'true') " +
		       "  OR (:employeeType = 'Consultant' AND d.isConsultant = 'true' AND (d.isApmosysProduct IS NULL OR d.isApmosysProduct = 'false')) " +
		       "  OR (:employeeType = 'Regular' AND ( " +
		       "        COALESCE(d.isApmosysProduct, 'false') = 'false' " +
		       "    AND COALESCE(d.isApprenticeship, 'false') = 'false' " +
		       "    AND COALESCE(d.isConsultant, 'false') = 'false')) " +
		       ")")
		List<DraftEmployee> findByEmployeementIdForUpdate(@Param("employeementId") Long employeementId,
		                                                  @Param("employeeType") String employeeType);


	DraftEmployee findByEmail(String email);

	List<DraftEmployee> findByMobileNo(Long employeementId);

	List<DraftEmployee> findByAadhar(Long aadhar);

	List<DraftEmployee> findByPanNumber(String panNumber);
	
	@Query(value = "FROM DraftEmployee e WHERE e.mobileNo = :mobileNo AND e.draftEmpId != :draftEmpId")
	List<DraftEmployee> findByMobileNoAndDraftEmpId(Long mobileNo, Long draftEmpId);

	@Query(value = "FROM DraftEmployee e WHERE e.aadhar = :aadhar AND e.draftEmpId != :draftEmpId")
	List<DraftEmployee> findByAadharAndDraftEmpId(Long aadhar, Long draftEmpId);

	@Query(value = "FROM DraftEmployee e WHERE e.panNumber = :panNumber AND e.draftEmpId != :draftEmpId")
	List<DraftEmployee> findByPanNumberAndDraftEmpId(String panNumber, Long draftEmpId);

	@Query(nativeQuery = true)
	List<Object[]> getAllDraftEmployees(String status);

	@Query(nativeQuery = true)
	List<Object[]> getDraftEmployeeByEmpId(Long empId);
	
//	@Query(nativeQuery = true)
//	List<Object[]> getDraftEmployeeByEmployeementId(Long employeementId);
	
	@Query(nativeQuery = true)
	List<Object[]> getDraftEmployeeByEmployeementIdForAp(Long employeementId);
	
	@Query(nativeQuery = true)
	List<Object[]> getDraftEmployeeByEmployeementIdForOthers(Long employeementId);

	@Modifying
	@Transactional
	@Query("DELETE FROM DraftEmployee e WHERE e.employeementId = :employeementId")
	void deleteAllByEmployeementId(@Param("employeementId") Long employeementId);

}
