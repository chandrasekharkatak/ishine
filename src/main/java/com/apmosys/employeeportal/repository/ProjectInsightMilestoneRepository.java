package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightMilestone;

public interface ProjectInsightMilestoneRepository extends JpaRepository<ProjectInsightMilestone, Long> {

	@Query(nativeQuery = true)
	List<Object[]> getAllProjectInsight();

	List<ProjectInsightMilestone> getByProjectId(Long projectId);

	boolean existsByProjectId(Long surveyId);

	ProjectInsightMilestone getByMilestoneId(Long milestoneId);

}
