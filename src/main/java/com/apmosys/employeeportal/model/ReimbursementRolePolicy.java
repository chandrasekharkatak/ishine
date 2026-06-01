package com.apmosys.employeeportal.model;

import java.math.BigDecimal;
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
@Table(name = "reimbursement_role_policy")
@Getter
@Setter
public class ReimbursementRolePolicy {

	public static final String CAT_AMOUNT_LIMIT = "AMOUNT_LIMIT";
	public static final String CAT_TRAVEL_MODE = "TRAVEL_MODE";
	/** Per-day amount cap for a specific travel mode (item_name = mode type). */
	public static final String CAT_TRAVEL_MODE_LIMIT = "TRAVEL_MODE_LIMIT";
	public static final String CAT_VEHICLE_TYPE = "VEHICLE_TYPE";
	public static final String CAT_VEHICLE_RATE = "VEHICLE_RATE";
	public static final String CAT_FOOD_ALLOWANCE = "FOOD_ALLOWANCE";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long policyId;

	@Column(name = "expense_policy_id", nullable = false)
	private Long expensePolicyId;

	@Column(name = "job_role_id")
	private Long jobRoleId;

	@Column(name = "policy_category", nullable = false, length = 32)
	private String policyCategory;

	@Column(name = "item_name", length = 255)
	private String itemName;

	@Column(name = "max_amount", precision = 15, scale = 2)
	private BigDecimal maxAmount;

	@Column(name = "is_allowed", nullable = false, length = 1)
	private String isAllowed = "Y";

	@Column(name = "created_by")
	private Long createdBy;

	@Column(name = "created_on", insertable = false, updatable = false)
	private Timestamp createdOn;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(name = "updated_on", insertable = false, updatable = false)
	private Timestamp updatedOn;
}
