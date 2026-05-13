package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Step 5 “skills to develop” chip option from {@code skillmatrix_aspiration_chip_master}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixAspirationChipDTO {

	private Integer chipId;
	private String chipLabel;
}
