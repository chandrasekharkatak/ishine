package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Row from {@code skill_category_master} (UAT: category_id, category_name). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixCategoryListDTO {

	private Integer categoryId;
	private String categoryName;
}
