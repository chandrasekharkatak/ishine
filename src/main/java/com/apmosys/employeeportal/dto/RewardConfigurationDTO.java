package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RewardConfigurationDTO {
	
	private String rewardName;
	
	private int categoryId;
	
	private List<String> rewardTypes;
	
	private List<CustomFilterDTO> customFilterDTOList;

}
