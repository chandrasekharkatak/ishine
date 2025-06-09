package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class SetProjectMappingAndDefaultProjectDTO {
	private List<Long> empId;
	private Integer projectId;
	private Long teamId;
	private String[] employeeRole;
	private Long resourceOverViewId;
	private Long createdBy;
}
