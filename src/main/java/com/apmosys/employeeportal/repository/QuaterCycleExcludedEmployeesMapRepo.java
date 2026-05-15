package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.QuaterCycleExcludedEmployeesMap;

public interface QuaterCycleExcludedEmployeesMapRepo extends JpaRepository<QuaterCycleExcludedEmployeesMap , Long>{

	List<QuaterCycleExcludedEmployeesMap> findByQuarterId(Long quarterId);
}
