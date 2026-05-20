package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubmitDraftRequest {
	/** Existing submission_id when updating an already saved draft. */
	private String submissionId;
	private Integer currentStep;

	/** Step 2+3 */
	private List<SkillMatrixSubmitDraftSkillDTO> skills = new ArrayList<>();

	/** Step 4 */
	private List<SkillMatrixSubmitDraftProjectDTO> projects = new ArrayList<>();

	/** Step 5 */
	private String targetRole2yr;
	private String messageToManager;
	private List<String> aspirationSkillNames = new ArrayList<>();
}

