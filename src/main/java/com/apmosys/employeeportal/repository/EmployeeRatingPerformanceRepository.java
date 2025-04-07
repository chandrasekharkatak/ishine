package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.apmosys.employeeportal.model.EmployeeRatingPerformance;

@Repository
public interface EmployeeRatingPerformanceRepository extends JpaRepository<EmployeeRatingPerformance, Long> {

}
