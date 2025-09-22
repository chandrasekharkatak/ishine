package com.apmosys.employeeportal.request;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTimesheetProjectRequest {

	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String listType;
	private Long empId;
}
