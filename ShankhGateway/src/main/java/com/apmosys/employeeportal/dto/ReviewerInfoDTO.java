package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewerInfoDTO {
	
	    private Long reviewerid;
	    private String reviewerName;
	    private Integer level;
	    private LocalDateTime reviewedOn;
	    private Boolean isApproved;
	    private Integer marks;
	    private String quality;
	    private String remarks;
	    private LocalDateTime reviewAssignedOn;
	    private ReviewerInfoDTO reassignReason;

}
