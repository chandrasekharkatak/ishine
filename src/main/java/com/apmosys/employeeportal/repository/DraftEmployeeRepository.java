package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.DraftEmployee;

public interface DraftEmployeeRepository extends JpaRepository<DraftEmployee,Long>{

	DraftEmployee findByEmployeementId(Long employeementId);

	DraftEmployee findByEmail(String email);

	DraftEmployee findByMobileNo(Long employeementId);

	DraftEmployee findByAadhar(Long aadhar);

	DraftEmployee findByPanNumber(String panNumber);

}
