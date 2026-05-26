package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TravelApprovalMatrixAppRole;
import com.apmosys.employeeportal.model.TravelApprovalMatrixAppRoleId;

public interface TravelApprovalMatrixAppRoleRepository
		extends JpaRepository<TravelApprovalMatrixAppRole, TravelApprovalMatrixAppRoleId> {

	List<TravelApprovalMatrixAppRole> findByMatrixId(Long matrixId);

	void deleteByMatrixId(Long matrixId);
}
