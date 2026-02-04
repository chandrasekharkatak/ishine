package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
