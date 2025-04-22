package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ProjectInsightUserContribution;

public interface ProjectInsightUserContributionRepository extends JpaRepository<ProjectInsightUserContribution, Long> {

	List<ProjectInsightUserContribution> findByEmpId(Long empId);

}
