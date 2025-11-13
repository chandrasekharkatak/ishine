package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class ProjectSummaryDTO {
    private String projectName;
    private String clientName;
    private String apmosysRM;
    private String clientRM;
    private String poNo;
    private String poProjectType;
    private String poStartDate;
    private String poEndDate;
    private String teamName;
    private String projectManagerName;

    public ProjectSummaryDTO(String projectName, String clientName, String apmosysRM, String clientRM, String poNo,
            String poProjectType, String poStartDate, String poEndDate, String projectManagerName, String teamName) {
        this.projectName = projectName;
        this.clientName = clientName;
        this.apmosysRM = apmosysRM;
        this.clientRM = clientRM;
        this.poNo = poNo;
        this.poProjectType = poProjectType;
        this.poStartDate = poStartDate;
        this.poEndDate = poEndDate;
        this.teamName = teamName;
        this.projectManagerName = projectManagerName;
    }
}