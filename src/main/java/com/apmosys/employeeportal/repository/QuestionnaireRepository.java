package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
  
    List<Questionnaire> findByQuarterId(Long quarterId);
    
    @Query("SELECT q FROM Questionnaire q WHERE q.quarterId = :quarterId AND q.departmentId = :departmentId")
    List<Questionnaire> findByQuarterAndDepartment(Long quarterId, Long departmentId);
    
}


