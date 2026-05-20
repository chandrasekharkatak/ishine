package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubskillListDTO {

	private Integer subskillId;
	private Integer skillId;
	private String skillName;
	private String subskillName;
	private Boolean isActive;
}
