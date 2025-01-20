package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeRewardsDTO {
	
	private Long rewardId;
	private String rewardTypeName;
	private Long rewardedTo;
	private String rewardedToByName; 
	private int rewardType;
	private Long managerId;
	private Long teamLeadId;
	private int isActive;
	private LocalDate fromDate;
	private LocalDate toDate;
	private String remark;
	private LocalDateTime updatedOn;
	private Long updatedBy;
	private String updatedByName;
	private Long createdBy;
	private String createdByName;
	private String managerName;
	private String teamLeadName;
	private Long teamId;
	private Long categoryId;
	private Long Id;
	private LocalDateTime createdOn;
	private String name;
	private Long empId;
	private String teamName;
	private String ofmonthyear;
}
