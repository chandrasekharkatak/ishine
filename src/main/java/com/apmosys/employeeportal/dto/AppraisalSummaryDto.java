package com.apmosys.employeeportal.dto;

import lombok.*;
@Getter
@Setter
public class AppraisalSummaryDto {
	private Long employeeId;
	private Float finalRating;
	private String finalRemarks;
	private Float appraisalScore;
	private String appraisalPercentage;
}
