package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.TravelMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TravelModeRepository extends JpaRepository<TravelMode, Long> {
	
	
    Optional<TravelMode> findByModeType(String modeType); 
    
//    @Query(nativeQuery = true,value="SELECT * FROM db_emp_portal.travel_mode where  ")
//    List<TravelMode> findByTravelMode(String modeType);

    List<TravelMode> findByTravelReasonId(Long travelReasonId);

    long countByTravelReason_Id(Long travelReasonId);

    @Query("SELECT tm FROM TravelMode tm LEFT JOIN FETCH tm.travelReason ORDER BY tm.travelModeId")
    List<TravelMode> findAllWithReason();

    


}
