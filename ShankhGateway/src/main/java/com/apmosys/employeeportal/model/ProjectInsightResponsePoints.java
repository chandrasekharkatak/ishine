package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
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
@Table(name = "project_insight_response_points")
public class ProjectInsightResponsePoints {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long projectInsightResponsePointId;

	private Double points;
	
	private Long pointsBy;

	private Long responseId;

	@Column(columnDefinition = "varchar(10) DEFAULT 'Y'")
	private String isPointsDrafted;
	
	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
	private Timestamp createdOn;

	private Timestamp updatedOn;

	private Timestamp finalSubmittedOn;

}