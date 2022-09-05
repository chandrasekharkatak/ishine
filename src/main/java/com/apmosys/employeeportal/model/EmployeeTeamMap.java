package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "EmployeeTeamMapping")
public class EmployeeTeamMap {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long employeeTeamMapId;
	
	private Long empId;
	private Long teamId;
	
	private Long jobRoleId;

}
