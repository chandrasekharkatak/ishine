package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
		    " CASE \n"+
			" WHEN tqm.activeStatus = 'false' THEN 'false' \n"+
			" WHEN tqm.activeStatus = 'true' THEN 'true' \n"+
			" WHEN tqm.activeStatus = 'Completed' THEN 'Completed' \n"+
			" END as is_active, \n"+
		    " cb.name, " +
		    " s.createdOn, " +
		    " ub.name, " +
		    " s.updatedOn, " +
		    " s.type, " +
		    " s.createdBy, " +
		    " s.updatedBy, s.cutOffQuestions " +
		    "FROM TrainingQuizMapping tqm " +
		    "JOIN tqm.survey s " +
		    "LEFT JOIN Employee cb ON cb.empId = s.createdBy " +
		    "LEFT JOIN Employee ub ON ub.empId = s.updatedBy " +
		    "WHERE tqm.trainingMaster.trainingId = :trainingId " +
		    "AND tqm.activeStatus IN ('true','Completed','false')"
		)
		List<Object[]> getSurveysByTrainingId(@Param("trainingId") Integer trainingId);

	@Modifying
	@Transactional
	@Query(value ="UPDATE surveys s\n" + 
				"INNER JOIN training_quiz_mapping tqm \n" + 
				"    ON tqm.survey_id = s.survey_id\n" + 
				"SET \n" + 
				"    s.is_active = CASE\n" + 
				"                     WHEN :activeStatus = 'true' \n" +
				"                          THEN CASE \n" + 
				"                                   WHEN tqm.survey_id = :surveyId THEN 'true'\n" +
				"                                   ELSE 'false'\n" +
				"                               END\n" +
				"                     ELSE\n" + 
				"                          CASE \n" + 
				"                              WHEN tqm.survey_id = :surveyId THEN :activeStatus\n" + 
				"                              ELSE s.is_active\n" + 
				"                          END\n" + 
				"                  END,\n" + 
				"    tqm.active_status = CASE\n" + 
				"                           WHEN :activeStatus = 'true' \n" +
				"                                THEN CASE \n" + 
				"                                         WHEN tqm.survey_id = :surveyId THEN 'true'\n" +
				"                                         ELSE 'false'\n" + 
				"                                     END\n" + 
				"                           ELSE\n" + 
				"                                CASE \n" + 
				"                                    WHEN tqm.survey_id = :surveyId THEN :activeStatus\n" + 
				"                                    ELSE tqm.active_status\n" + 
				"                                END\n" + 
				"                        END\n" +
				"WHERE tqm.training_id = :trainingId and tqm.active_status <> 'Completed' and s.is_active <> 'Completed'", nativeQuery = true)
	void updateAllQuizByTrainingId(@Param("trainingId") Integer trainingId, @Param("surveyId") Long surveyId, @Param("activeStatus") String activeStatus );

	// @Modifying
	// @Transactional
	// @Query(value = "update surveys s inner join training_quiz_mapping tqm on tqm.survey_id = s.survey_id\n" +
	// 			"set s.is_active = :activeStatus where tqm.training_id = :trainingId and tqm.survey_id = :surveyId", nativeQuery = true)
	// void updateSingleQuizByTrainingIdAndSurveyId(@Param("trainingId") Integer trainingId, @Param("surveyId") Long surveyId, @Param("activeStatus") String activeStatus );

}
