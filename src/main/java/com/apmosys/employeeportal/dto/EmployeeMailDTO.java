package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class EmployeeMailDTO {
	
    private Long empId;
    private String name;
    private Long employeementId;
    private String role;
    private String isApmosysProduct;
    private String isConsultant;
    
}
