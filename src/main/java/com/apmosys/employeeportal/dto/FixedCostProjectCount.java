package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FixedCostProjectCount {

	private Long totalFixedCostcount;
	private Long expiredCount;
	private Long delayedCount;
	private Long ontimeCount;
	
}