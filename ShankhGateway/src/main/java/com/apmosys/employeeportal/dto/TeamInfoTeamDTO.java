package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TeamInfoTeamDTO {
	private Long teamId;
    private String teamName;
    private String teamLeadName;
    private String spoc;
    private List<TeamInfoTeamMemberDTO> teamMemberDetails;
}
