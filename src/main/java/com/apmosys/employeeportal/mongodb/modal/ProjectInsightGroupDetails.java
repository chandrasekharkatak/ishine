package com.apmosys.employeeportal.mongodb.modal;

import java.util.Map;
import java.util.List;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "project_insight_group_details")
@NoArgsConstructor
public class ProjectInsightGroupDetails {

	@Id
	private String id;
	private String groupTitle;
	private String groupType;
	private String parentId;
	private String parentType;
	private String formId;
	private Map<String, Object> additionalInfo;
	private List<String> parentPathIds; 
	private String projectDetailsId;
	private Integer projectId;
	private String projectName;

	private String createdBy;
	private String createdOn;
	private String updatedBy;
	private String updatedOn;

	private List<Long> facetCategoryIds;
    private List<Long> facetValueIds;

	public ProjectInsightGroupDetails(String id, String groupTitle, String parentId, String parentType) {
		this.id = id;
		this.groupTitle = groupTitle;
		this.parentId = parentId;
		this.parentType = parentType;
	}
}
