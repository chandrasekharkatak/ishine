package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One skill row for submit step-2 pick lists (includes sub-skills for rating step). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubmitPickSkillDTO {

	private Integer skillId;
	private String skillName;
	private String skillType;
	private Integer categoryId;
	private String categoryName;
	private List<SkillMatrixSubmitSubskillDTO> subskills = new ArrayList<>();
}
