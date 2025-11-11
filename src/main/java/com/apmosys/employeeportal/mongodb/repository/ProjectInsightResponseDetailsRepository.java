package com.apmosys.employeeportal.mongodb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;

public interface ProjectInsightResponseDetailsRepository
		extends MongoRepository<ProjectInsightResponseDetails, String> {
	Optional<ProjectInsightResponseDetails> findByQuesIdAndResponseBy(String quesId, Long responseBy);

	List<ProjectInsightResponseDetails> findByQuesIdInAndResponseBy(List<String> quesIds, Long empId);

	void deleteByQuesIdIn(List<String> quesIds);

	public void deleteById(String id);

	@Query("{ 'reviewerInfo': { $elemMatch: { 'reviewerid': ?0 } }, 'quesId': { $in: ?1 } }")
	List<ProjectInsightResponseDetails> findByReviewerIdAndQuesIds(Long reviewerid, List<String> quesIds);

	List<ProjectInsightResponseDetails> findByIdIn(List<String> ids);

}
