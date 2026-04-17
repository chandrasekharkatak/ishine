package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInsightMappingWithCount {

    private Integer count;
    private List<ProjectInsighProjectMappingDTO> mappings;
    
}