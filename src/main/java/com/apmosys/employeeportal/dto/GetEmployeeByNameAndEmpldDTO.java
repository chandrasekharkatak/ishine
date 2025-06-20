package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class GetEmployeeByNameAndEmpldDTO {
	
	private Long empId;
	private String name;
	private String employmentId;
	
	public GetEmployeeByNameAndEmpldDTO(Long empId, String name, String employmentId) {
        this.empId = empId;
        this.name = name;
        this.employmentId = employmentId;
    }
}
