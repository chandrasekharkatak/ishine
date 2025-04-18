package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightMilestone;

public interface ProjectInsightMilestoneRepository extends JpaRepository<ProjectInsightMilestone, Long> {

	@Query(nativeQuery = true)
	List<Object[]> getAllProjectInsight();

	List<ProjectInsightMilestone> getByProjectId(Long projectId);

	boolean existsByProjectId(Long surveyId);

	ProjectInsightMilestone getByMilestoneId(Long milestoneId);

	@Query(value="Select pim from ProjectInsightMilestone pim "
			+ "inner join ProjectInsightAssignees pia on  pim.milestoneId = pia.entityId and pia.entityType = 'Milestone'"
			+ "where pia.assigneeId=:employeeId and pim.projectId=:projectId")
	List<ProjectInsightMilestone> findByProjectIdAndAssignedMilestone(Long projectId, Long employeeId);

	@Query(nativeQuery = true)
	List<Object[]> getAllProjectInsightByUser(Long empId);
	
	@Transactional
	@Modifying
	@Query(value = "delete from ProjectInsightMilestone pim where pim.milestoneId=:milestoneId ")
	void deleteProjectInsightMilestoneByMilestoneId(Long milestoneId);

}
