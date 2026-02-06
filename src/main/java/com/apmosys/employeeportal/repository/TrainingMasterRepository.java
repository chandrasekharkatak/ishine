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
	
	List<TrainingMaster> findByMandatoryFlagAndActiveStatus(String mandatoryFlag, String activeStatus);
	
	@Query("SELECT tm FROM TrainingMaster tm WHERE tm.mandatoryFlag = :mandatoryFlag AND tm.activeStatus = :activeStatus " +
		   "AND (tm.effectiveTo IS NULL OR tm.effectiveTo >= CURRENT_DATE) " +
		   "AND tm.effectiveFrom <= CURRENT_DATE")
	List<TrainingMaster> findActiveMandatoryTrainings(@Param("mandatoryFlag") String mandatoryFlag, 
													   @Param("activeStatus") String activeStatus);
	
	@Query("SELECT tm FROM TrainingMaster tm WHERE tm.activeStatus = :activeStatus")
	List<TrainingMaster> findByActiveStatus(@Param("activeStatus") String activeStatus);
	
	@Query("SELECT tm FROM TrainingMaster tm WHERE tm.activeStatus = :activeStatus " +
		   "AND (tm.effectiveTo IS NULL OR tm.effectiveTo >= CURRENT_DATE) " +
		   "AND tm.effectiveFrom <= CURRENT_DATE")
	List<TrainingMaster> findActiveTrainingsWithEffectiveDates(@Param("activeStatus") String activeStatus);
	
	
	@Query("SELECT t FROM TrainingMaster t WHERE LOWER(t.trainingName) = LOWER(:trainingName)")
	Optional<TrainingMaster> findByTrainingNameIgnoreCase(@Param("trainingName") String trainingName);
	
	@Query("SELECT t FROM TrainingMaster t WHERE LOWER(t.trainingName) = LOWER(:trainingName) AND tm.trainingId != :trainingId")
	Optional<TrainingMaster> findByTrainingNameAndNotTrainingId(@Param("trainingName") String trainingName, @Param("trainingId") Integer trainingId);
}
