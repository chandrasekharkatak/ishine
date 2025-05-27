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
	private List<Long> projectManagerId;
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
	private String poStartDate;
	private String poEndDate;
	private Long createdBy;
	private Long updatedBy;
	private String createdOn;
	private String deptName;
	private String projectType;
	private Integer projectId;
	private String status;
	private Long isActive;
	private String projectName;
	private Long teamId;
	private String teamName;
	private String billableType;
	private String startDate;
	private String updatedOn;
	private String employeeName;
	private Long employeementId;
	private String departmentName;
	private String endDate;
	private String isAllProj;
	private Integer active;
	private String poNo;
	private String clientLocationName;
	private String employeeRole;
	private Long clientId;
	private String apmosysRM;
	private String clientRM;
	private Boolean isRenewable;
	private String deptId;
	private List<ResourceRequirementDTO> resourceRequirements;
	private List<TeamSpocDTO> teamSpocs;
	private List<ProjectManagersDTO> projectManagers;
	private String apmosysRmEmail;
	private String projectCompletionDate;
	private String projectStatus;
    private Long shadowEmpId;
    private String shadowBillable;
    private String shadowBillableType;
    private String billable;
    private List<ProjectManagerMappingDTO> projectManagersList;
    private Long spocId;
    private String projectViewId;
}
