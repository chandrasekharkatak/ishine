package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResourceManagementDTO {
	
	private Long id;    // poPortal Project Id
	private String name;   // project name
	private Long projectManagerId;
	private String[] department;
	private String clientName;
	private String[] clientLocation;
	private String clientState;
	private List<TeamDTO> teamList;
	private String projectManagerName;
	private String isDraftProject;
	private String isTeamCreated;
	private String isHOD;
	private String projectManager; // poProjecManager : 'A-1234'
	private Long empId;
	private String rejectReason;
	private Long poProjectId;
	
	private Long createdBy;
	private Long updatedBy;
	
	private String createdOn;
	private String deptName;
	
	private String projectType;
	private Integer projectId;
	
}
