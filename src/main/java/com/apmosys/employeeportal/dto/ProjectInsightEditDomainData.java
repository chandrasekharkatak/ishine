package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectInsightEditDomainData {

    private Long domainId;

    private String domain;

    private List<ProjectInsightEditSubDomainData> subDomainList;

    private List<ProjectInsightEditServiceData> serviceList;

    
}