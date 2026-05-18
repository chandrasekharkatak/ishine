package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssignableEmployeeDTO {
    private Long employeeId;
    private Long empId;
    private String name;
    private String departmentName;
    private Integer deptId;
    private Boolean alreadyAssigned;
}

