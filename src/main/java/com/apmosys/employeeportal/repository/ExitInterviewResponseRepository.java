package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import com.apmosys.employeeportal.model.ExitInterviewResponse;

public interface ExitInterviewResponseRepository extends JpaRepository<ExitInterviewResponse, Long> {

	@Query(nativeQuery = true)
	List<Object[]> getAnsweredInterviewByEmpId(Long empId);

	@Query(nativeQuery = true)
	List<Object[]> getExitInterviewResponseBySurveyIdAndEmp(Long empId, Long surveyId);

	List<ExitInterviewResponse> findBySurveyIdAndEmpId(Long empId, Long surveyId);

}
