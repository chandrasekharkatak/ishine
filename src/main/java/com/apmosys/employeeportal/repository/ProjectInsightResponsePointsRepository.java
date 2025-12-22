package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ProjectInsightResponsePoints;

public interface ProjectInsightResponsePointsRepository extends  JpaRepository<ProjectInsightResponsePoints, Long> {

	ProjectInsightResponsePoints findByResponseIdAndPointsBy(Long projectInsightResponseId, Long pointsBy);
	
	List<ProjectInsightResponsePoints> findAllProjectInsightResponsePointsByResponseId(Long responseId);
	
}
