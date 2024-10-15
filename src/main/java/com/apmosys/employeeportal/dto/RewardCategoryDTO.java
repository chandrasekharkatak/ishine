package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RewardCategoryDTO {
    private Long rewardCategoryId;
	
	private String categoryName;

}
