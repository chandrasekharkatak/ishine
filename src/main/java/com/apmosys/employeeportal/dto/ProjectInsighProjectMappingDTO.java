package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightStructure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectInsighProjectMappingDTO {

	private Long projectInsightProjectMappingId;
	private Integer projectId;
	private String projectInsightId;
    private String isDraft;
    
    private Long createdBy;
	private Long updatedBy;
	private String updatedOn;
	private String createdOn;
	
	private String projectName;
	private String createdByName;
	private String projectManagerName;
	private String client;
	
	private ProjectInsightStructure projectInsightStructure;
	
	 public ProjectInsighProjectMappingDTO(
		        Integer projectId,
		        String projectInsightId,
		        String isDraft,
		        Long createdBy,
		        String projectName,
		        String createdByName,
		        String client,
		        String projectManagerName
		    ) {
		        this.projectId = projectId;
		        this.projectInsightId = projectInsightId;
		        this.isDraft = isDraft;
		        this.createdBy = createdBy;
		        this.projectName = projectName;
		        this.createdByName = createdByName;
		        this.projectManagerName = projectManagerName;
		        this.client = client;
		    }
	
}
