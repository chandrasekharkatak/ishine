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
@Table(name = "reimbursement_approval_matrix_level_dept")
@IdClass(ReimbursementApprovalMatrixLevelDeptId.class)
public class ReimbursementApprovalMatrixLevelDept {

	@Id
	@Column(name = "level_id")
	private Long levelId;

	@Id
	@Column(name = "dept_id")
	private Long deptId;
}
