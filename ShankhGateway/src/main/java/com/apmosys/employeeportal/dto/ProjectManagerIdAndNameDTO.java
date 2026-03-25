package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data; 

@Data
@AllArgsConstructor
public class ProjectManagerIdAndNameDTO {
    private Long empId;
    private String name;
}