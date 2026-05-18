package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceAuditVersionDTO {

	private Integer versionNo;
	private String changedAt;
	private String eventType;
	private String summary;
	private String changedByName;
}
