package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class GrievanceFeedbackDTO {
	private Long ticketId;
	private Integer rating;
	private String comments;
}
