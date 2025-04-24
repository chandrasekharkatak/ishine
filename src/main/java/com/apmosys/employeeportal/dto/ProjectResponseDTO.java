package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectResponseDTO {

	private Long projectInsightResponseId;
	private Long projectInsightResponseMetadataId;
	private Long questionMasterId;
	private String options;
	private String response;
	private String responseList;
	private Long responseBy;
	private String responseByEmpName;
	private String document;
	private String documentFileName;
	private String documentPath;
	private String responseType;
	private Long entityId;
	private String entityType;
	private String isFinalSubmitted;
	private List<ProjectInsightResponsePointsDTO> projectInsightResponsePointList;
	private boolean isApprovedForKnowledgeHub;

}
