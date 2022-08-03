package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.EmployeeCertificate;

public interface EmployeeCertificateRepository extends JpaRepository<EmployeeCertificate, Long> {

	List<EmployeeCertificate> findByEmpId(Long empId);

	void deleteByEmpId(Long draftEmpId);

}
