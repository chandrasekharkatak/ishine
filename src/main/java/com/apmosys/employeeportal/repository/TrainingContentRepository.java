package com.apmosys.employeeportal.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TrainingContent;

@Repository
public interface TrainingContentRepository extends JpaRepository<TrainingContent, Integer> {
	
	Optional<TrainingContent> findByContentId(Integer contentId);
	
	List<TrainingContent> findByTrainingMaster_TrainingId(Integer trainingId);
	
	@Query("SELECT tc FROM TrainingContent tc WHERE tc.trainingMaster.trainingId = :trainingId " +
		   "AND tc.activeStatus = 'true' " +
		   "AND tc.effectiveFrom <= :currentDate " +
		   "AND (tc.effectiveTo IS NULL OR tc.effectiveTo >= :currentDate) " +
		   "ORDER BY tc.effectiveFrom DESC")
	List<TrainingContent> findActiveContentByTrainingId(@Param("trainingId") Integer trainingId, 
														 @Param("currentDate") Date currentDate);
	
	@Query("SELECT tc FROM TrainingContent tc WHERE tc.trainingMaster.trainingId = :trainingId " +
		   "AND tc.activeStatus = 'true' " +
		   "AND tc.effectiveFrom <= CURRENT_DATE " +
		   "AND (tc.effectiveTo IS NULL OR tc.effectiveTo >= CURRENT_DATE) " +
		   "ORDER BY tc.effectiveFrom DESC")
	Optional<TrainingContent> findCurrentActiveContent(@Param("trainingId") Integer trainingId);
	
	@Query("SELECT tc FROM TrainingContent tc WHERE tc.trainingMaster.trainingId = :trainingId " +
		   "AND tc.activeStatus = 'true' " +
		   "AND ((tc.effectiveFrom <= :effectiveTo AND (tc.effectiveTo IS NULL OR tc.effectiveTo >= :effectiveFrom)) " +
		   "OR (tc.effectiveTo IS NULL AND tc.effectiveFrom <= :effectiveTo))")
	List<TrainingContent> findOverlappingActiveContent(@Param("trainingId") Integer trainingId,
														@Param("effectiveFrom") Date effectiveFrom,
														@Param("effectiveTo") Date effectiveTo);
}
