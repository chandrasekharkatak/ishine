package com.apmosys.employeeportal.mongodb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectDetails;

public interface ProjectInsightProjectDetailsRepository
		extends MongoRepository<ProjectInsightProjectDetails, String> {

	boolean existsById(String id);

	// List<ProjectInsightProjectDetails> findByProjectIdIn(List<Integer> projectIds);

}
