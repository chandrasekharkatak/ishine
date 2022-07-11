package com.apmosys.employeeportal.dto;

import java.util.List;

import com.apmosys.employeeportal.model.EmployeeTeamMap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TeamDTO {

	private Long teamId;
	private String teamName;
	private Long teamLeadId;
	private String teamLeadName;
	private String teamMemberName;
	private Integer projectId;
	List<EmployeeTeamMap> allTeamMemberList;
	private Long updatedBy;
	private Long createdBy;
	private String createdByName;
	private String createdOn;
	private Long employeeTeamMapId;
	private Long empId;

}
