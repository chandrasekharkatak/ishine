package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class TimesheetApprovalNewDTO {
	private List<Long> timesheetIds;
	private Long timesheetId;
	private Long loactionMappingId;
	private Long projectId;
	private Long rmId;
	private Integer statusId;
	private Long rejectionReasonId;
	private String remarks;
}
