package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Baseline snapshot for "Add Skills" mode: previous approved submission data (read-only).
 */
@Data
public class SkillMatrixApprovedBaselineDTO {
	private String approvedSubmissionId;
	private List<SkillMatrixSubmitDraftSkillDTO> skills = new ArrayList<>();
	private List<SkillMatrixSubmitDraftProjectDTO> projects = new ArrayList<>();
	private String targetRole2yr;
	private String messageToManager;
	private List<String> aspirationSkillNames = new ArrayList<>();
}

