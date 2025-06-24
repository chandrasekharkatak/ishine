package com.apmosys.employeeportal.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DepartmentBillableDTO {
	
	
	 private String departmentName;
	    private String billableType;
	    private Long employeeCount;

	    public DepartmentBillableDTO(String departmentName, String billableType, Long employeeCount) {
	        this.departmentName = departmentName;
	        this.billableType = billableType;
	        this.employeeCount = employeeCount;
	    }
	    
	    public DepartmentBillableDTO() {}
}
