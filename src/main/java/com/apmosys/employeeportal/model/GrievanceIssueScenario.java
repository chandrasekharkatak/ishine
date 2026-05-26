package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;

@Entity
@Table(name = "grievance_issue_scenario")
@Data
public class GrievanceIssueScenario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "scenario_id")
	private Long scenarioId;

	/** Normalized storage key for the portal tab (see GrievanceService.scenarioStorageKeyForTab). */
	@Column(name = "tab_key", nullable = false, length = 50)
	private String tabKey;

	/** Populated for API responses only; matches {@code tab_master.tab_name} for admin UI. */
	@Transient
	private String categoryDisplayName;

	@Column(name = "feature_name", length = 200)
	private String featureName;

	@Column(name = "sub_feature_name", length = 200)
	private String subFeatureName;

	@Column(name = "scenario_label", nullable = false, length = 500)
	private String scenarioLabel;

	@Column(name = "sort_order")
	private Integer sortOrder = 0;

	@Column(name = "is_active")
	private Integer isActive = 1;
}
