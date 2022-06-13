package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LeaveTypeMaster {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short leaveTypeMasterId;
	
	private String leaveType;
	
	private Float  noOfDays;	

	private String leaveTypeCode;
	
	private String paidLeave;
	
	@Column(columnDefinition = "varchar(1000) DEFAULT NULL")
	private String rules;
	
	@Column(columnDefinition = "varchar(1000) DEFAULT NULL")
	private String description;
	

}
