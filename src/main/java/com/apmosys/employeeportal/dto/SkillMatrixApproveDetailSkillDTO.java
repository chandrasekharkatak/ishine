package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SkillMatrixApproveDetailSkillDTO {
	private Long skillRatingId;
	private Integer skillId;
	private String skillName;
	private String skillCategory;
	private Boolean required;

	// Employee self inputs
	private Integer selfRating;
	private String yearsExperience;
	private String lastUsed;
	private String usageFrequency;
	private String whatCanYouDo;
	private Boolean usedInProject;
	private String githubPortfolioUrl;
	private String colleagueEndorser;
	private String knowledgeSessionNote;

	// Evidence
	private List<SkillMatrixApproveSubskillDTO> subskills = new ArrayList<>();
	private List<SkillMatrixApproveCertificationDTO> certifications = new ArrayList<>();
	private List<SkillMatrixApproveTrainingDTO> trainings = new ArrayList<>();
	private List<String> projectNames = new ArrayList<>();

	// Manager decision (current review round)
	private String managerDecision; // pending|approved|adjusted|sent_back|rejected
	private String hodDecision; // pending|approved|rejected
	private String finalDecision; // pending|approved|rejected
	private Integer managerRating;
	private String managerComment;
}

