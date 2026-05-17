package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubmitLockStatusDTO {
	private boolean locked;
	/** True when employee already has an approved submission (any cycle). */
	private boolean hasApproved;
	/** Latest approved submission_id (when hasApproved=true). */
	private String approvedSubmissionId;
	/** submission_id when locked */
	private String submissionId;
	/** submission status (submitted / under_review / approved / rejected / draft / etc.) */
	private String submissionStatus;
	/** manager_approved / hod_approved / final_status snapshot (may be null) */
	private String managerApprovalStatus;
	private String hodApprovalStatus;
	private String finalStatus;
	/** Pending with: MANAGER / HOD (null when not locked) */
	private String pendingWith;
	private String pendingWithName;
}

