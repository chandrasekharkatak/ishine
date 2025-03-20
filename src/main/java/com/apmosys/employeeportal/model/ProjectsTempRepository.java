package com.apmosys.employeeportal.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectsTempRepository extends JpaRepository<ProjectsTemp, Integer> {
	public ProjectsTemp findByPoProjectId(Long poProjectId);
}
