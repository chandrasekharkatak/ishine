package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class TeamSpocDTO {
	
	private Long teamId;
    private String teamName;
    private Long spocId;
    private String spocName;
    private String[] departmentList;
    

}
