package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementTicketAuditLog;

public interface ReimbursementTicketAuditLogRepository extends JpaRepository<ReimbursementTicketAuditLog, Long> {

	List<ReimbursementTicketAuditLog> findByTicketIdOrderByCreatedOnAsc(Long ticketId);

	ReimbursementTicketAuditLog findTopByClaimIdAndActionOrderByCreatedOnDesc(Long claimId, String action);
}
