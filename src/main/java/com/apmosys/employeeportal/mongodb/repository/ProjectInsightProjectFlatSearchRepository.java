package com.apmosys.employeeportal.mongodb.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;


public interface ProjectInsightProjectFlatSearchRepository extends MongoRepository<ProjectInsightProjectFlatSearch, String> {

    Optional<ProjectInsightProjectFlatSearch> findByParentId(String id);

    ProjectInsightProjectFlatSearch findByFlatSearchableText(String flatSearchableText);

}