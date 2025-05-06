package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectInsightDTO {

	private Long projectId;
	private String projectName;
	private Long projectManagerId;
	private String projectManagerName;
	private Long empId;
	private String employeeRole;
	private String description;
	private List<Long> assignedToUserId;
	private String assignedToUserNames;
	private List<Long> taggedToUserId;
	private String taggedToUserNames;
	private List<ProjectQuestionDTO> questionList;
	private List<ProjectInsightMilestoneDTO> projectInsightMilestoneList;
	private String performanceTabName;
	private String createdOn;
	private Long createdBy;
	private String createdByName;
	private String updatedOn;
	private Long updatedBy;
	private List<ResponseMarksDTO> empMarkList;
	private String projectInsightQuestionTemplate;
	private List<PoProjectSyncDTO> searchResultList;
	private List<String> optionList;
	private List<ProjectInsightEntityDTO> deletedProjectInsightEntityList;
	private String isFinalSubmitted;
	private Long responseBy;
	private Long pointsBy;
	private String isPointsDrafted;
	private String transferToKnowledgeHub;
	
	// For tags
	private StringBuilder projectText;
	private String tagType;
	private boolean isExcelUploaded;
	private String actionType;
	
	//addtional info
	private String[] departmentList;
	private String State;
	private List<String> tagList;

}
