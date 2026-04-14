package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HandleTeamsAsPerLinkedPoPayloadDTO {
	
	public HandleTeamsAsPerLinkedPoProjectDTO primaryProject;
	public List<HandleTeamsAsPerLinkedPoProjectDTO> deletedProjects;
}