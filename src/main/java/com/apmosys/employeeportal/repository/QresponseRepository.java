package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.Qresponse;
import com.apmosys.employeeportal.model.QuestionnaireResponse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QresponseRepository extends JpaRepository<Qresponse, Long> {
	
	@Query(nativeQuery=true,value = "select  sum(response)/count(response) from qresponse where emp_id = :empId and quarter_id = :quarterId")
	public float calculateByEmpIdAndQuarterId(Long empId,Long quarterId);
	
	@Query(nativeQuery = true, value = "select distinct(emp_id) from kresponse where emp_id = :empId and quarter_id = :quarterId")
	public Long  findempId(@Param("empId") Long empId,@Param("quarterId") Long quarterId);
	
	
	List<Qresponse> findByEmpId(Long empId);
}
