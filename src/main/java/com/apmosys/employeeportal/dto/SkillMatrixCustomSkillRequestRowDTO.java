package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.Data;

@Data
public class SkillMatrixCustomSkillRequestRowDTO {

	private Long requestId;
	private Long requestedByEmpId;
	private String requestedByName;
	private String designation;
	private String deptName;
	private String hodName;
	private String skillName;
	private Integer categoryId;
	private String categoryName;
	private String status;
	private String approvedSkillType;
	private String decisionComment;
	private Integer createdSkillId;
	private boolean actionEnabled;
	private Date createdAt;
	private Date decidedAt;
}
