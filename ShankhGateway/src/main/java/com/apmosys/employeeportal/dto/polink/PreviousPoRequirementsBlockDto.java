package com.apmosys.employeeportal.dto.polink;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/** One deleted / merged project block: project name + requirement rows (no project column in table). */
@Getter
@Builder
public class PreviousPoRequirementsBlockDto {

	private String projectDisplayName;
	private List<PreviousRequirementDto> rows;
}
