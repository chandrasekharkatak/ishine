package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MilestoneAuditDTO {
	
    	private String oldValue;
    	private String newValue;

	    private Date startDate;
	    private Date endDate;
	    private String status;

	    private String extentionReason;
	    private String othersReason;
	    private Long updatedBy;
	    private Date updatedOn;
	    private Long documentId;
	    private String documentName;
	    private String updatedByName;

}
