package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class ReimbursementDashboardFilterDTO {
	private String fromDate;
	private String toDate;
	private String department;
	/** Matches displayTicketStatus label, e.g. Paid, Submitted */
	private String ticketStatus;
	/** Raw workflow stage: PENDING_HOD, PENDING_HR, PENDING_FINANCE, PAID, REJECTED */
	private String workflowStage;
	private Long projectId;
	private Integer clientId;
	/** Expenditure type on claim, e.g. Travel, Food */
	private String expenditureType;
	/** Filter tickets for this employee (portal emp_id). */
	private Long employeeEmpId;
	/** Logged-in viewer — dashboard metrics are limited to this actor's approval scope when set. */
	private Long actorEmpId;
	private String actorEmail;
	/** Job role from session (e.g. SuperAdmin, Admin) — used for org-wide dashboard scope. */
	private String actorEmployeeRole;
	/**
	 * When true, metrics include all active reimbursement tickets (same as approve-reimbursement /
	 * dashboard tab access). Sent by the client for users with reimbursement approval / dashboard access.
	 */
	private Boolean dashboardFullScope;
}
