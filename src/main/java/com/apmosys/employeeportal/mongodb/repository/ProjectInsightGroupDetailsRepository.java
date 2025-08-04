package com.apmosys.employeeportal.mongodb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;

public interface ProjectInsightGroupDetailsRepository extends MongoRepository<ProjectInsightGroupDetails, String> {

	boolean existsById(String id);
}
