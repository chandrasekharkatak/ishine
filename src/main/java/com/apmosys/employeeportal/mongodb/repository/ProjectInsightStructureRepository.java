package com.apmosys.employeeportal.mongodb.repository;

import java.util.*;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightStructure;

public interface ProjectInsightStructureRepository extends MongoRepository<ProjectInsightStructure, ObjectId>  {

    @Query("{ 'data.questions.projectResponseList': { $elemMatch: { $or: [ { 'tags': { $in: [?#{#fieldValueMap['tags']}] } } ] } } } }")
    List<ProjectInsightStructure> findByFieldValuesAny(@Param("fieldValueMap") Map<String, List<String>> fieldValueMap);


}  
