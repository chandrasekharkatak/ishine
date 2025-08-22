package com.apmosys.employeeportal.mongodb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;

public interface ProjectInsightResponseDetailsRepository extends MongoRepository<ProjectInsightResponseDetails, String>  {
	Optional<ProjectInsightResponseDetails> findByQuesIdAndResponseBy(String quesId,Long responseBy);
	
	List<ProjectInsightResponseDetails> findByQuesIdInAndResponseBy(List<String> quesIds, Long empId);

	void deleteByQuesIdIn(List<String> quesIds);

}
