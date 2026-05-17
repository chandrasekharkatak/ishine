package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixAppDept;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixAppDeptId;

public interface ReimbursementApprovalMatrixAppDeptRepository
		extends JpaRepository<ReimbursementApprovalMatrixAppDept, ReimbursementApprovalMatrixAppDeptId> {

	List<ReimbursementApprovalMatrixAppDept> findByMatrixId(Long matrixId);

	void deleteByMatrixId(Long matrixId);
}
