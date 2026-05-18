package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixAppRole;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixAppRoleId;

public interface ReimbursementApprovalMatrixAppRoleRepository
		extends JpaRepository<ReimbursementApprovalMatrixAppRole, ReimbursementApprovalMatrixAppRoleId> {

	List<ReimbursementApprovalMatrixAppRole> findByMatrixId(Long matrixId);

	void deleteByMatrixId(Long matrixId);
}
