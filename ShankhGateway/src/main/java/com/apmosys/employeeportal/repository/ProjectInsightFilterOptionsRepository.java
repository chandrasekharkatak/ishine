package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ProjectInsightFilterOptions;

public interface ProjectInsightFilterOptionsRepository extends JpaRepository<ProjectInsightFilterOptions, Long> {

	List<ProjectInsightFilterOptions> findByFilterId(Long filterId);

}
