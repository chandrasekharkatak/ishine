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
@Table(name = "reimbursement_expense_policy")
@Getter
@Setter
public class ReimbursementExpensePolicy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "expense_policy_id")
	private Long expensePolicyId;

	/** Public reference e.g. APM-RMPOL-20260531-0001 (unique, assigned at create). */
	@Column(name = "policy_no", length = 32, unique = true)
	private String policyNo;

	@Column(name = "created_by")
	private Long createdBy;

	@Column(name = "created_on", insertable = false, updatable = false)
	private Timestamp createdOn;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(name = "updated_on", insertable = false, updatable = false)
	private Timestamp updatedOn;
}
