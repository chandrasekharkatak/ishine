package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.Question;

//import com.apmosys.employeeportal.model.Kpi;

public interface QuestionRepository extends JpaRepository<Question, Long> {
	
	
}
