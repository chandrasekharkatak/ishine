package com.apmosys.employeeportal.model;

import java.math.BigInteger;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reimbursement_client")
public class ReimbursementClient {

	public static final String CATEGORY_PROSPECTIVE_NEW = "PROSPECTIVE_NEW_CLIENT";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reimbursement_client_id")
	private Long reimbursementClientId;

	@Column(name = "client_name", nullable = false, length = 512)
	private String clientName;

	@Column(name = "client_category", nullable = false, length = 64)
	private String clientCategory;

	@Column(name = "created_by_emp_id")
	private BigInteger createdByEmpId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "is_active", nullable = false)
	private Integer isActive;
}
