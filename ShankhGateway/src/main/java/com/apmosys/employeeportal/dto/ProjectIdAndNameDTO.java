package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectIdAndNameDTO {
    private Integer projectId;
    private String projectName;
}