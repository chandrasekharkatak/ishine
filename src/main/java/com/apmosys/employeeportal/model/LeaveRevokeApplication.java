package com.apmosys.employeeportal.model;

import java.time.LocalDate;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class LeaveRevokeApplication {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long leaveRevokeId;
	private Long leaveId;
	private Long empId;
	private Short leaveRevokeStatusId;
	private Long leaveRevokeStatusUpdatedBy;
	private String reason;
	private String rejectReason;
	
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();
	

}
