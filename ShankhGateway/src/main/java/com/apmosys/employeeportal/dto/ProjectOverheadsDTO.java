package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProjectOverheadsDTO {
	
	private Long projectOverheadId;
	private String projectOverheadName;
	private Long projectId;
	
	public ProjectOverheadsDTO(Long projectOverheadId,String projectOverheadName) {
		this.projectOverheadId = projectOverheadId;
		this.projectOverheadName = projectOverheadName;
	}
	public ProjectOverheadsDTO(Long projectId,Long projectOverheadId,String projectOverheadName) {
		this.projectId = projectId;
		this.projectOverheadId = projectOverheadId;
		this.projectOverheadName = projectOverheadName;
	}
	
}
