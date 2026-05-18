package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectInsightEditServiceData {

    private Long serviceId;

    private String service;

    private List<ProjectInsightEditSubServiceData> subServiceList;
}