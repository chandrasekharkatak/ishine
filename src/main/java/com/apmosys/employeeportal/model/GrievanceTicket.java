package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "grievance_ticket")
@Data
public class GrievanceTicket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ticket_id")
	private Long ticketId;

	@Column(name = "ticket_number", length = 30, unique = true)
	private String ticketNumber;

	@Column(name = "created_by_emp_id", nullable = false)
	private Long createdByEmpId;

	@Column(name = "created_by_name", length = 200)
	private String createdByName;

	@Column(name = "created_by_employee_id")
	private Long createdByEmployeeId;

	@Column(name = "created_by_department", length = 200)
	private String createdByDepartment;

	/** Snapshot at ticket creation (mobile or alternate). */
	@Column(name = "created_by_phone", length = 30)
	private String createdByPhone;

	@Column(name = "created_by_project_name", length = 500)
	private String createdByProjectName;

	@Column(name = "created_by_client_name", length = 300)
	private String createdByClientName;

	@Column(name = "created_by_reporting_manager", length = 200)
	private String createdByReportingManager;

	@Column(name = "subject", nullable = false, length = 300)
	private String subject;

	@Column(name = "category", length = 150)
	private String category;

	/** Sub-feature name from sub_feature_master for the selected tab (category). */
	@Column(name = "sub_category", length = 200)
	private String subCategory;

	/** feature_master.feature_name for the feature that owns the selected sub-category. */
	@Column(name = "ticket_feature", length = 200)
	private String ticketFeature;

	/** Curated issue scenario (Timesheet / Leave modules); see grievance_issue_scenario. */
	@Column(name = "issue_scenario", length = 500)
	private String issueScenario;

	@Column(name = "description", nullable = false, length = 4000)
	private String description;

	@Column(name = "priority", length = 30)
	private String priority;

	@Column(name = "status", length = 40)
	private String status;

	@Column(name = "assigned_to_emp_id")
	private Long assignedToEmpId;

	@Column(name = "assigned_to_name", length = 200)
	private String assignedToName;

	@Column(name = "resolution_remarks", length = 4000)
	private String resolutionRemarks;

	@Column(name = "resolution_category", length = 150)
	private String resolutionCategory;

	@Column(name = "action_taken", length = 4000)
	private String actionTaken;

	@Column(name = "resolved_by", length = 200)
	private String resolvedBy;

	@Column(name = "resolution_date")
	private Timestamp resolutionDate;

	@Column(name = "resolution_doc_name", length = 255)
	private String resolutionDocName;

	@Column(name = "resolution_doc_path", length = 1000)
	private String resolutionDocPath;

	@Column(name = "feedback_rating")
	private Integer feedbackRating;

	@Column(name = "feedback_comments", length = 2000)
	private String feedbackComments;

	@Column(name = "feedback_on")
	private Timestamp feedbackOn;

	@Column(name = "proof_file_name", length = 255)
	private String proofFileName;

	@Column(name = "proof_file_path", length = 1000)
	private String proofFilePath;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "updated_on")
	private Timestamp updatedOn;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(name = "is_active")
	private Integer isActive = 1;

	/** 1 if the creator closed the ticket via rating flow; such tickets cannot be reopened. */
	@Column(name = "closed_by_creator")
	private Integer closedByCreator = 0;
}
