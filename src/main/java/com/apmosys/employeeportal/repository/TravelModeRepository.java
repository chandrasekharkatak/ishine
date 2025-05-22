package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.TravelMode;
import com.apmosys.employeeportal.model.TravelReason;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelModeRepository extends JpaRepository<TravelMode, Long> {
	
//	Optional<TravelMode> findByTravelModeName(String travelModeName);

}
