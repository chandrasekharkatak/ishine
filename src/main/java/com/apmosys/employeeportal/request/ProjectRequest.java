package com.apmosys.employeeportal.request;

import com.apmosys.employeeportal.dto.ProjectFilterDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequest {

	private String tabName;
	private ProjectFilterDTO projectFilter;
}
