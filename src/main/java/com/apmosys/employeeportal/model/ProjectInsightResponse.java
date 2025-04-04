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
@Table(name = "project_insight_response")
public class ProjectInsightResponse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long projectInsightResponseId;
	
	private Long questionMasterId;
	
	@Column(length = 1000)
	private String response;
	
	private Long empId;
	
	private String documentPath;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	private Long updatedBy;
	
	@Column(columnDefinition = "varchar(10) DEFAULT 'Y'")
	private String isActive;
	
	private String documentFileName;
	
	private Long processTo;
	
	@Column(columnDefinition = "varchar(10) DEFAULT 'Y'")
	private String isDraft;
	
}
