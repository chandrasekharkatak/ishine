package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class SkillMatrixApproveDetailDTO {
	private String submissionId;
	private Long employeeId;
	private String employeeName;
	private String deptName;
	private String designation;
	private String status;
	private Date submittedAt;
	private Date reviewDeadline;
	/** viewerRole: manager | hod (based on logged-in user) */
	private String viewerRole;
	/** pending | yes | no */
	private String managerApprovalStatus;
	/** pending | yes | no */
	private String hodApprovalStatus;
	/** pending | approved | rejected */
	private String finalStatus;

	private SkillMatrixApproveAspirationDTO aspiration;
	private List<SkillMatrixApproveDetailSkillDTO> skills = new ArrayList<>();
	/** Total skills for this submission (for server-side paging). */
	private int skillsTotal;
	/** Zero-based page index. */
	private int skillsPage;
	/** Page size. */
	private int skillsSize;

	/** Summary counts across all skills (current review round). */
	private int pendingCount;
	private int approvedCount;
	private int adjustedCount;
	private int sentBackCount;
	private int rejectedCount;
}

