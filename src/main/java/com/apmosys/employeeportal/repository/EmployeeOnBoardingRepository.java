package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.Asset;

public interface EmployeeOnBoardingRepository extends JpaRepository<Asset, Long> {

	@Query(nativeQuery = true)
	List<Object[]> getAssetListByEmpId(Long empId);

	Asset findByAssetName(String category);

}
