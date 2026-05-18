package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.ReimbursementTicketClaim;

public interface ReimbursementTicketClaimRepository extends JpaRepository<ReimbursementTicketClaim, Long> {

	List<ReimbursementTicketClaim> findByTicketTicketIdOrderByLineNo(Long ticketId);
}
