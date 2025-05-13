package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

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

	private Long projectInsightResponseMetadataId;

	private Long questionMasterId;

	@Column(length = 3000)
	private String response;

	private String documentPath;

	private String documentFileName;
	
	@Column(columnDefinition = "varchar(10) DEFAULT 'N'")
	private String isApprovedForKnowledgeHub;

	@Transient
	private String responseType;

	@Transient
	private String isFinalSubmitted;

	@Transient
	private Long responseBy;
	
	public ProjectInsightResponse() {
		super();
	}

	public ProjectInsightResponse(Long projectInsightResponseId, Long projectInsightResponseMetadataId,
			Long questionMasterId, String response, String documentPath, String documentFileName,String isApprovedForKnowledgeHub, Long responseBy,
			String isFinalSubmitted) {
		super();
		this.projectInsightResponseId = projectInsightResponseId;
		this.projectInsightResponseMetadataId = projectInsightResponseMetadataId;
		this.questionMasterId = questionMasterId;
		this.response = response;
		this.documentPath = documentPath;
		this.documentFileName = documentFileName;
		this.isApprovedForKnowledgeHub = isApprovedForKnowledgeHub;
		this.isFinalSubmitted = isFinalSubmitted;
		this.responseBy = responseBy;
	}

}
