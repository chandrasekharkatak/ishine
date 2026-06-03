package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixLevelEmp;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixLevelEmpId;

@Repository
public interface ReimbursementApprovalMatrixLevelEmpRepository
		extends JpaRepository<ReimbursementApprovalMatrixLevelEmp, ReimbursementApprovalMatrixLevelEmpId> {

	void deleteByLevelId(Long levelId);

	List<ReimbursementApprovalMatrixLevelEmp> findByLevelId(Long levelId);
}

