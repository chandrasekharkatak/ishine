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
@Table(name = "travel_desk_ticket")
public class TravelDeskTicket {

	public static final String STAGE_PENDING_LEVEL = "PENDING_LEVEL";
	public static final String STAGE_PENDING_ADMIN = "PENDING_ADMIN";
	public static final String STAGE_COMPLETED = "COMPLETED";
	public static final String STAGE_REJECTED = "REJECTED";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ticket_id")
	private Long ticketId;

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

	@Column(name = "manager_emp_id")
	private BigInteger managerEmpId;

	@Column(name = "manager_name")
	private String managerName;

	@Column(name = "manager_email")
	private String managerEmail;

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

	@Column(name = "admin_remarks")
	private String adminRemarks;

	@Column(name = "completed_on")
	private Timestamp completedOn;

	@Column(name = "admin_actor_emp_id")
	private Long adminActorEmpId;

	@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("lineNo ASC")
	private List<TravelDeskTicketLine> lines = new ArrayList<>();
}
