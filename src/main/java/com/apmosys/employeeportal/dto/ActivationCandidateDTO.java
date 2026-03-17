package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class ActivationCandidateDTO {
	
	private Long employeeTeamMapId;
	private Long empId;
	private String employeeCode;
	private String empName;
	private Integer projectId;
	private String projectName;
	private String poProjectType;
	private LocalDateTime startDate;

}
