package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.Survey;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

	@Query(nativeQuery = true)
	public List<Object[]> getAllSurveys();
	
    @Query(
		    "SELECT " +
		    " s.surveyId, " +
		    " s.surveyName, " +
		    " s.description, " +
		    " s.isActive, " +
		    " cb.name, " +
		    " s.createdOn, " +
		    " ub.name, " +
		    " s.updatedOn, " +
		    " s.type, " +
		    " s.createdBy, " +
		    " s.updatedBy " +
		    "FROM TrainingQuizMapping tqm " +
		    "JOIN tqm.survey s " +
		    "LEFT JOIN Employee cb ON cb.empId = s.createdBy " +
		    "LEFT JOIN Employee ub ON ub.empId = s.updatedBy " +
		    "WHERE tqm.trainingMaster.trainingId = :trainingId " +
		    "AND tqm.activeStatus = 'true'"
		)
		List<Object[]> getSurveysByTrainingId(@Param("trainingId") Integer trainingId);


}
