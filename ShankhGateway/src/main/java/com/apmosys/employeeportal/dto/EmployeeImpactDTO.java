package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeImpactDTO {
	
	private String employeeName;
    private String teamName;
    private String departmentName;
    private Long deptId;
    
    
    private String roleName;

    private String previousTeamName;
    private String newTeamName;

    private String previousPoNumber;
    private String currentPoNumber;

    private String reason;
    
    public EmployeeImpactDTO(String employeeName,String teamName) {
    	this.employeeName = employeeName;
    	this.teamName = teamName;
    }


}
