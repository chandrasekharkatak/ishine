package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Audited
@ToString
public class ProjectTemp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer projectTempId ;  
	  private String active ; 
	  private Timestamp approvedOn ; 
	  private Integer clientId ; 
	  private String clientLocation ; 
	  private String clientName ; 
	  private Integer count ; 
	  private Long createdBy ; 
	  private Timestamp createdOn ;  
	  private String departmentName ; 
	  private String description ; 
	  private Long empId ; 
	  private String experience ; 
	  private String isDraftProject ; 
	  private String projectEndDate ; 
	  private String poNo ; 
	  private Long poProjectId ; 
	  private String poProjectType ; 
	  private String projectStartDate ; 
	  private Long projectManagerId ; 
	  private String projectName ; 
	  private String role ; 
	  private String state ; 
	  private String syncProject ; 
	  private Long updatedBy ; 
	  private LocalDateTime updatedOn ; 
	  private String apmosysrm ; 
	  private String clientrm ; 
	  private String deptId ; 
	  private Boolean isRenewable ; 
	  private String status ; 
	  private String apmosysRmEmail ; 
	  private String projectCompletionDate ; 
	  private String projectStatus ; 
	  private Integer projectId ; 
	  private String prevPoNo;
	  private String nextPoNo;
}
