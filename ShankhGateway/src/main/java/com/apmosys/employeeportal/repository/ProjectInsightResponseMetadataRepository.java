package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.ProjectInsightResponseMetadata;

@Repository
public interface ProjectInsightResponseMetadataRepository extends JpaRepository<ProjectInsightResponseMetadata, Long> {

	ProjectInsightResponseMetadata findProjectInsightResponseMetadataByResponseByAndProjectId(Long responseBy, Long projectId);

	ProjectInsightResponseMetadata findByResponseByAndProjectId(Long responseBy, Long projectId);
}
