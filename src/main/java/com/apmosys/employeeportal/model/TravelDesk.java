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
	private BigInteger mobileNo;
	
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
	
	@Column(name = "level1_approve_by")
	private BigInteger approver1;
	
	@Column(name = "level1_approver_email")
	private String level1ApproverEmail;
	
	@Column(name = "level1_approve_on")
	private Timestamp level1ApproveOn;
	
	@Column(name = "level2_approve_by")
	private BigInteger approver2;
	
	@Column(name = "level2_approver_email")
	private String level2ApproverEmail;
	
	@Column(name = "level2_approve_on")
	private Timestamp level2ApproveOn;
	
	
	@Column(name = "level")
	private Integer level;
	
	@Column(name = "level1_approver_Remarks")
	private String level1approverRemarks;
	
	@Column(name = "level2_approver_Remarks")
	private String level2approverRemarks;
	
	@Column(name = "level2_approver_name")
	private String level2approverName;
	
	@Column(name = "level2_approver_status")
	private String level2approverStatus;
	
	
	@Column(name = "docPath")
	private String docPath;
	
	@Column(name = "isActive")
	private Integer isActive;
	
	@Column(name = "hod_name")
	private String hodName;
	
	@Column(name = "hotel_category")
	private String hotelCategory;
	
	@Column(name = "city_category")
	private String cityCategory;
	
	@Column(name = "doc_id")
	private String docId;
	
	


}
