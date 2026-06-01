package com.apmosys.employeeportal.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReimbursementRolePolicyRowDTO {
	private String policyCategory;
	private String itemName;
	private BigDecimal maxAmount;
	private Boolean allowed;
}
