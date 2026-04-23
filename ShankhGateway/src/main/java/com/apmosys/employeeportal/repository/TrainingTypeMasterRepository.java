package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TrainingTypeMaster;

@Repository
public interface TrainingTypeMasterRepository extends JpaRepository<TrainingTypeMaster, Long> {

	List<TrainingTypeMaster> findByIsActiveTrueOrderByTrainingTypeAsc();
	
	List<TrainingTypeMaster> findByIsActiveOrderByTrainingTypeAsc(Boolean isActive);
	
	@Query("SELECT COUNT(t) > 0 FROM TrainingTypeMaster t WHERE LOWER(t.trainingType) = LOWER(:trainingType)")
    boolean existsByTrainingTypeIgnoreCase(@Param("trainingType") String trainingType);
    
}


