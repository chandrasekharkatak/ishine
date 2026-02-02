package com.apmosys.employeeportal.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TrainingConsent;

@Repository
public interface TrainingConsentRepository extends JpaRepository<TrainingConsent, Long> {
	
	Optional<TrainingConsent> findByConsentId(Long consentId);
	
	@Query("SELECT tc FROM TrainingConsent tc WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "AND tc.completionCycleNumber = :cycleNumber")
	Optional<TrainingConsent> findByEmpIdAndTrainingIdAndCycleNumber(@Param("empId") Long empId,
																	  @Param("trainingId") Integer trainingId,
																	  @Param("cycleNumber") Integer cycleNumber);
	
	@Query("SELECT tc FROM TrainingConsent tc WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "AND tc.trainingContent.contentId = :contentId " +
		   "AND tc.completionCycleNumber = :cycleNumber")
	Optional<TrainingConsent> findByEmpIdAndTrainingIdAndContentIdAndCycleNumber(
			@Param("empId") Long empId,
			@Param("trainingId") Integer trainingId,
			@Param("contentId") Integer contentId,
			@Param("cycleNumber") Integer cycleNumber);
	
	@Query("SELECT COUNT(DISTINCT tc.completionCycleNumber) FROM TrainingConsent tc " +
		   "WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "AND tc.consentTimestamp >= :fromDate")
	Long countCompletionsInLast12Months(@Param("empId") Long empId,
										 @Param("trainingId") Integer trainingId,
										 @Param("fromDate") Timestamp fromDate);
	
	@Query("SELECT MAX(tc.completionCycleNumber) FROM TrainingConsent tc " +
		   "WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId")
	Integer findMaxCycleNumber(@Param("empId") Long empId, @Param("trainingId") Integer trainingId);
	
	@Query("SELECT tc FROM TrainingConsent tc WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "ORDER BY tc.consentTimestamp DESC")
	List<TrainingConsent> findByEmpIdAndTrainingId(@Param("empId") Long empId, 
													@Param("trainingId") Integer trainingId);
}
