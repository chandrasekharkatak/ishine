package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.TrainingMasterDTO;
import com.apmosys.employeeportal.model.TrainingMaster;

@Repository
public interface TrainingMasterRepository extends JpaRepository<TrainingMaster, Integer> {
	
	Optional<TrainingMaster> findByTrainingId(Integer trainingId);
	
	@Query("SELECT new com.apmosys.employeeportal.dto.TrainingMasterDTO(tm.trainingId, tm.trainingName, tm.trainingType, tm.mandatoryFlag, tm.effectiveFrom, tm.effectiveTo, tm.lockEnabled, tm.minViewTimeMinutes, tm.consentRequired, tm.skipAllowed, tm.deadlineEnabled, tm.deadlinePattern, tm.customDeadlineMonths, tm.activeStatus, tm.createdBy, e.name, tm.createdOn, tm.updatedBy, tm.updatedOn) FROM TrainingMaster tm \n" + 
       "Left JOIN TrainingQuizMapping tqm on tqm.trainingMaster.trainingId = tm.trainingId and (tqm.mappingId IS NULL OR tqm.activeStatus = 'true') \n"+
       "LEFT JOIN EmployeeQuizResponseStatusMapping eqrsm ON eqrsm.quizId = tqm.survey.surveyId and eqrsm.id IS NULL \n"+
	   "INNER JOIN Employee e ON e.empId = tm.createdBy \n"+
       "WHERE (tm.mandatoryFlag = :mandatoryFlag AND tm.activeStatus = :activeStatus )")
	List<TrainingMasterDTO> findByMandatoryFlagAndActiveStatus(@Param("mandatoryFlag") String mandatoryFlag, 
															@Param("activeStatus") String activeStatus);

	
	@Query("SELECT tm FROM TrainingMaster tm \n" +
		   "Left JOIN TrainingQuizMapping tqm on tqm.trainingMaster.trainingId = tm.trainingId and (tqm.mappingId IS NULL OR tqm.activeStatus = 'true') \n"+
		//    "Left JOIN EmployeeQuizResponseStatusMapping eqrsm ON eqrsm.quizId = tqm.survey.surveyId \n"+ 
		   "WHERE (tm.mandatoryFlag = :mandatoryFlag) \n"+
		//    "OR (eqrsm.id IS NULL )) \n" +
		   "AND tm.activeStatus = :activeStatus \n" +
		   "AND (tm.effectiveTo IS NULL OR tm.effectiveTo >= CURRENT_DATE) " +
		   "AND tm.effectiveFrom <= CURRENT_DATE")
	List<TrainingMaster> findActiveMandatoryTrainings(@Param("mandatoryFlag") String mandatoryFlag, 
													   @Param("activeStatus") String activeStatus);
	
	@Query("SELECT distinct new com.apmosys.employeeportal.dto.TrainingMasterDTO(tm.trainingId, tm.trainingName, tm.trainingType, tm.mandatoryFlag, tm.effectiveFrom, tm.effectiveTo, tm.lockEnabled, tm.minViewTimeMinutes, tm.consentRequired, tm.skipAllowed, tm.deadlineEnabled, tm.deadlinePattern, tm.customDeadlineMonths, tm.activeStatus, tm.createdBy, \n"+ 
	"e.name, \n"+
	"tm.createdOn, tm.updatedBy, tm.updatedOn) FROM TrainingMaster tm \n"+ 
			"Left JOIN TrainingQuizMapping tqm on tqm.trainingMaster.trainingId = tm.trainingId \n"+
			"Left JOIN EmployeeQuizResponseStatusMapping eqrsm ON eqrsm.quizId = tqm.survey.surveyId \n"+
			"Left JOIN Employee e ON e.empId = tm.createdBy \n"+
			"WHERE (:activeStatus IS NULL OR tm.activeStatus = :activeStatus)")
	List<TrainingMasterDTO> findByActiveStatus(@Param("activeStatus") String activeStatus);
	
	@Query("SELECT DISTINCT tm FROM TrainingMaster tm \n" +
       "LEFT JOIN TrainingQuizMapping tqm ON tqm.trainingMaster.trainingId = tm.trainingId \n" +
       "LEFT JOIN EmployeeQuizResponseStatusMapping eqrsm ON eqrsm.quizId = tqm.survey.surveyId AND (eqrsm.id IS NULL) \n" +
       "WHERE tm.activeStatus = :activeStatus \n" +
       "AND (tm.effectiveTo IS NULL OR tm.effectiveTo >= CURRENT_DATE) \n" +
       "AND tm.effectiveFrom <= CURRENT_DATE \n" 
	)
	List<TrainingMaster> findActiveTrainingsWithEffectiveDates(
			@Param("activeStatus") String activeStatus);
	
	@Query("SELECT t FROM TrainingMaster t WHERE LOWER(t.trainingName) = LOWER(:trainingName)")
	Optional<TrainingMaster> findByTrainingNameIgnoreCase(@Param("trainingName") String trainingName);
	
	@Query("SELECT t FROM TrainingMaster t WHERE LOWER(t.trainingName) = LOWER(:trainingName) AND t.trainingId != :trainingId")
	Optional<TrainingMaster> findByTrainingNameAndNotTrainingId(@Param("trainingName") String trainingName, @Param("trainingId") Integer trainingId);
}
