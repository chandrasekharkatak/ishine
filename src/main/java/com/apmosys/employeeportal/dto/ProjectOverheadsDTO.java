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
	
	public ProjectOverheadsDTO(Long projectOverheadId,String projectOverheadName) {
		this.projectOverheadId = projectOverheadId;
		this.projectOverheadName = projectOverheadName;
	}
	
}
