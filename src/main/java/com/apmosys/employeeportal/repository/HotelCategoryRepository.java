package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.HotelCategory;
import com.apmosys.employeeportal.model.TravelMode;
import com.apmosys.employeeportal.model.TravelReason;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelCategoryRepository extends JpaRepository<HotelCategory, Long> {
//    boolean existsByHotelCategory(String hotelCategory);
	Optional<HotelCategory> findByHotelCategory(String hotelCategory);
	
//	 List<HotelCategory> findHotelCategoryById(Long id);
	

	Optional<HotelCategory> findHotelCategoryById(String hotelCategory);
	
	Optional<HotelCategory> findById(Long id);

	List<HotelCategory> findByIsActiveOrderByHotelCategoryAsc(String isActive);

	Optional<HotelCategory> findByHotelCategoryAndIsActive(String hotelCategory, String isActive);

}
