package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.Kresponse;
import com.apmosys.employeeportal.model.Qresponse;

public interface KresponseRepository extends JpaRepository<Kresponse, Long> {
	
	@Query(nativeQuery=true , value ="select sum(response)/count(response) from kresponse where emp_id = :empId and quarter_id = :quarterId")
	public Float calculateByEmpIdAndQuarterId(Long empId , Long quarterId);
	
	@Query(nativeQuery = true, value = "select distinct(emp_id) from kresponse where emp_id = :empId and quarter_id = :quarterId")
	public Long  findempId(@Param("empId") Long empId,@Param("quarterId") Long quarterId);

	List<Kresponse> findByEmpId(Long empId);
	
	public List<Kresponse> findByEmpIdAndQuarterId(Long empId,Long quarterId);

	

}
