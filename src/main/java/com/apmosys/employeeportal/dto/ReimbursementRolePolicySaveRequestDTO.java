package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReimbursementRolePolicySaveRequestDTO {
	private Long expensePolicyId;
	private Long jobRoleId;
	private List<Long> jobRoleIds = new ArrayList<>();
	private Long updatedBy;
	private List<ReimbursementRolePolicyRowDTO> policies = new ArrayList<>();
}
