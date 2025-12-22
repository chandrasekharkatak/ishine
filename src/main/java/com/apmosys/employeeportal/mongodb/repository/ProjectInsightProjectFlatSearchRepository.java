package com.apmosys.employeeportal.mongodb.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;

@Repository
public interface ProjectInsightProjectFlatSearchRepository extends MongoRepository<ProjectInsightProjectFlatSearch, String> {

    Optional<ProjectInsightProjectFlatSearch> findByParentId(String id);

    ProjectInsightProjectFlatSearch findByFlatSearchableText(String flatSearchableText);

    @Query(value = "{ 'parentIds.0': ?0 }", delete = true)
    void deleteByParentIds0(String parentId);
}