package com.apmosys.employeeportal.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reimbursement_ticket_claim")
public class ReimbursementTicketClaim {

	public static final String STATUS_PENDING_HOD = "PENDING_HOD";
	/** Matrix-driven tickets awaiting the current approval level. */
	public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
	public static final String STATUS_LEVEL_REJECTED = "LEVEL_REJECTED";
	public static final String STATUS_HOD_REJECTED = "HOD_REJECTED";
	public static final String STATUS_PENDING_HR = "PENDING_HR";
	public static final String STATUS_HR_REJECTED = "HR_REJECTED";
	public static final String STATUS_PENDING_FINANCE = "PENDING_FINANCE";
	public static final String STATUS_FINANCE_REJECTED = "FINANCE_REJECTED";
	public static final String STATUS_PAID = "PAID";
	/** Claim is queued until the ticket's processing cycle month opens for approval. */
	public static final String STATUS_HELD_FOR_CYCLE = "HELD_FOR_CYCLE";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "claim_id")
	private Long claimId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_id", nullable = false)
	private ReimbursementTicket ticket;

	@Column(name = "line_no")
	private Integer lineNo;

	@Column(name = "expenditure_type")
	private String expenditureType;

	@Column(name = "expenditure_type_description", length = 255)
	private String expenditureTypeDescription;

	@Column(name = "amount", precision = 15, scale = 2)
	private BigDecimal amount;

	@Column(name = "travel_mode")
	private String travelMode;

	@Column(name = "distance")
	private BigInteger distance;

	@Column(name = "vehicle_type")
	private String vehicleType;

	@Column(name = "food_allowance_type")
	private String foodAllowanceType;

	@Column(name = "date_of_food")
	private Timestamp dateOfFood;

	@Column(name = "from_date")
	private Timestamp fromDate;

	@Column(name = "to_date")
	private Timestamp toDate;

	@Column(name = "purpose", length = 4000)
	private String purpose;

	@Column(name = "business_justification", columnDefinition = "TEXT")
	private String businessJustification;

	@Column(name = "hod_approval", nullable = false)
	private Integer hodApproval;

	@Column(name = "pre_approval_date")
	private java.sql.Date preApprovalDate;

	@Column(name = "pre_approval_doc_ids", length = 2000)
	private String preApprovalDocIds;

	@Column(name = "project_id")
	private Long projectId;

	@Column(name = "project_name", length = 500)
	private String projectName;

	@Column(name = "client_id")
	private Integer clientId;

	@Column(name = "client_name", length = 512)
	private String clientName;

	@Column(name = "reimbursement_client_id")
	private Long reimbursementClientId;

	@Column(name = "client_category", length = 64)
	private String clientCategory;

	@Column(name = "recurring_expense", nullable = false)
	private Integer recurringExpense;

	@Column(name = "poc_project", nullable = false)
	private Integer pocProject;

	@Column(name = "doc_ids", length = 2000)
	private String docIds;

	@Column(name = "claim_status", nullable = false)
	private String claimStatus;

	@Column(name = "hod_remarks", length = 2000)
	private String hodRemarks;

	@Column(name = "hr_remarks", length = 2000)
	private String hrRemarks;
}
