package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class ProjectInsightDomainDataDto {
    private Long id;
    private String name;
    private String type;
    private Boolean isChildAvailable;
    private Boolean isActive;

    public ProjectInsightDomainDataDto(Long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
    public ProjectInsightDomainDataDto(Long id, String name, String type, Boolean isChildAvailable) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isChildAvailable = isChildAvailable;
    }
    public ProjectInsightDomainDataDto(Long id, String name, String type, Boolean isChildAvailable, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isChildAvailable = isChildAvailable;
        this.isActive = isActive;
    }
}
