package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class TravelDeskLineDecisionDTO {
	private Long lineId;
	private Boolean approved;
	private String remarks;
}
