package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProjectHierarchyMappingDTO {
	
	private Integer parentProjectId;
	private Integer childProjectId;
	private boolean active;
    private Long createdBy;
    private LocalDateTime createdOn;
    private Long updatedBy;
    private LocalDateTime updatedOn;
}
