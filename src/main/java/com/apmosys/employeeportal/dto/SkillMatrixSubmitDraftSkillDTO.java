package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubmitDraftSkillDTO {
	private Integer skillId;
	private String skillName;
	/** Required | Optional */
	private String skillType;
	private boolean required;
	private Integer selfRating; // 1..5
	/** Manager review fields (shown when editing a rejected submission). */
	private String managerDecision; // approved | adjusted | sent_back | rejected | pending
	private String managerComment;
	/** HOD review fields (shown when editing a rejected submission). */
	private String hodDecision; // pending | approved | rejected
	private String hodComment;
	/** Step 3 evidence fields */
	private String yearsExperience;
	private String lastUsed;
	private String usageFrequency;
	private String whatCanYouDo;
	private Boolean usedInProject;
	private String githubPortfolioUrl;
	private String colleagueEndorser;
	private String knowledgeSessionNote;
	/** Step 3 evidence: certification(s) for this skill (usually 0..1 from UI). */
	private List<SkillMatrixSubmitCertificationDTO> certifications = new ArrayList<>();
	/** Step 3 evidence: trainings (usually 0..1 from UI). */
	private List<SkillMatrixSubmitTrainingDTO> trainings = new ArrayList<>();
	private List<SkillMatrixSubmitSubskillDTO> selectedSubskills = new ArrayList<>();
}

