package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DepartmentLeavePolicyMap {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short departmentLeavePolicyMapId;
	
	private Long deptId;
	
	private Short leavePolicyMasterId;
	
}
