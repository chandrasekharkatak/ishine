package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.QuestionnaireResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {
    List<QuestionnaireResponse> findByEmpId(Long empId);
    List<QuestionnaireResponse> findByGoalId(Long goalId);
	List<QuestionnaireResponse> findByEmpIdAndQuarter(Long employeeId, Integer quarter);
	
	
}
