package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientProjectReportDTO {
	
    // private Integer clientId;
    private String clientName;
    private String departmentName;
    private Integer active;
    private String projectName;
    
    private String deptIds;           
    private List<Long> deptIdsList;   
    private Long deptId;
    private Long clientId;
    private Integer totalProjects;
    private Integer totalActiveProjects;
    private Integer totalInactiveProjects;
    
    private String clientIds;
    private List<Long> clientIdsList;
    private String projectType;
    
    private Integer projectId;
    private String poProjectType;
	private String apmosysRM;
	private String clientRM;
	private String poStartDate;
    private String poEndDate;
	private String poNo;
	private Long poProjectId;
	private Timestamp createdOn;
	
	private List<ProjectManagersDTO> projectManagers;
	
    private Integer totalActiveResources;

    public ClientProjectReportDTO() {}

  
    
    
}