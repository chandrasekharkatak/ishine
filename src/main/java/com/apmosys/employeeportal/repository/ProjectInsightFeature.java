package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ProjectInsightServiceModel;

public interface ProjectInsightFeature extends JpaRepository<ProjectInsightServiceModel, Long> {
}