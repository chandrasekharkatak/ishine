package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BioMaxRequestDTO {
	private Long biomaxreequestId;
	private String biomaxTitle;
	private Long empId;
	private String empName;
	private Long reportingManagerId;
	private String reportingManagerName;
	private LocalDateTime biomaxrequestDate;
	private String biomaxStatus;
	private LocalDateTime createdOn;
	private Long statusBy;
	private String requestRemark;
	private LocalDateTime statusDate;
	private boolean isEnabled;
}
