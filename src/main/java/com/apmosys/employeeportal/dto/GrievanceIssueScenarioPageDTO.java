package com.apmosys.employeeportal.dto;

import java.util.List;

import com.apmosys.employeeportal.model.GrievanceIssueScenario;

import lombok.Data;

@Data
public class GrievanceIssueScenarioPageDTO {

	private List<GrievanceIssueScenario> content;
	private long totalElements;
	private int totalPages;
	/** 1-based page index for UI */
	private int page;
	private int size;
	private String sortBy;
	private String sortDir;
}
