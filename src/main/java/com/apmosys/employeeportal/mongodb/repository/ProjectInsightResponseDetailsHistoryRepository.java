package com.apmosys.employeeportal.mongodb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetailsHistory;

public interface ProjectInsightResponseDetailsHistoryRepository extends MongoRepository<ProjectInsightResponseDetailsHistory, String> {
	List<ProjectInsightResponseDetailsHistory> findByQuesIdAndResponseBy(String quesId,Long responseBy);
	
	List<ProjectInsightResponseDetailsHistory> findByQuesIdInAndResponseBy(List<String> quesIds, Long empId);
	
	ProjectInsightResponseDetailsHistory findFirstByQuesIdAndResponseByOrderByVersionDesc(String quesId, Long responseBy);
}
