package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class SkillMatrixApproveSkillMetaDTO {
	private Long skillRatingId;
	private Integer skillId;
	private String skillName;
	private String skillCategory;
	private Boolean required;
	private Integer selfRating;
	private String decision;
	private Integer managerRating;
}

