package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RMGTeam {
	
	private Long teamId;
	private String teamName;
	private String isActive;
	private String employeeRole;
	private String status;


}
