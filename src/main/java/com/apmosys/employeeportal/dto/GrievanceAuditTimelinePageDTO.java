package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class GrievanceAuditTimelinePageDTO {

	private List<GrievanceAuditTimelineEntryDTO> content = new ArrayList<>();
	private long totalElements;
	private int totalPages;
	private int page;
	private int size;
}
