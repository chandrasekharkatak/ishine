package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class SkillDomainFeatureSaveRequest {

	private Integer domainId;
	/** Optional; when null the feature is stored at domain level only. */
	private Integer subdomainId;
	private String featureName;
	private Boolean isActive;
}
