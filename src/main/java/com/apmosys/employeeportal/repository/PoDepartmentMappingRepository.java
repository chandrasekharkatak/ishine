package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.PoDepartmentMapping;


@Repository
public interface PoDepartmentMappingRepository extends JpaRepository<PoDepartmentMapping, Long> {

	
	@Modifying
	@Query("DELETE FROM PoDepartmentMapping")
	void deleteAllRecords();
}
