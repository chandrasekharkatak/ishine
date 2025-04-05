package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ProjectInsightResponse;

public interface ProjectInsightResponseRepository extends JpaRepository<ProjectInsightResponse, Long> {

	ProjectInsightResponse findByQuestionMasterId(Long questionMasterId);

	List<ProjectInsightResponse> findByQuestionMasterIdAndEmpId(Long questionMasterId, Long employeeId);

	List<ProjectInsightResponse> findAllByQuestionMasterId(Long questionMasterId);

	ProjectInsightResponse findByEmpIdAndQuestionMasterId(Long empId, Long questionId);
	
}
