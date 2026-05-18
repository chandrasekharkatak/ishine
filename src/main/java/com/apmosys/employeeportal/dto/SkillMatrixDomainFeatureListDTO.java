package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixDomainFeatureListDTO {

	private Integer featureId;
	private Integer domainId;
	private String domainName;
	/** Null when the feature is attached only to the domain. */
	private Integer subdomainId;
	private String subdomainName;
	private String featureName;
	private Boolean isActive;
}
