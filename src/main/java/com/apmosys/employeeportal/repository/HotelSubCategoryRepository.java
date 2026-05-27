package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.City;
import com.apmosys.employeeportal.model.HotelSubCategory;
import com.apmosys.employeeportal.model.TravelClass;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HotelSubCategoryRepository extends JpaRepository<HotelSubCategory, Long> {

	long countByHotelCategory_Id(Long hotelCategoryId);

	//City findAll(String travelModeName);
    // Custom queries can be added if needed
	
	@Query(nativeQuery = true , value = "SELECT * FROM hotel_sub_category where hotel_sub_category_name =:travelModeName")
	public Optional<List<City>> findAll(String travelModeName);
	
	@Query(nativeQuery = true , value = "SELECT * FROM hotel_sub_category where hotel_sub_category_name =:travelModeName")
	public Optional<List<HotelSubCategory>> findAllByHotelSubCategoryName(String travelModeName);

	List<HotelSubCategory> findByHotelCategory_IdAndIsActiveOrderByHotelSubCategoryNameAsc(Long hotelCategoryId, String isActive);

	List<HotelSubCategory> findByHotelCategory_HotelCategoryAndIsActiveOrderByHotelSubCategoryNameAsc(
			String hotelCategory, String isActive);

}
