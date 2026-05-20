package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectInsightEditSubServiceData {

    private String subServiceId;
    private String subService;
    private List<ProjectInsightEditSubServiceData> subDomainList;
    
}