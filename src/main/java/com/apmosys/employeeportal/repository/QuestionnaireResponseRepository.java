package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.QuestionnaireResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {
    List<QuestionnaireResponse> findByEmpId(Long empId);
    List<QuestionnaireResponse> findByGoalId(Long goalId);
	List<QuestionnaireResponse> findByEmpIdAndQuarter(Long employeeId, Integer quarter);
	
	
	
   @Query(nativeQuery = true , value="SELECT q.score FROM questionnaire_response q WHERE q.emp_id = :employee_id  AND q.quarter_id = :quarter_id")
   public Float calculateTotalScoreByEmpIdAndQuarter(Long employee_id,Long quarter_id);
}