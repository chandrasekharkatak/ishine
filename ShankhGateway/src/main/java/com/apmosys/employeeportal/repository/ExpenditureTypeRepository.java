package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.ExpenditureType;
import com.apmosys.employeeportal.model.TravelReason;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenditureTypeRepository extends JpaRepository<ExpenditureType, Long> {
	
	Optional<ExpenditureType> findByExpenditureTypeName(String expenditureType);

}
