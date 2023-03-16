package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.Designation;

public interface DesignationRepository extends JpaRepository<Designation, Long> {

	@Query(nativeQuery = true)
	List<Object[]> findAllDesignation();

	Designation findByDesignationName(String designationName);

	Designation findByDesignationId(Long designationId);

}
