package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.PoRequirementMapping;



@Repository
public interface PoRequirementMappingRepository extends JpaRepository<PoRequirementMapping, Long> {

	
	
	
	@Modifying
	@Query("DELETE FROM PoRequirementMapping")
	void deleteAllRecords();
}
