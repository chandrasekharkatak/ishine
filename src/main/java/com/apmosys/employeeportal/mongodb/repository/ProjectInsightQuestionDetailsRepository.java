package com.apmosys.employeeportal.mongodb.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;

public interface ProjectInsightQuestionDetailsRepository
		extends MongoRepository<ProjectInsightQuestionDetails, String> {

	boolean existsByParentIdAndParentType(String parentId, String parentType);
	
}
