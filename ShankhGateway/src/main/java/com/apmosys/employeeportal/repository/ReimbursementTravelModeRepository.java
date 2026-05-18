package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.ReimbursementTravelMode;
import com.apmosys.employeeportal.model.TravelMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReimbursementTravelModeRepository extends JpaRepository<ReimbursementTravelMode, Long> {
   // Optional<TravelMode> findByModeType(String modeType); 
    
   // List<TravelMode> findByTravelReasonId(Long travelReasonId);

}
