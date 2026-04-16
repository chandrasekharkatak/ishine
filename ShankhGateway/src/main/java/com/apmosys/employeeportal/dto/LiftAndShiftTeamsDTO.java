package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LiftAndShiftTeamsDTO {
	
	private List<Long> teamIds;
	private Integer sourceProjectId; 
	private Integer targetProjectId;
	private Long currentUserEmpId;

}
