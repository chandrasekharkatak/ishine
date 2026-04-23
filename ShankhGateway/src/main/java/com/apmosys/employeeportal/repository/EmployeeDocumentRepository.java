package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.EmployeeDocument;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {

	List<EmployeeDocument> findByEmpId(Long empId);

	List<EmployeeDocument> findByEmpIdAndIsDraft(Long empId, String isDraft);

	Optional<EmployeeDocument> findByEmployeeDocumentIdAndIsDraft(Long employeeDocumentId, String isDraft);

}
