package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkippedTimesheetDTO {
	private Long timesheetId;
    private String employmentId;
    private LocalDate date;
    private String reason;
}
