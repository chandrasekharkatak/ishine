package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectIdNameClientDTO {
	private Integer projectId;
	private String projectName;
	private Integer clientId;
}
