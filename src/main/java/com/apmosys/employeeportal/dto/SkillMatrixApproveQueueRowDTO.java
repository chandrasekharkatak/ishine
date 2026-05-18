package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.Data;

@Data
public class SkillMatrixApproveQueueRowDTO {
	private String submissionId;
	private Long employeeId;
	private String employeeName;
	private String deptName;
	private String designation;
	private String status;
	private Date submittedAt;
	private Date reviewDeadline;

	// Two-level workflow (single-table: skillmatrix_assessment_approval)
	private String managerName;
	/** pending | yes | no */
	private String managerApprovalStatus;
	private String hodName;
	/** pending | yes | no */
	private String hodApprovalStatus;
	/** pending | approved | rejected */
	private String finalStatus;
	/** Role-based action visibility (manager can review pending; HOD can review after manager approved). */
	private boolean actionEnabled;

	private int totalSkills;
	private int reviewedSkills;
	private int approvedCount;
	private int adjustedCount;
	private int sentBackCount;
	private int rejectedCount;
}

