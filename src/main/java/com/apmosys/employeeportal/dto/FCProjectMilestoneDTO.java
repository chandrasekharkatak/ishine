package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FCProjectMilestoneDTO {

	private Long id;

	private Long poId;

	private Long projectId;

	private String name;

	private String description;

	private Date startDate;

	private Date endDate;

	private String status;

	private String remarks;

	private Long lineItemId;
	
	private String lineItemName;

	private String lineItemStatus;
	
	private Long updatedBy;
	
	private LocalDateTime updatedOn;
	
	private String documentName;
	
	private String documentPath;
    
	private String documentBase64;
	
	// Constructor for repository query
	public FCProjectMilestoneDTO(Long id,Long poId,Long projectId, String name, String description, Date startDate, Date endDate, String status, String remarks,Long lineItemId,String lineItemName,String lineItemStatus) {
		this.id = id;
		this.poId = poId;
		this.projectId = projectId;
		this.name = name;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
		this.remarks = remarks;	
		this.lineItemId = lineItemId;
		this.lineItemName = lineItemName;
		this.lineItemStatus = lineItemStatus;
	}
}
