package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixLevel;

public interface ReimbursementApprovalMatrixLevelRepository
		extends JpaRepository<ReimbursementApprovalMatrixLevel, Long> {

	List<ReimbursementApprovalMatrixLevel> findByMatrixIdOrderByLevelOrderAsc(Long matrixId);

	void deleteByMatrixId(Long matrixId);
}
