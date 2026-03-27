package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.Data;

@Data
public class MilestoneExtendedDate {

	 	private Long id;
	    private Date extendedDate;
	    private Long updatedBy;
	    private String extentionReason;
	    private Date updatedOn;
	    private byte[] documentContent;
	    private String documentName;
	    private String documentType;
	    private String updatedByName;
	
}

