package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RestoreProjectPayloadDTO {
	
	private Integer projectId;
	private Long currentUserEmpId;
	
}
