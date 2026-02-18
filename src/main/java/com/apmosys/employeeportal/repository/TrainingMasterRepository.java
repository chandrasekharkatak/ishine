package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TrainingMaster;

@Repository
public interface TrainingMasterRepository extends JpaRepository<TrainingMaster, Integer> {
	
	Optional<TrainingMaster> findByTrainingId(Integer trainingId);
	
	@Query("SELECT tm FROM TrainingMaster tm \n" + 
       "Left JOIN TrainingQuizMapping tqm on tqm.trainingMaster.trainingId = tm.trainingId \n"+
       "LEFT JOIN EmployeeQuizResponseStatusMapping eqrsm ON eqrsm.quizId = tqm.survey.surveyId \n"+
       "WHERE (tqm.mappingId IS NULL OR tqm.activeStatus = 'true') AND (tm.mandatoryFlag = :mandatoryFlag AND tm.activeStatus = :activeStatus ) \n"+
       "OR (eqrsm.id IS NULL OR eqrsm.passStatus IN :passStatus)")
	List<TrainingMaster> findByMandatoryFlagAndActiveStatus(@Param("mandatoryFlag") String mandatoryFlag, 
															@Param("activeStatus") String activeStatus,
															@Param("passStatus") List<String> passStatus);

	
	@Query("SELECT tm FROM TrainingMaster tm \n" +
		   "Left JOIN TrainingQuizMapping tqm on tqm.trainingMaster.trainingId = tm.trainingId \n"+
		   "Left JOIN EmployeeQuizResponseStatusMapping eqrsm ON eqrsm.quizId = tqm.survey.surveyId \n"+ 
		   "WHERE (tqm.mappingId IS NULL OR tqm.activeStatus = 'true') AND ((tm.mandatoryFlag = :mandatoryFlag AND tm.activeStatus = :activeStatus ) \n"+
		   "OR (eqrsm.id IS NULL OR eqrsm.passStatus IN :passStatus)) \n" +
		   "AND (tm.effectiveTo IS NULL OR tm.effectiveTo >= CURRENT_DATE) " +
		   "AND tm.effectiveFrom <= CURRENT_DATE")
	List<TrainingMaster> findActiveMandatoryTrainings(@Param("mandatoryFlag") String mandatoryFlag, 
													   @Param("activeStatus") String activeStatus,
													   @Param("passStatus") List<String> passStatus);
	
	@Query("SELECT tm FROM TrainingMaster tm \n"+ 
			"Left JOIN TrainingQuizMapping tqm on tqm.trainingMaster.trainingId = tm.trainingId \n"+
			"Left JOIN EmployeeQuizResponseStatusMapping eqrsm ON eqrsm.quizId = tqm.survey.surveyId \n"+
			"WHERE tm.activeStatus = :activeStatus")
	List<TrainingMaster> findByActiveStatus(@Param("activeStatus") String activeStatus);
	
	@Query("SELECT DISTINCT tm FROM TrainingMaster tm \n" +
       "LEFT JOIN TrainingQuizMapping tqm ON tqm.trainingMaster.trainingId = tm.trainingId \n" +
       "LEFT JOIN EmployeeQuizResponseStatusMapping eqrsm ON eqrsm.quizId = tqm.survey.surveyId \n" +
       "WHERE tm.activeStatus = :activeStatus \n" +
       "AND (tm.effectiveTo IS NULL OR tm.effectiveTo >= CURRENT_DATE) \n" +
       "AND tm.effectiveFrom <= CURRENT_DATE \n" 
       +"AND (eqrsm.id IS NULL OR eqrsm.passStatus IN :passStatus)"
	)
	List<TrainingMaster> findActiveTrainingsWithEffectiveDates(
			@Param("activeStatus") String activeStatus,
			@Param("passStatus") List<String> passStatus);
	
	@Query("SELECT t FROM TrainingMaster t WHERE LOWER(t.trainingName) = LOWER(:trainingName)")
	Optional<TrainingMaster> findByTrainingNameIgnoreCase(@Param("trainingName") String trainingName);
	
	@Query("SELECT t FROM TrainingMaster t WHERE LOWER(t.trainingName) = LOWER(:trainingName) AND t.trainingId != :trainingId")
	Optional<TrainingMaster> findByTrainingNameAndNotTrainingId(@Param("trainingName") String trainingName, @Param("trainingId") Integer trainingId);
}
