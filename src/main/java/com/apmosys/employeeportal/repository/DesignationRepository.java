package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.EmpIdAndNameDTO;
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

	@Query(
		"SELECT new com.apmosys.employeeportal.dto.EmpIdAndNameDTO( \n" +
		"   e.empId, \n" +
		"   e.name \n" +
		") \n" +
		"FROM Employee e \n" +
		"WHERE e.designationId IN :designationIds \n" +
		"AND e.employmentstatus <> 'InActive' \n" +
		"AND e.empId NOT IN (1, 2, 3, 4, 5, 6) \n" +
		"AND ( \n" +
		"   :searchName IS NULL \n" +
		"   OR :searchName = '' \n" +
		"   OR LOWER(e.name) LIKE LOWER(CONCAT('%', :searchName, '%')) \n" +
		")"
	)
	Page<EmpIdAndNameDTO> findEmployeesByDesignationIds(
			@Param("searchName") String searchName,
			@Param("designationIds") List<Long> designationIds,
			Pageable pageable
	);

}
