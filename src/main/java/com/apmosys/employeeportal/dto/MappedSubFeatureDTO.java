package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class MappedSubFeatureDTO {
	
	private Long jobRoleId;
    private String jobRoleName;
    private String employeeRole;

    private Long subFeatureId;
    private String subFeatureName;

    private Long featureId;
    private String featureName;

    public MappedSubFeatureDTO(Long jobRoleId,String jobRoleName,String employeeRole,
           Long subFeatureId,String subFeatureName,Long featureId,String featureName) {
        
    	this.jobRoleId = jobRoleId;
        this.jobRoleName = jobRoleName;
        this.employeeRole = employeeRole;
        this.subFeatureId = subFeatureId;
        this.subFeatureName = subFeatureName;
        this.featureId = featureId;
        this.featureName = featureName;
    }

}
