package com.apmosys.employeeportal.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
public class ProjectInsightSubDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subDomainId;

    private String subDomain;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private ProjectInsightDomain domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_sub_domain_id")
    @JsonIgnore
    private ProjectInsightSubDomain parentSubDomain;
    private Boolean isActive;

    @OneToMany(mappedBy = "parentSubDomain", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectInsightSubDomain> children = new ArrayList<>();

    @OneToMany(mappedBy = "subDomain", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectInsightServiceModel> services = new ArrayList<>();
}
