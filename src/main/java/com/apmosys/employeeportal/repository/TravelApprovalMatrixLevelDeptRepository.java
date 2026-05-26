package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TravelApprovalMatrixLevelDept;
import com.apmosys.employeeportal.model.TravelApprovalMatrixLevelDeptId;

public interface TravelApprovalMatrixLevelDeptRepository
		extends JpaRepository<TravelApprovalMatrixLevelDept, TravelApprovalMatrixLevelDeptId> {

	List<TravelApprovalMatrixLevelDept> findByLevelId(Long levelId);

	void deleteByLevelId(Long levelId);
}
