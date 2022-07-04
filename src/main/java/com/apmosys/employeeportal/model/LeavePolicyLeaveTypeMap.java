package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LeavePolicyLeaveTypeMap {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long leavepolicyLeaveTypeMapId;
	
	private Short leaveTypeMasterId;
	
	private Short leavePolicyMasterId;
	
	private String increment;
	
	private Short incrementValue;

}
