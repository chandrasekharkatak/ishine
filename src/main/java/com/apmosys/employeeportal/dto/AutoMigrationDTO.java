package com.apmosys.employeeportal.dto;

import java.util.List;

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
public class AutoMigrationDTO {

	    private Long roleId;
	    private String roleName;
	    private String previousPoNumber;
	    private String currentPoNumber;
	    private String projectName;
	    private String projectType;
	    private List<EmployeeImpactDTO> employees;
}
