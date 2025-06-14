package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.util.List;

import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Department;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoProjectSyncDTO {

	private Integer projectId;
	private String clientName;
	private String state;
	private String projectName;
	private String description;
	private List<Long> projectManagerId;
	private Long empId;
	private Timestamp approvedOn;
	private Timestamp createdOn;
	private Long employeementId;
	private Long teamId;
	private Long poProjectId;
	private Integer clientId;
    private Integer clientLocationId;
    private String employeeName;
    private Integer poClientId;
    private String poProjectManagerId;
    private List<PoTeamDTO> teamList;
    private String[] departmentList;
    private String[] clientLocation;
    private List<ClientLocation> allClientLocationList;
    private String syncProject;
    private String status;
    private String createdBy;
    private String updatedBy;
    private String updatedOn;
	private String projectManagerName;
    private List<ProjectManagerMappingDTO> projectManagersList;
	private List<ProjectManagersDTO> projectManagers;
	private String internalProjectType;
	private String deptId;
}
