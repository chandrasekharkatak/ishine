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
public class EmployeeKpi {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long kpiId;
	
	private Long templateId;
	private Long empId;
	private Long assignedBy;
	private Long assignedOn;
	private String departmentId;
	private String employeeRole;
	private Long quarterId;
	
	
}
