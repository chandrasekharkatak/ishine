package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubmitSkillDTO {

	private Integer skillId;
	private String skillName;
	private String skillType;
	private String categoryName;
	private List<SkillMatrixSubmitSubskillDTO> subskills = new ArrayList<>();
}
