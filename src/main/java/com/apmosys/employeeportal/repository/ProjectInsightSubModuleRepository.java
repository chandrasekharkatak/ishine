package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.model.ProjectInsightModule;
import com.apmosys.employeeportal.model.ProjectInsightSubModule;

public interface ProjectInsightSubModuleRepository extends JpaRepository<ProjectInsightSubModule, Long> {

	List<ProjectInsightSubModule> getByModuleId(Long moduleId);

	List<ProjectInsightSubModule> getByModuleIdAndSubModuleType(Long moduleId,String subModuleType);
	
	List<ProjectInsightSubModule> findByModuleId(Long moduleId);

	@Query(value="Select pim from ProjectInsightSubModule pim "
			+ "inner join ProjectInsightAssignees  pia on  pim.submoduleId = pia.entityId and pia.entityType = 'SubModule'"
			+ "where pia.assigneeId=:employeeId and pim.moduleId=:moduleId")
	List<ProjectInsightSubModule> findByModuleIdAndAssignedSubModule(Long moduleId, Long employeeId);

	@Transactional
	@Modifying
	@Query(value="delete from ProjectInsightSubModule pim where pim.submoduleId=:submoduleId  and pim.subModuleType=:subModuleType  ")
	void deleteBySubModuleIdAndSubModuleType(Long submoduleId, String subModuleType);
	
}
