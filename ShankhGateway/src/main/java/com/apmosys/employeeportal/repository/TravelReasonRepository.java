package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.TravelReason;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelReasonRepository extends JpaRepository<TravelReason, Long> {
	
	Optional<TravelReason> findByTravelReasonName(String travelReasonName);


    
}
