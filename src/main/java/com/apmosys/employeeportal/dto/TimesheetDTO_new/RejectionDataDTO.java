package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectionDataDTO {

	private Integer projectId;
	private Long locationMappingId;
	private Long timesheetId;
	private String rejectionReason;
	private String remark;
	private String rejectedOn;
}
