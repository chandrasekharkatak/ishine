package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
//import lombok.Getter;
//import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Audited
@ToString
@Table(name="projects")
public class Project {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer projectId;
	
	private String clientName;
	private String clientLocation;
	private String state;
//	@Column(unique = true)
	private String projectName;
	private String description;
	private Long projectManagerId;
	private Long empId;
	@Column(name="approved_on")
	private Timestamp approvedOn;
    
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
	private String role;
    private Integer count;
    private String experience;
    private String poStartDate;
	private String poEndDate;
	private String poNo;
	private String poProjectType;
	private String apmosysRM;
	private String clientRM;
	private Boolean isRenewable;
	private String deptId;
	private String status;
	private String apmosysRmEmail;
	private String projectCompletionDate;
	private String projectStatus;
	private String internalProjectType;
	private Boolean hasClientSideId;
	
}
