package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentCountDTO {
	
	private Long deptId;
    private String deptCode;
    private String deptName;
    private Integer count;
    
}
