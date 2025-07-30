package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectInsightEditSubDomainData {

    private Long subDomainId;
    private String subDomain;
    private List<ProjectInsightEditServiceData> serviceList;
    
}