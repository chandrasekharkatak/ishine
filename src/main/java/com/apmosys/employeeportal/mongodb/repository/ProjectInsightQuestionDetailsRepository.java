package com.apmosys.employeeportal.mongodb.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;

public interface ProjectInsightQuestionDetailsRepository
		extends MongoRepository<ProjectInsightQuestionDetails, String> {

	boolean existsByParentIdAndParentType(String parentId, String parentType);

	List<ProjectInsightQuestionDetails> findByParentIdAndParentType(String parentId, String parentType);

}
