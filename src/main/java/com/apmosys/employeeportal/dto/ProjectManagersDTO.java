package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProjectManagersDTO {
	
	private Long projectManagerId;
	private String projectManagerName;
	
	public ProjectManagersDTO(Long projectManagerId,String projectManagerName) {
		this.projectManagerId = projectManagerId;
		this.projectManagerName = projectManagerName;
	}

}
