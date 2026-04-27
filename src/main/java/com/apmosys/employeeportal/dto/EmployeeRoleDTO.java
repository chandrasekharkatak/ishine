package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class EmployeeRoleDTO {
	
	private Long subFeatureId;
	private String subFeatureName;
	private Long featureId;
	private String featureName;
	private Long tabId;
	private String tabName;
	private List<FeatureMasterDTO> permissionList;
	
	public EmployeeRoleDTO(Long subFeatureId, String subFeatureName, Long featureId, String featureName, Long tabId,
			String tabName) {
		super();
		this.subFeatureId = subFeatureId;
		this.subFeatureName = subFeatureName;
		this.featureId = featureId;
		this.featureName = featureName;
		this.tabId = tabId;
		this.tabName = tabName;
	}
	
	

	

}
