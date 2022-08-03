package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.PreviousEmployment;

public interface PreviousEmploymentRepository extends JpaRepository<PreviousEmployment, Long> {

	List<PreviousEmployment> findByEmpId(Long empId);

	void deleteByEmpId(Long draftEmpId);

}
