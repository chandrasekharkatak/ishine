package com.apmosys.employeeportal.mongodb.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightFormDetails;

public interface ProjectInsightFormDetailsRepository extends MongoRepository<ProjectInsightFormDetails, String> {

	Optional<ProjectInsightFormDetails> findByParentIdAndParentType(String parentId, String parentType);

}
