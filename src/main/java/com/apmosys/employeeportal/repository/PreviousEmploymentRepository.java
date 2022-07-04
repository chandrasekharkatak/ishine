package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.PreviousEmployment;

public interface PreviousEmploymentRepository extends JpaRepository<PreviousEmployment, Long> {

}
