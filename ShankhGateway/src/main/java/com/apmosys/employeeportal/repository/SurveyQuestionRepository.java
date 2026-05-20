package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.SurveyQuestion;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {


	public List<SurveyQuestion> findAllBySurveyId(Long surveyId);

	public boolean existsBySurveyId(Long surveyId);

	@Query(nativeQuery = true)
	public List<Object[]> getAnsweredSurveysByEmpId(Long empId);

	public List<SurveyQuestion> findBySurveyId(Long surveyId);

}
