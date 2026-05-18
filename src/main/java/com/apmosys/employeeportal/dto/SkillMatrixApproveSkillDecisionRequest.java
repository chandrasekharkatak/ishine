package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class SkillMatrixApproveSkillDecisionRequest {
	private Long skillRatingId;
	private String decision; // approved|adjusted|sent_back|rejected
	private Integer managerRating; // required for adjusted
	private String managerComment; // required for adjusted/sent_back/rejected
}

