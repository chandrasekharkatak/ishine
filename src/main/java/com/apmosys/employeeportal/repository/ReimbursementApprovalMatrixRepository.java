package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementApprovalMatrix;

public interface ReimbursementApprovalMatrixRepository extends JpaRepository<ReimbursementApprovalMatrix, Long> {

	List<ReimbursementApprovalMatrix> findByIsActiveOrderByMatrixIdAsc(String isActive);

	List<ReimbursementApprovalMatrix> findAllByOrderByMatrixIdAsc();
}
