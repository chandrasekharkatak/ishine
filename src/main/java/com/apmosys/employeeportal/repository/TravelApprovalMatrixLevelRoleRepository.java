package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TravelApprovalMatrixLevelRole;
import com.apmosys.employeeportal.model.TravelApprovalMatrixLevelRoleId;

public interface TravelApprovalMatrixLevelRoleRepository
		extends JpaRepository<TravelApprovalMatrixLevelRole, TravelApprovalMatrixLevelRoleId> {

	List<TravelApprovalMatrixLevelRole> findByLevelId(Long levelId);

	void deleteByLevelId(Long levelId);
}
