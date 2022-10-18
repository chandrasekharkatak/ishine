package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.SurveyEmployeeResponse;

public interface SurveyEmployeeResponseRepository extends JpaRepository<SurveyEmployeeResponse, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getSurveyResponseByEmpIdAndSurveyId(Long empId, Long surveyId);

	@Query(nativeQuery = true)
	public List<Object[]> getSurveyAllResponsesBySurveyId(Long surveyId);

}
