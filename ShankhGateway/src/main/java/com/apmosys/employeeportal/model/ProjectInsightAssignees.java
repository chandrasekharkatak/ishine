package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "project_insight_assignees")
public class ProjectInsightAssignees {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long assigneeId;
	
	private Long entityId;
    private String entityType;
	private Long assignedTo;
	
	private Long taggedBy;
    private String assignType;
	private Double contributionPercentage;
	
}
