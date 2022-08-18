package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.EmployeeCertificate;

public interface EmployeeCertificateRepository extends JpaRepository<EmployeeCertificate, Long> {

	List<EmployeeCertificate> findByEmpId(Long empId);

	List<EmployeeCertificate> findByEmpIdAndIsDraft(Long empId, String isDraft);

}
