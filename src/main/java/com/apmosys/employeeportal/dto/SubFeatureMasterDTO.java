package com.apmosys.employeeportal.dto;

import java.util.List;

import com.apmosys.employeeportal.model.EmployeeRole;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubFeatureMasterDTO {

	private Long featureId;
	private String featureName;
	private Long subFeatureMasterId;
	private String subFeatureName;
	private short subFeatureType;
	private boolean isActive;
	private Long roleFeatureMapId;
	private String tabName;
	private List<EmployeeRole> employeeRoleList;

	
}
