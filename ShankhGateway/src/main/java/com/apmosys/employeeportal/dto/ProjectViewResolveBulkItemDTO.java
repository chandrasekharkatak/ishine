package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProjectViewResolveBulkItemDTO {
	private String resolvedProjectViewId;
	private boolean redirected;
	private String resolvedProjectName;
}

