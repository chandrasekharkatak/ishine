package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;



import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeTeamMapDTO {

    private Long employeeTeamMapId;
	private Long empId;
	private Long teamId;
	private Long jobRoleId;
	private Long active;
	private Timestamp startDate;
	private String[] employeeRole;
	private LocalDateTime updatedOn;
	private String teamMemberName;
	private Long teamMemberDeptId;
	
}
