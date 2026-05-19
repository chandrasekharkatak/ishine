package com.apmosys.employeeportal.model;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
public class ProjectInsightServiceModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long serviceId;

	private String service;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sub_domain_id")
    @JsonIgnore
	private ProjectInsightSubDomain subDomain;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_domain_id")
	@JsonIgnore
	private ProjectInsightDomain parentDomain;

	private Boolean isActive;

	@OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProjectInsightSubService> subServices = new ArrayList<>();
}
