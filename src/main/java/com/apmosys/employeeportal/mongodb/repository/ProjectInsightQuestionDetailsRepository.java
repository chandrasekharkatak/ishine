package com.apmosys.employeeportal.mongodb.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;

public interface ProjectInsightQuestionDetailsRepository
		extends MongoRepository<ProjectInsightQuestionDetails, String> {

	boolean existsByParentIdAndParentType(String parentId, String parentType);

	List<ProjectInsightQuestionDetails> findByParentIdAndParentType(String parentId, String parentType);
	
	@Query("{ 'toAssignedEmployeeIdList': ?0, 'parentPathIds': ?1 }")
	List<ProjectInsightQuestionDetails> findByAssignedEmployeeAndParentPathId(Long empId, String projectId);
	
	@Query("{ 'parentId': ?0, 'parentType': ?1, 'toAssignedEmployeeIdList': ?2 }")
	List<ProjectInsightQuestionDetails> findQuestionsForEmployee(String parentId, String parentType, Long empId);


	void deleteByParentPathIds0(String parentId);

}
