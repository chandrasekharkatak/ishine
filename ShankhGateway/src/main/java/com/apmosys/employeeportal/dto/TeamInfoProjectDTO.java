package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TeamInfoProjectDTO {
	
	private Integer projectId;
    private String projectName;
    private Long clientId;
    private String clientName;
    private List<TeamInfoTeamDTO> teamDetails;

}
