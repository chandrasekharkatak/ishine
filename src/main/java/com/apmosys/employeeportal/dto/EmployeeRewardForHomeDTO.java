package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class EmployeeRewardForHomeDTO {
    private Long rewardId;
    private int isActive;
    private Long id; 
    private Long rewardedTo;
    private String rewardedToByName;  
    private int rewardTypeID;
    private Integer categoryId;
    private String rewardTypeName;
    private Long departmentId;
    private String department;
	public EmployeeRewardForHomeDTO(Long rewardId, int isActive, Long id, Long rewardedTo, String rewardedToByName,
			int rewardTypeID, Integer categoryId, String rewardTypeName, Long departmentId, String department) {
		super();
		this.rewardId = rewardId;
		this.isActive = isActive;
		this.id = id;
		this.rewardedTo = rewardedTo;
		this.rewardedToByName = rewardedToByName;
		this.rewardTypeID = rewardTypeID;
		this.categoryId = categoryId;
		this.rewardTypeName = rewardTypeName;
		this.departmentId = departmentId;
		this.department = department;
	}
   
    
}