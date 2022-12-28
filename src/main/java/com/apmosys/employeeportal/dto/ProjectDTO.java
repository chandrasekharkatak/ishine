package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.util.List;

import com.apmosys.employeeportal.model.EmployeeTeamMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

}
