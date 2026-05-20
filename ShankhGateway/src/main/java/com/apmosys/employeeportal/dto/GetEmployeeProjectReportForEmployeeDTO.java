package com.apmosys.employeeportal.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetEmployeeProjectReportForEmployeeDTO {
	
	private Long empId;
    private String employmentstatus;
    private String name;
    private String email;
    private Long mobileNo;
    private Long managerId;
    private String managerName;
    private String billable;
    private String billableType;
    private String teamIds;
    private String teamName;
    private String projectIds;
    private String projectName;
    private String projectStartDate;
    private String projectEndDate;
    private String poNo;
    private String clientName;
    private String clientLocation;
    private String workLocation;
    private Float totalExperience;
    private Long departmentId;
    private String departmentName;
    private String poProjectType;
    private Long teamId;
    private Integer projectId;
    private String jobRole;
    private String employeementId;
    private String poProjectId;
    private String primaryProjectId;
    private String primaryProjectName;
    private Map<String, String> projects;
    private String clientRM;
    private String apmosysRM;
    private String effectiveStartDate;
    private String effectiveEndDate;
    private String employeementIdAccToET;

}
