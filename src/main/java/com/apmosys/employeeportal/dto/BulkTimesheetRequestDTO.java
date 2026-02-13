package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class BulkTimesheetRequestDTO {
//	private Map<Long, List<Long>> timesheetProjectMap;
	private List<Long> timesheetIds; // optional for approve
    private String status;           // APPROVED / REJECTED
    private Long updatedBy;
    private List<ProjectRejectionDTO> projectRejections;
}
