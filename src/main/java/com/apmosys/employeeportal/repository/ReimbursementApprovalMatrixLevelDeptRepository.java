package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixLevelDept;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixLevelDeptId;

public interface ReimbursementApprovalMatrixLevelDeptRepository
		extends JpaRepository<ReimbursementApprovalMatrixLevelDept, ReimbursementApprovalMatrixLevelDeptId> {

	List<ReimbursementApprovalMatrixLevelDept> findByLevelId(Long levelId);

	void deleteByLevelId(Long levelId);
}
