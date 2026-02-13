package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectRejectionDTO {
	 private Long projectId;

	 private List<Long> rejectionIds;

	    private String rejectRemark;
}
