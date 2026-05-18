package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class GrievanceIssueScenarioDTO {

	private Long scenarioId;
	private String tabKey;
	/** Portal tab display name (from category dropdown); server resolves tabKey from this when set. */
	private String categoryTabName;
	private String featureName;
	private String subFeatureName;
	private String scenarioLabel;
	private Integer sortOrder;
	private Integer isActive;
}
