package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementTravelMode;

public interface ReimbursementTravelModeRepository extends JpaRepository<ReimbursementTravelMode, Long> {

	long countByExpenditureType_Id(Long expenditureTypeId);

	List<ReimbursementTravelMode> findByExpenditureType_IdOrderByModeTypeAsc(Long expenditureTypeId);

	boolean existsByExpenditureType_IdAndModeTypeIgnoreCase(Long expenditureTypeId, String modeType);

	boolean existsByExpenditureType_IdAndModeTypeIgnoreCaseAndTravelModeIdNot(Long expenditureTypeId,
			String modeType, Long travelModeId);
}
