package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reimbursement_approval_matrix")
@Getter
@Setter
public class ReimbursementApprovalMatrix {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "matrix_id")
	private Long matrixId;

	@Column(name = "matrix_name", nullable = false, length = 120)
	private String matrixName;

	@Column(name = "is_active", length = 1)
	private String isActive = "Y";

	@Column(name = "finance_dept_id")
	private Long financeDeptId;

	@Column(name = "finance_assignee_emp_id")
	private Long financeAssigneeEmpId;

	@Column(name = "created_by")
	private Long createdBy;

	@Column(name = "created_by_name", length = 200)
	private String createdByName;

	@Column(name = "created_on", insertable = false, updatable = false)
	private Timestamp createdOn;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(name = "updated_by_name", length = 200)
	private String updatedByName;

	@Column(name = "updated_on", insertable = false)
	private Timestamp updatedOn;
}
