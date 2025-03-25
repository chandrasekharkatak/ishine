package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.apmosys.employeeportal.model.Kresponse;

public interface KresponseRepository extends JpaRepository<Kresponse, Long> {
	
	@Query(nativeQuery=true , value ="select sum(response)/count(response) from kresponse where emp_id = :empId and quarter_id = :quarterId;")
	public float calculateByEmpIdAndQuarterId(Long empId , Long quarterId);

	

}
