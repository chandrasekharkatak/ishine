package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class TravelDeskDashboardFilterDTO {
	private String fromDate;
	private String toDate;
	private String department;
	private String ticketStatus;
	private String workflowStage;
	private Long projectId;
	private Integer clientId;
	private String expenditureType;
	private Long employeeEmpId;
	private Long actorEmpId;
	private String actorEmail;
	private String actorEmployeeRole;
	private Boolean dashboardFullScope;
}
