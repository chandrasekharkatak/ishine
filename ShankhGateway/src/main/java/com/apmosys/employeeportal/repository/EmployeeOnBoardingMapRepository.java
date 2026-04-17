package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.EmployeeAssetMap;

public interface EmployeeOnBoardingMapRepository extends JpaRepository<EmployeeAssetMap, Long>{

	@Query(nativeQuery = true)
	List<Object[]> findAssetByAssetNameAndEmpId(String category, Long empId);

	EmployeeAssetMap findByAssetIdAndEmpId(Long assetId, Long empId);

	@Query(nativeQuery = true)
	List<Object[]> getExitAssetDetailsByEmployeementId(Long employeementId);

	List<EmployeeAssetMap> findByEmpId(Long empId);
}
