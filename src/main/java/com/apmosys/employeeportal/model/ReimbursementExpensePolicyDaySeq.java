package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Per-calendar-day counter for public expense policy numbers (APM-RMPOL-YYYYMMDD-####). */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reimbursement_expense_policy_day_seq")
public class ReimbursementExpensePolicyDaySeq {

	@Id
	@Column(name = "day_key", length = 8, nullable = false)
	private String dayKey;

	@Column(name = "last_seq", nullable = false)
	private Integer lastSeq;
}
