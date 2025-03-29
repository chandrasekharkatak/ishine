package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ProjectInsightModule;

public interface ProjectInsightModuleRepository extends JpaRepository<ProjectInsightModule, Long> {

	List<ProjectInsightModule> getByMilestoneId(Long milestoneId);

}
