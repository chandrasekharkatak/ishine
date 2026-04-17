package com.apmosys.employeeportal.mongodb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightFormDetails;

public interface ProjectInsightFormDetailsRepository extends MongoRepository<ProjectInsightFormDetails, String> {

	ProjectInsightFormDetails findByParentIdAndParentType(String parentId, String parentType);

	Optional<ProjectInsightFormDetails> findFormDetailsByParentIdAndParentType(String parentId, String parentType);

	List<ProjectInsightFormDetails> findByParentIdIn(List<String> parentIds);
}
