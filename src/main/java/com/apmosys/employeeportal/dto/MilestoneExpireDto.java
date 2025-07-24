package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.Data;

@Data
public class MilestoneExpireDto {
	
	
	private Long id;    // Milestone ID
	private Long poId;
	private Long projectId;
	private String name;  	// Milestone name
	private Date startDate;
	private Date endDate;
	private String description;
	private String remarks;
	private String status;
	private Long lienItemId;   
	private String lineItemName;
	private String projectName;
	private String poNo;
	private Long updatedBy;
	
    
	
	

   
    
    
   

 
  
    
	

}
