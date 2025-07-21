package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import com.apmosys.employeeportal.enums.ProjectInsightDomainApprovedStatus;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInsightDomainCreatedBy {

    private String domain;
    private String createdBy;
    private LocalDateTime createdOn;
    private Long domainId;
    private Boolean isActive;
    private ProjectInsightDomainApprovedStatus isApproved;
    private String approvedBy;

    public ProjectInsightDomainCreatedBy(String domain,Long domainId, LocalDateTime createdOn, String createdBy, Boolean isActive,
            ProjectInsightDomainApprovedStatus isApproved, String approvedBy) {
        this.domain = domain;
        this.domainId = domainId;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.isActive = isActive;
        this.isApproved = isApproved;
        this.approvedBy = approvedBy;
    }

}