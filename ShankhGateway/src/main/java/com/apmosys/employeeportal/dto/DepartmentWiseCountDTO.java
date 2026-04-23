package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class DepartmentWiseCountDTO {
	
	private String departmentName;
    private String status;
    private Long empCount;
    
    
    public DepartmentWiseCountDTO(String departmentName, String status, Long empCount) {
        this.departmentName = departmentName;
        this.status = status;
        this.empCount = empCount;
    }
    
}
