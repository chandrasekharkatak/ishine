package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RMGProject {
	
	private Integer projectId;
	private String clientName;
	private String clientLocation;
	private String projectName;
	private Long poProjectId;
	private String projectStartDate;
	private String projectEndDate;
	private String apmosysRM;
	private String clientRM;
	private String poProjectType;
	private String poNo;
	private String active;
	private List<RMGTeam> rmgTeam;
	private List<ProjectManagersDTO> projectManagers;
	
}
