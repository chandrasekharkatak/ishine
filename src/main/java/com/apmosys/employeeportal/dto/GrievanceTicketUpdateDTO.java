package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class GrievanceTicketUpdateDTO {

	private Long ticketId;
	private String subject;
	private String category;
	private String description;
	private String priority;
	private String status;
	private Long assignedToEmpId;
	private String assignedToName;
	private String resolutionRemarks;
	private String resolutionCategory;
	private String actionTaken;
	private Integer feedbackRating;
	private String feedbackComments;
}
