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
//	private Long empId;
	private Long rewardedTo;
	private int rewardType;
	private Long managerId;
	private Long teamLeadId;
	private boolean isActive;
	private LocalDate fromDate;
	private LocalDate toDate;
	private String remark;
	private LocalDateTime updatedOn;
	private Long updatedBy;
	private Long createdBy;
}
