package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.SurveyQuestion;
import com.apmosys.employeeportal.model.TrainingQuizMapping;

public interface TrainingQuizMappingRepository extends JpaRepository<TrainingQuizMapping, Integer> {
	
	List<TrainingQuizMapping> findByTrainingMasterTrainingId(Integer trainingId);
	
	List<TrainingQuizMapping> findByTrainingMasterTrainingIdAndActiveStatus(Integer trainingId, String activeStatus);
	
	Optional<TrainingQuizMapping> findByTrainingMasterTrainingIdAndSurveySurveyId(Integer trainingId, Long surveyId);
	
	Optional<TrainingQuizMapping> findByTrainingMasterTrainingIdAndTrainingContentContentIdAndSurveySurveyId(
			Integer trainingId, Integer contentId, Long surveyId);
	
	@Query("SELECT tqm FROM TrainingQuizMapping tqm WHERE tqm.trainingMaster.trainingId = :trainingId " +
			"AND (tqm.trainingContent.contentId = :contentId OR tqm.trainingContent IS NULL) " +
			"AND tqm.activeStatus = 'true'")
	List<TrainingQuizMapping> findActiveByTrainingAndContent(
			@Param("trainingId") Integer trainingId, 
			@Param("contentId") Integer contentId);

	@Query("SELECT sq FROM TrainingQuizMapping tqm \n" +
			"INNER JOIN Survey s ON s.surveyId = tqm.survey.surveyId \n"+
			"INNER JOIN SurveyQuestion sq on sq.surveyId = s.surveyId \n"+
			"WHERE tqm.trainingMaster.trainingId = :trainingId " +
			"AND tqm.activeStatus = 'true'")
	List<SurveyQuestion> findActiveByTraining(@Param("trainingId") Integer trainingId);

	@Query("SELECT tqm.survey.surveyId FROM TrainingQuizMapping tqm WHERE tqm.trainingMaster.trainingId = :trainingId AND tqm.activeStatus = 'true'")
	Long findActiveSurveyIdByTraining(@Param("trainingId") Integer trainingId);



	// @Modifying
	// @Transactional
	// @Query(value ="UPDATE training_quiz_mapping\n"+
	// 			"SET active_status = CASE\n"+
	// 			"                       WHEN survey_id = :surveyId THEN :activeStatus \n"+
	// 			"                       ELSE 'false'\n"+
	// 			"                   END\n"+
	// 			"WHERE active_status <> 'Completed'" , nativeQuery = true)
	// void updateAllQuizByTrainingId(@Param("surveyId") Long surveyId, @Param("activeStatus") String activeStatus);
}
