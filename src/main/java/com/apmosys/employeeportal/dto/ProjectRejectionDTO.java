package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectRejectionDTO {
	 private List<Long> projectIds;

	 private List<Long> rejectionIds;

	    private String rejectRemark;
}
