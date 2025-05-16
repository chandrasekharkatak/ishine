package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TravelBasedReimbursementRequest;


@Repository
public interface TravelBasedReimbursementRequestRepository extends JpaRepository<TravelBasedReimbursementRequest, Integer> {
  
	
}
