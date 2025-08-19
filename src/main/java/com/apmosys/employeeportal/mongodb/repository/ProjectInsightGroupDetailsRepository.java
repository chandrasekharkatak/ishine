package com.apmosys.employeeportal.mongodb.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;

public interface ProjectInsightGroupDetailsRepository extends MongoRepository<ProjectInsightGroupDetails, String> {

    boolean existsById(String id);

    List<ProjectInsightGroupDetails> findByParentIdAndParentType(String parentId, String parentType);

    boolean existsByGroupTitleAndParentIdAndParentType(String groupTitle, String parentId, String parentType);

    @Query(value = "{ 'parentId': ?0, 'parentType':?1 }", fields = "{'_id':1,'groupTitle': 1,'parentId':1,'parentType':1 }")
    List<ProjectInsightGroupDetails> getAllProjectInsightGroupsByParentId(String parentId, String parentType);

    void deleteByParentPathIds0(String parentId);
}
