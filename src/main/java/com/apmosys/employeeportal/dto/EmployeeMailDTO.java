package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class EmployeeMailDTO {
    private Long empId;
    private String name;
    private Long employeementId;
    private String role;

    public EmployeeMailDTO(Long empId, String name, Long employeementId, String role) {
        this.empId = empId;
        this.name = name;
        this.employeementId = employeementId;
        this.role = role;
    }
}
