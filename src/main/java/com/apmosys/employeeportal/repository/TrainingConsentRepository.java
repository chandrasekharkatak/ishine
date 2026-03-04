package com.apmosys.employeeportal.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.TrainingResponseDTO;
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
		   "AND (:contentId IS NULL OR tc.trainingContent.contentId = :contentId) " +
		   "AND ((:quizId IS NULL AND tc.quizId IS NULL) OR " +
       	   "	(:quizId IS NOT NULL AND tc.quizId = :quizId)) " +
		   "AND tc.completionCycleNumber = :cycleNumber")
	Optional<TrainingConsent> findByEmpIdAndTrainingIdAndContentIdAndQuizIdAndCycleNumber(
			@Param("empId") Long empId,
			@Param("trainingId") Integer trainingId,
			@Param("contentId") Integer contentId,
			@Param("quizId") Long quizId,
			@Param("cycleNumber") Integer cycleNumber);
	
	
	
	@Query(
		    "SELECT CASE WHEN COUNT(tc) > 0 THEN true ELSE false END " +
		    "FROM TrainingConsent tc " +
		    "WHERE tc.empId = :empId " +
		    "AND tc.trainingMaster.trainingId = :trainingId " +
			"AND ((:quizId IS NULL AND tc.quizId IS NULL) OR " +
       	   "(:quizId IS NOT NULL AND tc.quizId = :quizId)) \n"+ 
		    "AND tc.trainingContent.contentId = :contentId"
		)
		boolean existsByEmpIdAndTrainingIdAndContentIdAndQuizId(
		        @Param("empId") Long empId,
		        @Param("trainingId") Integer trainingId,
		        @Param("contentId") Integer contentId,
				@Param("quizId") Long quizId
		);

	
	@Query("SELECT COUNT(DISTINCT tc.completionCycleNumber) FROM TrainingConsent tc " +
		   "WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "AND ((:quizId IS NULL AND tc.quizId IS NULL) OR " +
       "(:quizId IS NOT NULL AND tc.quizId = :quizId)) "+
		   "AND tc.consentTimestamp >= :fromDate")
	Long countCompletionsInLast12Months(@Param("empId") Long empId,
										 @Param("trainingId") Integer trainingId,
										 @Param("quizId") Long quizId,
										 @Param("fromDate") Timestamp fromDate);
	
	@Query("SELECT MAX(tc.completionCycleNumber) FROM TrainingConsent tc " +
		   "WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId")
	Integer findMaxCycleNumber(@Param("empId") Long empId, @Param("trainingId") Integer trainingId);
	
	@Query("SELECT tc FROM TrainingConsent tc WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "AND((:quizId IS NULL AND tc.quizId IS NULL) OR " +
       "(:quizId IS NOT NULL AND tc.quizId = :quizId)) " +
		   "ORDER BY tc.consentTimestamp DESC")	
	List<TrainingConsent> findByEmpIdAndTrainingIdAndQuizId(@Param("empId") Long empId, 
																	@Param("trainingId") Integer trainingId, @Param("quizId") Long quizId);

	
	@Query("SELECT tc FROM TrainingConsent tc WHERE tc.empId = :empId " +
		   "AND tc.trainingMaster.trainingId = :trainingId " +
		   "ORDER BY tc.consentTimestamp DESC")
	List<TrainingConsent> findByEmpIdAndTrainingId(@Param("empId") Long empId, 
																	@Param("trainingId") Integer trainingId);
	@Query("select tc from TrainingConsent tc\n" +
				"where tc.trainingContent.activeStatus = 'true' and tc.trainingMaster.trainingId IN :trainingIds and tc.empId = :empId")
	List<TrainingConsent> findByEmpIdAndTrainingIdsIn(@Param("empId") Long empId, @Param("trainingIds") List<Integer> trainingId);

	@Query("select new com.apmosys.employeeportal.dto.TrainingResponseDTO(tc.trainingMaster.trainingId, tc.completionCycleNumber, e.name, MAX(tc.consentTimestamp)) from TrainingConsent tc \n"+ 
    "INNER JOIN Employee e on e.empId = tc.empId "+
    "where tc.trainingMaster.trainingId = :trainingId group by tc.empId")
    Page<TrainingResponseDTO> findByTrainingId(@Param("trainingId") Integer trainingId, Pageable pageable);
}