package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubmitDraftProjectSkillDTO {
	private Integer skillId;
	private String skillName;
	private Integer levelUsed; // 1..5
	private String specificContribution;
}

