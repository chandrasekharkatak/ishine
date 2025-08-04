package com.apmosys.employeeportal.mongodb.modal;

import java.util.Map;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "project_insight_group_details")
public class ProjectInsightGroupDetails {

	@Id
	private String id;
	private String groupName;
	private String groupType;
	private String parentId;
	private String parentType;
	private String formId;
	private Map<String, Object> additionalInfo;

	private String createdBy;
	private String createdOn;
	private String updatedBy;
	private String updatedOn;
	
}
