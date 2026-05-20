package com.apmosys.employeeportal.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reimbursement_ticket")
public class ReimbursementTicket {

	public static final String STAGE_PENDING_HOD = "PENDING_HOD";
	public static final String STAGE_PENDING_HR = "PENDING_HR";
	public static final String STAGE_PENDING_FINANCE = "PENDING_FINANCE";
	/** Ticket is at a configured approval-matrix level (see {@link #currentLevelOrder}). */
	public static final String STAGE_PENDING_LEVEL = "PENDING_LEVEL";
	public static final String STAGE_REJECTED = "REJECTED";
	public static final String STAGE_PAID = "PAID";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ticket_id")
	private Long ticketId;

	/** Public reference e.g. APM-RMB-20260513-0001 (unique, assigned at submit). */
	@Column(name = "ticket_no", length = 32, unique = true)
	private String ticketNo;

	@Column(name = "emp_id")
	private BigInteger empId;

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "email")
	private String email;

	@Column(name = "department")
	private String department;

	@Column(name = "designation")
	private String designation;

	@Column(name = "mobile_no")
	private String mobileNo;

	@Column(name = "hod_emp_id")
	private BigInteger hodEmpId;

	@Column(name = "hod_name")
	private String hodName;

	@Column(name = "hod_email")
	private String hodEmail;

	@Column(name = "workflow_stage", nullable = false)
	private String workflowStage;

	@Column(name = "approval_matrix_id")
	private Long approvalMatrixId;

	@Column(name = "current_level_order")
	private Integer currentLevelOrder = 1;

	@Column(name = "current_assignee_emp_id")
	private Long currentAssigneeEmpId;

	@Column(name = "submitted_on")
	private Timestamp submittedOn;

	@Column(name = "is_active")
	private Integer isActive;

	@Column(name = "finance_reject_reason")
	private String financeRejectReason;

	@Column(name = "paid_on")
	private Timestamp paidOn;

	@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("lineNo ASC")
	private List<ReimbursementTicketClaim> claims = new ArrayList<>();
}
