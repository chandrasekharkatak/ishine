package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;

import com.apmosys.employeeportal.enums.ProjectInsightDomainApprovedStatus;

import lombok.*;

@Entity
@Data
@Table(name = "project_insight_domain")
public class ProjectInsightDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long domainId;

    private String domain;
    private Long createdBy;

    @CreationTimestamp
    private LocalDateTime createdOn;

    private Boolean isActive;

    private Long approvedBy;

    @Enumerated(EnumType.STRING)
    private ProjectInsightDomainApprovedStatus isApproved;

    private LocalDateTime approvedOn;

    @OneToMany(mappedBy = "domain", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectInsightSubDomain> subDomains = new ArrayList<>();

    @OneToMany(mappedBy = "parentDomain", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectInsightServiceModel> services = new ArrayList<>();

}
