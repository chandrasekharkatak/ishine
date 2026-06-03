package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reimbursement_approval_matrix_level_emp")
@IdClass(ReimbursementApprovalMatrixLevelEmpId.class)
@Getter
@Setter
@NoArgsConstructor
public class ReimbursementApprovalMatrixLevelEmp {

	@Id
	@Column(name = "level_id", nullable = false)
	private Long levelId;

	@Id
	@Column(name = "emp_id", nullable = false)
	private Long empId;

	public ReimbursementApprovalMatrixLevelEmp(Long levelId, Long empId) {
		this.levelId = levelId;
		this.empId = empId;
	}
}

