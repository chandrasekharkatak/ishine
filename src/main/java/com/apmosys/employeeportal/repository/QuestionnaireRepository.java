package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Optional;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
  
    List<Questionnaire> findByQuarterId(Long quarterId);
    
    @Query("SELECT q FROM Questionnaire q WHERE q.departmentId = :departmentId AND q.quarterId = :quarterId")
    List<Questionnaire> findByQuarterAndDepartment(@Param("departmentId") Long departmentId, @Param("quarterId") Long quarterId);

	List<Questionnaire> findByDepartmentName(String departmentName);

	List<Questionnaire> findByDepartmentAndQuarterId(Long departmentID, Long quarterId);
    
}


