package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.AppraisalSummary;
import java.util.List;

public interface AppraisalSummaryRepository extends JpaRepository<AppraisalSummary,Long>{
	List<AppraisalSummary> findByEmployeeId(Long employeeId);
}
