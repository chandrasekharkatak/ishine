package com.apmosys.employeeportal.model;

import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "travel_desk")
public class TravelDesk {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "request_id")
	private BigInteger requestId;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "emp_id")
	private BigInteger empId;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "department")
	private String department;
	
	@Column(name = "mobileNo")
	private Long mobileNo;
	
	@Column(name = "requestType")
	private String requestType;
	
	@Column(name = "travelMode")
	private String travelMode;
	
	@Column(name = "travelClass")
	private String travelClass;
	
	@Column(name = "purpose")
	private String purpose;
	
	@Column(name = "fromLocation")
	private String fromLocation;
	
	@Column(name = "toLocation")
	private String toLocation;
	
	@Column(name = "fromDate")
	private Timestamp fromDate;
	
	@Column(name = "toDate")
	private Timestamp toDate;
	
	@Column(name = "appliedBy")
	private BigInteger appliedBy;
	
	@Column(name = "appliedOn")
	private Timestamp appliedOn;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "approver")
	private BigInteger approver;
	
	@Column(name = "level")
	private Integer level;
	
	@Column(name = "approverRemarks")
	private String approverRemarks;
	
	@Column(name = "docPath")
	private String docPath;
	
	@Column(name = "isActive")
	private boolean isActive;
	


}
