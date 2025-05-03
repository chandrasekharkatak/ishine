package com.apmosys.employeeportal.mongodb.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.apmosys.employeeportal.mongodb.modal.FileStorage;

public interface FileMongoRepository extends MongoRepository<FileStorage, String> {

	@Query(value = "{ 'extractedText': { $in: ?0 } }", 
	           fields = "{ 'entityId': 1, 'entityType': 1, 'projectId': 1, '_id': 0 }")
	    List<FileStorage> findByTagsIn(List<String> tags);
	
}
