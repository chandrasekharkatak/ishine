package com.apmosys.employeeportal.model;

import java.util.*;

import java.time.LocalDateTime;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Data
@Entity
@Table(name = "project_insight_sub_service")
public class ProjectInsightSubService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_sub_service_id")
    @JsonIgnore
    private ProjectInsightSubService parentSubService;

    @OneToMany(mappedBy = "parentSubService", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectInsightSubService> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    @JsonIgnore
    private ProjectInsightServiceModel service;

    private Boolean isActive;

    private Long createdBy;

    @CreationTimestamp
    private LocalDateTime createdOn;
}