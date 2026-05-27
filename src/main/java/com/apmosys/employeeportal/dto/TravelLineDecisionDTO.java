package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class TravelLineDecisionDTO {
	private Long lineId;
	private Boolean approved;
	private String remarks;
}
