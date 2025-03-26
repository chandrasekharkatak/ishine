package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.AppraisalSummary;
import java.util.List;

public interface AppraisalSummaryRepository extends JpaRepository<AppraisalSummary,Long>{
	List<AppraisalSummary> findByEmployeeId(Long employeeId);
	
	@Query(nativeQuery = true,value = "select * from Appraisal_Summary where employee_id = :empId and quarter_id = :quarterId;")
	List<AppraisalSummary> findbyEmpIdAndQuarterId(Long empId,Long quarterId);
}
