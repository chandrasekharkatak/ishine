package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectPoPortalDTO {
	private Long id;
    private String name;
    private String projectType;
    private String projectManager;
    private String projectManagerName;
    private Timestamp startDate;
    private Timestamp endDate;
    private String status;
    private List<String> department;
    private String clientName;
    private List<String> clientLocation;
    private String clientState;
    private String createdOn;
    private String poNo;
    private String projectStartDate;
    private String projectEndDate;
    private String poProjectType;
    private String PoProjectId;
    private List<ResourceRequirementDTO> resourceRequirements;
	private String apmosysRM;
	private String clientRM;
	private Boolean isRenewable;
	private String deptId;
	private String apmosysRmEmail; 
}
