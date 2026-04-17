package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.FoodType;

@Repository
public interface FoodTypeRepository extends JpaRepository<FoodType, Long> {
	
	//Optional<ExpenditureType> findByExpenditureTypeName(String expenditureType);

}