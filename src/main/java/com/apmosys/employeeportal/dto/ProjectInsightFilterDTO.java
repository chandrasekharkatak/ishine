package com.apmosys.employeeportal.dto;

import java.util.List;

import com.apmosys.employeeportal.model.ProjectInsightFilterOptions;

import lombok.Data;

@Data
public class ProjectInsightFilterDTO {

	private Long filterId;
	private String filterName;
	private List<ProjectInsightFilterOptions> optionList;
	
}
