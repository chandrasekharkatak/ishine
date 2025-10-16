package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.util.List;

import com.apmosys.employeeportal.model.EmployeeTeamMap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTO {

	private Integer projectId;
	private String clientName;
	private String clientLocation;
	private String state;
	private String projectName;
	private String description;
	private Long projectManagerId;
	private Long empId;
	private Timestamp approvedOn;
	private Timestamp createdOn;
	private Long employeementId;
	private Long teamId;
	private String departmentName;
	private Long poProjectId;
	private Integer clientId;
    private Integer clientLocationId;
    private String employeeName;
    private Integer poClientId;
    private String poProjectManagerId;
    private List<TeamDTO> teamList;
    private String[] departmentList;
    private String createdByName;
    private String updatedByName;
    private String updatedOn;
    //added as per RMG Requirement
    private String role;
    private Integer count;
    private String experience;
    private List<ResourceRequirementDTO> resourceRequirement;
    
    private List<ResourceManagementDTO> bulkSyncList;
	private Integer page;
	private Integer size;
	private Boolean isClientDashboard;

    
    public ProjectDTO(Integer projectId , String projectName) {
 
		this.projectId = projectId;
		this.projectName = projectName;
		
	}

}
