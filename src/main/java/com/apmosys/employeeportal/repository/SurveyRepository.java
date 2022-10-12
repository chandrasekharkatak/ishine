package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.Survey;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getAllSurveys();

}
