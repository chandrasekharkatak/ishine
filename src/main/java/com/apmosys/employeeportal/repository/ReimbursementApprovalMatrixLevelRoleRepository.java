package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixLevelRole;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixLevelRoleId;

public interface ReimbursementApprovalMatrixLevelRoleRepository
		extends JpaRepository<ReimbursementApprovalMatrixLevelRole, ReimbursementApprovalMatrixLevelRoleId> {

	List<ReimbursementApprovalMatrixLevelRole> findByLevelId(Long levelId);

	void deleteByLevelId(Long levelId);
}
