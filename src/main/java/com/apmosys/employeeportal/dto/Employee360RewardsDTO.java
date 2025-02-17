package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



@ToString
@Setter
@Getter
public class Employee360RewardsDTO {
	
	private String rewardTypeName;
	private Long rewardedTo;
	private LocalDateTime createdOn;
	private String remark;
	private String createdByName;
	private String name;
	private List<RewardTeamDTO> teamlist;

}
