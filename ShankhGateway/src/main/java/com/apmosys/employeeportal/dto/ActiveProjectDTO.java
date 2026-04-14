package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

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

public class ActiveProjectDTO {

	private Long empId;
	private Integer projectId;
	private String projectName;
	private String poProjectType;
	
}
