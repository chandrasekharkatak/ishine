package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixMySubmissionListDTO {
	private String submissionId;
	private String assessmentCycle;
	private Integer cycleYear;
	private String deptName;
	private String designation;
	private String reportingManagerName;
	// Two-level workflow
	private String managerApprovalStatus;
	private String hodName;
	private String hodApprovalStatus;
	private String finalStatus;
	private String status;
	private Date submittedAt;
	private Date updatedAt;
	private Date createdAt;
}

