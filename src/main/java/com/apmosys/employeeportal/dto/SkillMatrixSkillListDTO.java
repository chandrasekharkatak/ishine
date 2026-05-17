package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSkillListDTO {

	private Integer skillId;
	private String skillName;
	private Integer categoryId;
	private String categoryName;
	private String skillType;
	private Boolean isActive;
	private Long departmentId;
}
