package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.City;
import com.apmosys.employeeportal.model.TravelClass;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
	
	@Query(nativeQuery = true , value = "SELECT * FROM city where hotel_sub_category_id =:travelModeId")
	public Optional<List<City>> findBySubCategoryId(Long travelModeId);
	
    @Query(value = "SELECT * FROM city WHERE hotel_sub_category_id IN (:subCategoryIds)", nativeQuery = true)
    List<City> findBySubCategoryIds(List<Long> subCategoryIds);
}
