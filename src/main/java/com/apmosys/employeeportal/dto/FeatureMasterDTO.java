package com.apmosys.employeeportal.dto;

import java.util.List;


import com.apmosys.employeeportal.model.TabMaster;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FeatureMasterDTO {
	
	private Long featureId;	
	private String featureName;	
	private Long tabId;
	private List<SubFeatureMasterDTO> subFeatures;
	private Long jobRoleId;
	private String permission;
	private String employeeRole;
	private Long subFeatureId;
	private String subFeatureName;
	
	
	
	
	
	
	

}
