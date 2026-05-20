package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reimbursement_approval_matrix_level")
@Getter
@Setter
public class ReimbursementApprovalMatrixLevel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "level_id")
	private Long levelId;

	@Column(name = "matrix_id", nullable = false)
	private Long matrixId;

	@Column(name = "level_order", nullable = false)
	private Integer levelOrder;

	@Column(name = "routing_mode", nullable = false, length = 64)
	private String routingMode;

	@Column(name = "specific_employee_id")
	private Long specificEmployeeId;
}
