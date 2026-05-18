package com.apmosys.employeeportal.model;

import java.math.BigInteger;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reimbursement_ticket_audit")
public class ReimbursementTicketAuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "audit_id")
	private Long auditId;

	@Column(name = "ticket_id")
	private Long ticketId;

	@Column(name = "claim_id")
	private Long claimId;

	@Column(name = "actor_emp_id")
	private BigInteger actorEmpId;

	@Column(name = "actor_email")
	private String actorEmail;

	@Column(name = "action", length = 120)
	private String action;

	@Column(name = "remarks", length = 4000)
	private String remarks;

	@Column(name = "created_on")
	private Timestamp createdOn;
}
