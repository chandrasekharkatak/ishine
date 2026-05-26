package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of {@code skillmatrix_aspiration_chip_master} for master configuration grid. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixAspirationChipMasterListDTO {

	private Integer chipId;
	private Long deptId;
	/** Resolved label: real department name, or global pool caption when {@code deptId} is 0. */
	private String departmentName;
	private String chipLabel;
	private Integer sortOrder;
	private Boolean isActive;
}
