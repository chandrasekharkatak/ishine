package com.apmosys.employeeportal.mongodb.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionLibraryEntry;

public interface ProjectInsightQuestionLibraryEntryRepository
                extends MongoRepository<ProjectInsightQuestionLibraryEntry, String> {

        ProjectInsightQuestionLibraryEntry findByNormalizedQuestion(String normalizedQuestion);

        @Query(value = "{ 'normalizedQuestion': { $regex: ?0, $options: ?1 } }")
        List<ProjectInsightQuestionLibraryEntry> findByNormalizedQuestionRegex(String regex, String options);
}
