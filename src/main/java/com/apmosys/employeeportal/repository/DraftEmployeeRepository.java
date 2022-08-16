package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;

public interface DraftEmployeeRepository extends JpaRepository<DraftEmployee,Long>{

	DraftEmployee findByEmployeementId(Long employeementId);

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

}
