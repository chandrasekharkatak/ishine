package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResourceManagementDTO {

//	private Long draftTeamId;
//	private Long id;
//	private String name;
//	private String teamName;
//	private String description;
//	private Long teamLeadId;
//	
	
	private Long id;    // poPortal Project Id
	private String name;   // project name
	private Long projectManagerId;
	private String[] department;
	private String clientName;
	private String clientLocation;
	private String clientState;
	private List<TeamDTO> teamList;
	private String projectManagerName;
	private String isDraftProject;
	private String isTeamCreated;
	private String isHOD;
	
	private Long createdBy;
	private Long updatedBy;
	
}
