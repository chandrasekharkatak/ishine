package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class GrievanceTicketRequestDTO {

	private String subject;
	private String category;
	private String subCategory;
	/** feature_master.feature_name chosen after sub-category. */
	private String ticketFeature;
	private String issueScenario;
	private String description;
	private String priority;
}
