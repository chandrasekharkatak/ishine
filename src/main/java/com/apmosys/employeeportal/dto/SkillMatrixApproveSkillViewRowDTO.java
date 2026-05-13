package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.Data;

@Data
public class SkillMatrixApproveSkillViewRowDTO {
	private String submissionId;
	private Long employeeId;
	private String employeeName;
	private String deptName;

	private Integer skillId;
	private String skillName;
	private String skillCategory;

	private Integer selfRating;
	private String decision; // pending|approved|adjusted|sent_back|rejected
	private Integer managerRating;

	private boolean hasEvidence;
	private boolean hasCertification;
	private boolean hasTraining;
	private boolean hasProject;
	private Date submittedAt;
}

