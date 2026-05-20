package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.TravelClass;
import com.apmosys.employeeportal.model.TravelMode;

public interface TravelClassRepository extends JpaRepository<TravelClass, Long> {

	long countByTravelMode_TravelModeId(Long travelModeId);

	long countByTravelReason_Id(Long travelReasonId);
	
//	Optional<TravelClass> findByTravelReasonName(String travelMode);
	
  //  List<TravelClass> findByTravelModeId(Long travelModeId);
    
	@Query(nativeQuery = true , value = "SELECT * FROM travel_class where travel_mode_id =:travelModeId")
	public Optional<List<TravelClass>> findByTravelModeId(Long travelModeId);

	List<TravelClass> findAllByTravelMode_TravelModeId(Long travelModeId);

}
