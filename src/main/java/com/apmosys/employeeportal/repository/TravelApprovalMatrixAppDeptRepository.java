package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TravelApprovalMatrixAppDept;
import com.apmosys.employeeportal.model.TravelApprovalMatrixAppDeptId;

public interface TravelApprovalMatrixAppDeptRepository
		extends JpaRepository<TravelApprovalMatrixAppDept, TravelApprovalMatrixAppDeptId> {

	List<TravelApprovalMatrixAppDept> findByMatrixId(Long matrixId);

	void deleteByMatrixId(Long matrixId);
}
