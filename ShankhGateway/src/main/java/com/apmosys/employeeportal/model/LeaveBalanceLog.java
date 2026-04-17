package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

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
public class LeaveBalanceLog {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long leaveLogId;
	
	private Long empId;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
	private Short leaveTypeMasterId;
	
	private String updateBalanceBy;
	
	private Float balance; 
	
	@Column(length = 500)
	private String message;	


}
