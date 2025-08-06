package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TeamInfoTeamMemberDTO {
	
	private Long empId;
    private String employeeName;
    private String employeeRole;
    private String billableType;
    private String startDate;
    private Integer active;
    private Integer isDefaultProject;
    private List<Map<String, Object>> otherActiveProjects;
    private Long employeeTeamMapId;
    private String department;
    private String employmentId;

}
