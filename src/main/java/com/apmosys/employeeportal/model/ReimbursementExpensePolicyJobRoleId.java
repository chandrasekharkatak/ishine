package com.apmosys.employeeportal.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementExpensePolicyJobRoleId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long expensePolicyId;
	private Long jobRoleId;
}
