package com.apmosys.employeeportal.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CompOffLeave {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long compOffLeaveId;
	
	private Long empId;
	private Integer managerId;
	
	private Short leaveTypeMasterId;
	private Short leaveStatusId;
	
	private String reason;
	@Column(length = 500)
	private String description;
	
	@Embedded
	private CommonProperties commonProperties = new CommonProperties();
	
	private LocalDate fromDate;
	private LocalDate toDate;
	private Float noOfDays;	
	private String leaveCode;

}
