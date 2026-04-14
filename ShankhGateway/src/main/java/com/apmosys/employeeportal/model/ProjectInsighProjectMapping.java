package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.bson.types.ObjectId;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class ProjectInsighProjectMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long projectInsightProjectMappingId;
	private Integer projectId;
	private String projectInsightId;
	private String projectInsightDetailsId;
    private String isDraft;
    
    private Long createdBy;
	private Long updatedBy;
	private Timestamp updatedOn;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;

}
