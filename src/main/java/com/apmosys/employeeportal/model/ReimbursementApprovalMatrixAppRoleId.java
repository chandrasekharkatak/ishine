package com.apmosys.employeeportal.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementApprovalMatrixAppRoleId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long matrixId;
	private Long jobRoleId;
}
