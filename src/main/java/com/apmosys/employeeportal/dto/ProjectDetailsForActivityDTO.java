package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProjectDetailsForActivityDTO {

	private Integer projectId;
    private String projectName;
    private List<TeamIdTeamNameDTO> teams = new ArrayList<>();
}
