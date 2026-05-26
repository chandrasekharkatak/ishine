package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class GrievanceAuditTimelineEntryDTO {

	private Long auditId;
	private Long ticketId;
	private String eventType;
	private String summary;
	private Integer versionNo;
	private Long changedBy;
	private String changedByName;
	private String changedAt;
	private String reason;
	private List<GrievanceAuditFieldChangeDTO> fieldChanges = new ArrayList<>();
	/** True when fieldChanges is non-empty (UI expand). */
	private boolean hasFieldDetails;
}
