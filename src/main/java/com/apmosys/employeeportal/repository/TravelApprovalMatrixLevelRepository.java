package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TravelApprovalMatrixLevel;

public interface TravelApprovalMatrixLevelRepository
		extends JpaRepository<TravelApprovalMatrixLevel, Long> {

	List<TravelApprovalMatrixLevel> findByMatrixIdOrderByLevelOrderAsc(Long matrixId);

	void deleteByMatrixId(Long matrixId);
}
