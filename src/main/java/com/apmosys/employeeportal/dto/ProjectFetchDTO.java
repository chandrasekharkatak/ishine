package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectFetchDTO {
	private Integer projectId;
	private Timestamp createdOn;
	private String projectName;
	private String state;
	private Integer clientId;
	private Long poProjectId;
	private String active;
	private String syncProject;
	private Long createdBy;
	private Long updatedBy;
	private LocalDateTime updatedOn;
	private String isDraftProject;
	private String poEndDate;
	private String poNo;
	private String poProjectType;
	private String poStartDate;
	private String apmosysRM;
	private String clientRM;
	private String deptId;
	private Boolean isRenewable;
	private String status;
	private String apmosysRmEmail;
	private String projectCompletionDate;
	private String projectStatus;
	private String internalProjectType;
	private String clientName;
	private String draftStatus;

	public ProjectFetchDTO(Integer projectId, Timestamp createdOn, String projectName, String state, Integer clientId,
			Long poProjectId, String active, String syncProject, Long createdBy, Long updatedBy,
			LocalDateTime updatedOn, String isDraftProject, String poEndDate, String poNo, String poProjectType,
			String poStartDate, String apmosysRM, String clientRM, String deptId, Boolean isRenewable, String status,
			String apmosysRmEmail, String projectCompletionDate, String projectStatus, String internalProjectType,String clientName,
			String draftStatus) {
		this.projectId = projectId;
		this.createdOn = createdOn;
		this.projectName = projectName;
		this.state = state;
		this.clientId = clientId;
		this.poProjectId = poProjectId;
		this.active = active;
		this.syncProject = syncProject;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.updatedOn = updatedOn;
		this.isDraftProject = isDraftProject;
		this.poEndDate = poEndDate;
		this.poNo = poNo;
		this.poProjectType = poProjectType;
		this.poStartDate = poStartDate;
		this.apmosysRM = apmosysRM;
		this.clientRM = clientRM;
		this.deptId = deptId;
		this.isRenewable = isRenewable;
		this.status = status;
		this.apmosysRmEmail = apmosysRmEmail;
		this.projectCompletionDate = projectCompletionDate;
		this.projectStatus = projectStatus;
		this.internalProjectType = internalProjectType;
		this.clientName = clientName;
		this.draftStatus = draftStatus;
	}
	
	public ProjectFetchDTO(Integer projectId, Timestamp createdOn, String projectName, String state, Integer clientId,
			Long poProjectId, String active, String syncProject, Long createdBy, Long updatedBy,
			LocalDateTime updatedOn, String isDraftProject, String poEndDate, String poNo, String poProjectType,
			String poStartDate, String apmosysRM, String clientRM, String deptId, Boolean isRenewable, String status,
			String apmosysRmEmail, String projectCompletionDate, String projectStatus,
			String draftStatus) {
		this.projectId = projectId;
		this.createdOn = createdOn;
		this.projectName = projectName;
		this.state = state;
		this.clientId = clientId;
		this.poProjectId = poProjectId;
		this.active = active;
		this.syncProject = syncProject;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.updatedOn = updatedOn;
		this.isDraftProject = isDraftProject;
		this.poEndDate = poEndDate;
		this.poNo = poNo;
		this.poProjectType = poProjectType;
		this.poStartDate = poStartDate;
		this.apmosysRM = apmosysRM;
		this.clientRM = clientRM;
		this.deptId = deptId;
		this.isRenewable = isRenewable;
		this.status = status;
		this.apmosysRmEmail = apmosysRmEmail;
		this.projectCompletionDate = projectCompletionDate;
		this.projectStatus = projectStatus;
		this.draftStatus = draftStatus;
	}

}
