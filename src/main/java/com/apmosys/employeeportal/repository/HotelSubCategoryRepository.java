package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.HotelSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelSubCategoryRepository extends JpaRepository<HotelSubCategory, Long> {
    // Custom queries can be added if needed
}
