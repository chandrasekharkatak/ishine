package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewerInfoDTO {
	
	    private Long reviewerid;
	    private LocalDateTime reviewedOn;
	    private Boolean isApproved;
	    private Integer marks;
	    private String remarks;
	    private LocalDateTime reviewAssignedOn;

}
