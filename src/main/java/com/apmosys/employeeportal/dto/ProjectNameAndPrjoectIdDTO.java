package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProjectNameAndPrjoectIdDTO {

	private Integer projectId;
	private String projectName;
	private String internalProjectType;

	public ProjectNameAndPrjoectIdDTO(Integer projectId, String projectName) {
		this.projectId = projectId;
		this.projectName = projectName;
	}

	public ProjectNameAndPrjoectIdDTO(Integer projectId, String projectName, String internalProjectType) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.internalProjectType = internalProjectType;
	}

}
