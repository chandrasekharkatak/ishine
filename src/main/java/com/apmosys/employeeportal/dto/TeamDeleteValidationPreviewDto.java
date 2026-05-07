package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TeamDeleteValidationPreviewDto {
	private List<TeamDeleteValidationRowDto> rows = new ArrayList<>();
	private List<Long> applicableEmpIds = new ArrayList<>();
	private List<Long> blockedEmpIds = new ArrayList<>();
}

