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
public class EmployeeLeavesMap {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short employeeLeavesMapId;
	
	private Long empId;
	
	private Short casualLeaves;
	private Short privilegeLeaves;
	private Short maternityLeaves;
	private Short paternityLeaves;
	private Short compensatoryOffs;
	private Short sickLeaves;
	private Short leaveWithoutPays;
	private Short fieldLeaves;

	
}
