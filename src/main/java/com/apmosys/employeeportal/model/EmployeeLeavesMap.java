package com.apmosys.employeeportal.model;

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
@ToString
@Table(name="EmployeeLeavesMapping")
public class EmployeeLeavesMap {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short employeeLeavesMapId;
	
	private Long empId;
	
	private Short leaveTypeMasterId;
	
	private Float  balance;
	
	private Float pendingForApproval;

	
}
