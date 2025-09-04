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

	@Query("{ 'parentPathIds': { $in: ?0 } }")
	List<ProjectInsightQuestionDetails> findByParentPathIds(List<String> parentIds);

	@Query(value = "{ 'parentPathIds': { $in: ?0 } }", count = true)
	Long countByParentPathIds(List<String> parentIds);

	@Query(value = "{ 'parentPathIds.0': ?0 }", delete = true)
    void deleteByParentPathIds0(String parentId);

}
