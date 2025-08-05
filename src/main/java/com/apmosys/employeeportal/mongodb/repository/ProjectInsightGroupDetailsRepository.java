package com.apmosys.employeeportal.mongodb.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;

public interface ProjectInsightGroupDetailsRepository extends MongoRepository<ProjectInsightGroupDetails, String> {

	boolean existsById(String id);

    List<ProjectInsightGroupDetails> findByParentIdAndParentType(String parentId, String parentType);

    
    boolean existsByGroupTitleAndParentIdAndParentType(String groupTitle,String parentId, String parentType);
}
