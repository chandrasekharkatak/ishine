package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Set;

import com.apmosys.employeeportal.model.Notification;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AppreciationAndRewardsCountDto {
	
    private Long empId;
    private String appreciationCount;
    private String rewardsCount;
    private String averageRating;
}
