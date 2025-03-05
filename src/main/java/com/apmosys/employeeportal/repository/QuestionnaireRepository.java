package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
  
    List<Questionnaire> findByQuarterId(Long quarterId);
}


