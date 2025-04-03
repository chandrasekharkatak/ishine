package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ProjectInsightAssignees;

public interface ProjectInsightAssigneesRepository extends JpaRepository<ProjectInsightAssignees, Long> {

	List<ProjectInsightAssignees> getByEntityIdAndEntityType(Long milestoneId, String string);

}
