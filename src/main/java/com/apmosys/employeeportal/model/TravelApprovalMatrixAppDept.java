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
@Table(name = "travel_approval_matrix_app_dept")
@IdClass(TravelApprovalMatrixAppDeptId.class)
public class TravelApprovalMatrixAppDept {

	@Id
	@Column(name = "matrix_id")
	private Long matrixId;

	@Id
	@Column(name = "dept_id")
	private Long deptId;
}
