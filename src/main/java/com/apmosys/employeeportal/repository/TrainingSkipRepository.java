package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TrainingSkip;

@Repository
public interface TrainingSkipRepository extends JpaRepository<TrainingSkip, Long> {
	
	Optional<TrainingSkip> findBySkipId(Long skipId);
	
	@Query("SELECT ts FROM TrainingSkip ts WHERE ts.empId = :empId " +
		   "AND ts.trainingMaster.trainingId = :trainingId " +
		   "AND ts.cycleNumber = :cycleNumber")
	Optional<TrainingSkip> findByEmpIdAndTrainingIdAndCycleNumber(@Param("empId") Long empId,
																   @Param("trainingId") Integer trainingId,
																   @Param("cycleNumber") Integer cycleNumber);
	
	@Query("SELECT ts FROM TrainingSkip ts WHERE ts.empId = :empId " +
		   "AND ts.trainingMaster.trainingId = :trainingId")
	java.util.List<TrainingSkip> findByEmpIdAndTrainingId(@Param("empId") Long empId, 
														   @Param("trainingId") Integer trainingId);
}
