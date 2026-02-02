package com.apmosys.employeeportal.dto;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TrainingContentDTO {
	
	private Integer contentId;
	private Integer trainingId;
	private String trainingName;
	private String contentType; // PPT, PDF, VIDEO, AUDIO, LINK
	private String contentName;
	private String contentPath;
	private String externalLinkUrl;
	private Date effectiveFrom;
	private Date effectiveTo;
	private Long fileSizeBytes;
	private String mimeType;
	private String activeStatus;
	private Boolean isCurrentlyActive;
	private Long createdBy;
	private String createdByName;
	private String createdOn;
	private Long updatedBy;
	private String updatedByName;
	private String updatedOn;
}
