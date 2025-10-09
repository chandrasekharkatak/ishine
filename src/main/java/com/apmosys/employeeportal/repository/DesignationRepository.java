package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.Designation;
import com.apmosys.employeeportal.model.Employee;

public interface DesignationRepository extends JpaRepository<Designation, Long> {

	@Query(nativeQuery = true)
	List<Object[]> findAllDesignation();

	Designation findByDesignationName(String designationName);

	Designation findByDesignationId(Long designationId);
	
	@Query(nativeQuery = true,value ="SELECT * FROM designation \n"
			+ "WHERE REPLACE(LOWER(designation_name), ' ', '') = REPLACE(LOWER(:name), ' ', '')")
	Designation findByDesignationNameIgnoreCase(@Param("name") String name);

}
