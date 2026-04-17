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
    
    @Query(nativeQuery = true,value="SELECT * FROM db_emp_portal.travel_mode tm inner join travel_reason tr on tm.travel_reason_id = tr.id ")
    List<TravelMode> findAllData();
    
    @Query(nativeQuery = true,value="SELECT * FROM db_emp_portal.travel_mode where travel_mode_id = :modeId ")
	TravelMode findByModeId(Long modeId);

    


}
