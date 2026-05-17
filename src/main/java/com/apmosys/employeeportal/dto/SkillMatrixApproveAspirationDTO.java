package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SkillMatrixApproveAspirationDTO {
	private String targetRole2yr;
	private String messageToManager;
	private List<String> skillsToLearn = new ArrayList<>();
}

