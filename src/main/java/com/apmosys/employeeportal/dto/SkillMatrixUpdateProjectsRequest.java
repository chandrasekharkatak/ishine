package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SkillMatrixUpdateProjectsRequest {
	private String submissionId;
	private List<SkillMatrixSubmitDraftProjectDTO> projects = new ArrayList<>();
}

