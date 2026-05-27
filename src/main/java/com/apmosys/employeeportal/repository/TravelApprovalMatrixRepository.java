package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TravelApprovalMatrix;

public interface TravelApprovalMatrixRepository extends JpaRepository<TravelApprovalMatrix, Long> {

	List<TravelApprovalMatrix> findByIsActiveOrderByMatrixIdAsc(String isActive);

	List<TravelApprovalMatrix> findAllByOrderByMatrixIdAsc();
}
