package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeRewardsDTO {
	
	private Long rewardId;
	
	
	private int rewardType;
	
	private Long teamLeadId;
	private int isActive;
	private LocalDate fromDate;
	private LocalDate toDate;
	
	private LocalDateTime updatedOn;
	private Long updatedBy;
	private String updatedByName;
	private Long createdBy;
	private String createdByName;
	
	private String teamLeadName;
	private Long teamId;
	
	
	private Long categoryId;
	private String categoryName;
	
	
	private Long Id;
	private String rewardName;
	private List<String> rewardTypes;
	private String rewardTypeName;
	
	
	private Long rewardedTo;
	private String rewardedToByName; 
	private Long managerId;
	private String managerName;
	
	private String ofmonthyear;
	private String remark;
	
	
	private LocalDateTime createdOn;
	private String name;
	private Long empId;
	private String teamName;
	
	
}
