package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name="projects_temp")
public class ProjectsTemp {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer projectId;
	
	private String clientName;
	private String clientLocation;
	private String state;
	@Column(unique = true)
	private String projectName;
	private String description;
	private Long projectManagerId;
	private Long empId;
	private Timestamp approvedOn;
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	private Integer clientId;
	private String departmentName;
	private Long poProjectId;
	private String active;
	private String syncProject;
	private String isDraftProject;
	private Long createdBy;
	private Long updatedBy;
	private LocalDateTime updatedOn;
	//added as per RMG Requirement
	private String role;
    private Integer count;
    private String experience;
    private String poEndDate;
    private String poNo;
    private String poStartDate;
    private String poProjectType;
}
