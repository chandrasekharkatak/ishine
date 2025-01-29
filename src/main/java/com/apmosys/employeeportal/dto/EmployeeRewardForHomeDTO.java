package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
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
}