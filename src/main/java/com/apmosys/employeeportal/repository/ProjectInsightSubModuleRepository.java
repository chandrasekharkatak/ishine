package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ProjectInsightSubModule;

public interface ProjectInsightSubModuleRepository extends JpaRepository<ProjectInsightSubModule, Long> {

	List<ProjectInsightSubModule> getByModuleId(Long moduleId);

}
