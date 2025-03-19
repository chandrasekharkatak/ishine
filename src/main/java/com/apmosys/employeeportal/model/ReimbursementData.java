package com.apmosys.employeeportal.model;

import java.math.BigInteger;
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
@Table(name = "reimbursement_data")
public class ReimbursementData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "requestid")
	private BigInteger requestId;
	
	@Column(name ="fullName")
	private String fullName;
	
	@Column(name ="empId")
	private BigInteger empId;
	
	@Column(name ="email")
	private String email;
	
	@Column(name ="department")
	private String department;
	
	@Column(name ="mobileNo")
	private BigInteger mobileNo;
	
	@Column(name ="expenditureType")
	private String expenditureType;
	
	@Column(name ="currency")
	private String currency;
	
	@Column(name ="amount")
	private BigInteger amount;
	
	@Column(name ="travelMode")
	private String travelMode;
	
	@Column(name ="distance")
	private BigInteger distance;
	
	@Column(name ="fromDate")
	private Timestamp fromDate;
	
	@Column(name ="toDate")
	private Timestamp toDate;
	
	@Column(name ="purpose")
	private String purpose;
	
	@Column(name ="supportingDocPath")
	private String supportingDocPath;
	
	@Column(name ="isActive")
	private boolean isActiv;
}

