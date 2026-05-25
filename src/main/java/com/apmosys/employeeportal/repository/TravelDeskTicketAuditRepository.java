package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.TravelDeskTicketAudit;

public interface TravelDeskTicketAuditRepository extends JpaRepository<TravelDeskTicketAudit, Long> {
}
