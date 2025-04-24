package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;

import com.apmosys.employeeportal.model.ProjectInsightUserContribution;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ProjectInsightUserContributionDTO {

		private Long userContributionId;
		private Long empId;
		private String response;
		private String status;
		private Long assignTo;
		private Long projectId;
		private String userDefinedProjectName;
		private String createdOn;
		private String updatedOn;
		private String onlyText;
		private String title;
		private List<Long> teamMembers;
		private Long parentContribution;
		private List<String> tags;
		
		// "preReviewer" , "Approve", "Reject"
		private String processType;
		private String remark;
		
}
