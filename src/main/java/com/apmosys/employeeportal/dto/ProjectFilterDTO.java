package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ProjectFilterDTO {
	
	private String approvalStatus;
	private String completionStatus;
	private Long currentUserEmpId;
	private Boolean isAdmin;
	private Boolean isHod;
	private Boolean isOther;
	private List<Long> departmentsids;
	private List<GetDeptIdByRoleDTO> departments;
	
	private Integer days;

}
