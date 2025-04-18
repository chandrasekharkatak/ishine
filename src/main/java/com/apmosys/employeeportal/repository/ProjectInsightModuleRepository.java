package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightModule;

public interface ProjectInsightModuleRepository extends JpaRepository<ProjectInsightModule, Long> {

	List<ProjectInsightModule> getByMilestoneId(Long milestoneId);

	List<ProjectInsightModule> findByMilestoneId(Long milestoneId);
	
	@Query(value="Select pim from ProjectInsightModule pim "
			+ "inner join ProjectInsightAssignees pia on  pim.moduleId = pia.entityId and pia.entityType = 'Module'"
			+ "where pia.assigneeId=:employeeId and pim.milestoneId=:milestoneId")
	List<ProjectInsightModule> findByMilestoneIdAndAssignedModule(Long milestoneId, Long employeeId);

	ProjectInsightModule getByModuleId(Long moduleId);
	
	@Transactional
	@Modifying
	@Query(value = "delete from ProjectInsightModule pim where pim.moduleId=:moduleId ")
	void deleteProjectInsightModuleByModuleId(Long moduleId);

}
