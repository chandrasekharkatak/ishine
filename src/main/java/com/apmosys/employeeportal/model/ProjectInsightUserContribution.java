package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInsightUserContribution {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userContributionId;
	private Long empId;
	
    @Column(columnDefinition = "LONGTEXT")
	private String response;
    
    @Column(columnDefinition = "LONGTEXT")
	private String onlyTextResponse;
    
	private String status;
	private Long assignTo;
	private Long projectId;
	private String userDefinedProjectName;
	private String title;
	private Long parentContribution;
	
	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
	private Timestamp createdOn;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	
}
