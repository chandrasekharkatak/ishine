package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.EmployeeRewardForHomeDTO;

import com.apmosys.employeeportal.model.EmployeeRewards;

@Repository
public interface EmployeeRewardsRepository extends JpaRepository <EmployeeRewards, Long>  {
	
	@Query(nativeQuery = true)
	 List<List<Object>> fetchEmployeesForHomepage();
	
}