package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HandleTeamsAsPerLinkedPoProjectDTO {
	
	private Long projectId;
	private String projectName;
	private String poNo;
	private Integer clientId;
	private Timestamp startDate;
	private Timestamp endDate;
	
}
