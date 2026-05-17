package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubmitDraftProjectDTO {
	/** Primary key from {@code skillmatrix_assessment_project.id} when loaded from DB. */
	private Long assessmentProjectId;
	/** hrms | self_added */
	private String projectSource;
	private Integer hrmsProjectId;
	private String projectName;
	private String clientOrType;
	private String projectStatus;
	/** Optional: prefer these over HRMS lookup when provided (yyyy-MM-dd). */
	private String startDate;
	/** Optional: prefer these over HRMS lookup when provided (yyyy-MM-dd). */
	private String endDate;
	private String durationText;
	private String employeeRole;
	private Integer allocationPct;
	private String contributionSummary;
	private boolean included = true;
	/** When true, skill domain / subdomain / feature IDs refer to skill_*_master tables. */
	private boolean domainSpecific;
	private Integer skillDomainId;
	private Integer skillSubdomainId;
	private Integer skillDomainFeatureId;
	private List<SkillMatrixSubmitDraftProjectSkillDTO> skillsApplied = new ArrayList<>();
}

