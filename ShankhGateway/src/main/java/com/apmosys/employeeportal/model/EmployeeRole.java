package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Table(name = "employee_role_master")
@ToString
public class EmployeeRole {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 20)
	private String employeeRole;

	private Long subFeatureMasterId;
	
	@Column(length = 50)
	private String subFeatureName;

	@Column(columnDefinition = "varchar(10) DEFAULT 'N'")
	private String permission;
	

}
