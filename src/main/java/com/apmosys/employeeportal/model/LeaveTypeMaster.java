package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	
	@Column(columnDefinition="FLOAT DEFAULT 0.0")
	private Float  noOfDays;	

	private String leaveTypeCode;
	
	private String gender;
	
	private String paidLeave;
	
	@Column(columnDefinition = "varchar(1000) DEFAULT NULL")
	private String rules;
	
	@Column(columnDefinition = "varchar(1000) DEFAULT NULL")
	private String description;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
	private Integer createdBy;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	
	private Integer updatedBy;
}
