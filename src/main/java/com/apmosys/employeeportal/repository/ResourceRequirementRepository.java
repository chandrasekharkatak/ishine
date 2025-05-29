package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ResourceRequirement;

public interface ResourceRequirementRepository extends JpaRepository<ResourceRequirement, Long> {

}
