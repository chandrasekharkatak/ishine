package com.apmosys.employeeportal.model;

import java.math.BigInteger;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "travel_desk_ticket_audit")
public class TravelDeskTicketAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "audit_id")
	private Long auditId;

	@Column(name = "ticket_id")
	private Long ticketId;

	@Column(name = "line_id")
	private Long lineId;

	@Column(name = "actor_emp_id")
	private BigInteger actorEmpId;

	@Column(name = "actor_email")
	private String actorEmail;

	@Column(name = "action")
	private String action;

	@Column(name = "remarks")
	private String remarks;

	@Column(name = "created_on", insertable = false, updatable = false)
	private Timestamp createdOn;
}
