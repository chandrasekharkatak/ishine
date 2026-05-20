package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.GrievanceDocument;

public interface GrievanceDocumentRepository extends JpaRepository<GrievanceDocument, Long> {

	List<GrievanceDocument> findByTicketIdAndIsActiveOrderBySortOrderAscDocumentIdAsc(Long ticketId, Integer isActive);
}
