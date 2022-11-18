package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.EmployeeAssetMap;

public interface EmployeeOnBoardingMapRepository extends JpaRepository<EmployeeAssetMap, Long>{

	EmployeeAssetMap findByAssetId(Long onBoardingId);

}
