package com.apmosys.employeeportal.dto;

import java.util.List;

import com.apmosys.employeeportal.model.EmployeeTeamMap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PoTeamDTO {


	private Long teamId;
	private String teamName;
	private Long teamLeadId;
	private String teamLeadName;
	private String teamMemberName;
	private Integer projectId;
	private List<EmployeeTeamMap> allTeamMemberList;
	private String createdByName;
	private String createdOn;
	private Long employeeTeamMapId;
	private Long empId;
	private List<EmployeeTeamMap> updatedTeamMemberList;
	private Long employeementId;
	
	private String projectName;
	private String clientName;
	private String clientLocation;
	private String state;
	private Long projectManagerId;
	private String projectManagerName;
	private String isActive;
	private String startDate;
	private String endDate;
	private Long poTeamId;
	private Long teamMemberId;
	private String poTeamLeadId;
	private String[] teamMemberList;
	private String description;
	private String departmentName;
	private Long teamLeadDeptId;
	private Long teamMemberDeptId;
	private String updatedOn;
	private String updatedBy;
	private String createdBy;
	private String[] departmentList;
	
	private List<PoTeamMemberDetailDTO> teamMemberDetails;

	
}
