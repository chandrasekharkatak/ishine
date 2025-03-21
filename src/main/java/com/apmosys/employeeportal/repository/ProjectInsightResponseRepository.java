package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ProjectInsightResponse;

public interface ProjectInsightResponseRepository extends JpaRepository<ProjectInsightResponse, Long> {

	ProjectInsightResponse findByQuestionMasterId(Long questionMasterId);

}
