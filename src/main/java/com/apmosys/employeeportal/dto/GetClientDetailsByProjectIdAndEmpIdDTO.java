package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetClientDetailsByProjectIdAndEmpIdDTO {
	
	private Integer clientId;
	private String clientName;
	private Integer clientLocationId;
	private String clientLocation;
	private Integer projectId;
	private String projectName;
	private String teamName;
	private Long teamId;

}
