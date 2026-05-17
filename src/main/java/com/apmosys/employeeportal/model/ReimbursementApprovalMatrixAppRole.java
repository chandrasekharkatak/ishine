package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reimbursement_approval_matrix_app_role")
@IdClass(ReimbursementApprovalMatrixAppRoleId.class)
public class ReimbursementApprovalMatrixAppRole {

	@Id
	@Column(name = "matrix_id")
	private Long matrixId;

	@Id
	@Column(name = "job_role_id")
	private Long jobRoleId;
}
