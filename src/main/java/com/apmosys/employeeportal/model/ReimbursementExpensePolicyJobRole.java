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
@Table(name = "reimbursement_expense_policy_job_role")
@IdClass(ReimbursementExpensePolicyJobRoleId.class)
public class ReimbursementExpensePolicyJobRole {

	@Id
	@Column(name = "expense_policy_id")
	private Long expensePolicyId;

	@Id
	@Column(name = "job_role_id")
	private Long jobRoleId;
}
