package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class GetProjectByMonthRangeAndEmpIdDTO {
	
	private Long empId;
	private String name;
	private Integer projectId;
	private String projectName;
	private String poNo;
	private String employementId;
			
}
