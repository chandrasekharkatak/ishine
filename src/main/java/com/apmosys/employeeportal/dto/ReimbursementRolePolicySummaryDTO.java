package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReimbursementRolePolicySummaryDTO {

	private Long expensePolicyId;
	private String policyNo;
	private Long representativeJobRoleId;
	private String jobRoleName;
	private List<String> jobRoleNames = new ArrayList<>();
	private String policySummary;
	private int policyCount;
	private String updatedByName;
	private Timestamp updatedOn;
}
