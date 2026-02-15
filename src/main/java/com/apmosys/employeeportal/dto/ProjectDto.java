package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ProjectDto {

    private Integer projectId;
    private String startDate;
    private Long updatedBy;

}
