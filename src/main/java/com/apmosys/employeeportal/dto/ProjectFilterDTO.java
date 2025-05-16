package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ProjectFilterDTO {
	
	private String approvalStatus;
	private Long currentUserEmpId;
	private List<Long> departmentsids;

}
