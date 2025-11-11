package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
public class ProjectInsightSubDomainDTO {
    private String subdomain;
    private List<ProjectInsightSubDomainDTO> subDomainChildrenList;
    private List<ProjectInsightServiceDTO> serviceList;
}
