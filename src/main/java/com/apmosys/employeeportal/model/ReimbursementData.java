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
	
	@Column(name ="appliedBy")
	private BigInteger appliedBy;
	
	@Column(name ="appliedOn")
	private Timestamp appliedOn;
	
	@Column(name ="isActive")
	private Integer isActive;
	
	@Column(name ="approver")
	private BigInteger approver;
	
	@Column(name ="status")
	private String status;
	
	@Column(name = "level1_approve_by")
	private BigInteger approver1;
	
	@Column(name = "level1_approver_email")
	private String level1ApproverEmail;
	
	@Column(name = "level1_approve_on")
	private Timestamp level1ApproveOn;
	
	@Column(name = "level2_approve_by")
	private String approver2;
	
	@Column(name = "level3_approve_by")
	private String approver3;
	
	@Column(name = "level2_approver_email")
	private String level2ApproverEmail;
	
	@Column(name = "level3_approver_email")
	private String level3ApproverEmail;
	
	@Column(name = "level2_approve_on")
	private Timestamp level2ApproveOn;
	
	@Column(name = "level3_approve_on")
	private Timestamp level3ApproveOn;
	
	
	@Column(name = "level")
	private Integer level;
	
	@Column(name = "level1_approver_remarks")
	private String level1approverRemarks;
	
	@Column(name = "level2_approver_remarks")
	private String level2approverRemarks;
	
	@Column(name = "hod_name")
	private String hodName;
	
	@Column(name = "final_status")
	private String finalStatus;

	
	@Column(name = "level2_approver_name")
	private String level2approverName;
	
	@Column(name = "level2_approver_status")
	private String level2approverStatus;
	
	@Column(name = "level3_approver_status")
	private String level3approverStatus;
	
	@Column(name = "level3_approver_Remarks")
	private String level3approverRemarks;
	
	@Column(name = "foodAllowanceType")
	private String foodAllowanceType;
	
	@Column(name = "dateOfFood")
	private Timestamp dateOfFood;
	
	@Column(name = "vehicleType")
	private String vehicleType;
	
	@Column(name = "doc_id")
	private String docId;
	
	@Column(name = "level3_approver_Name")
	private String level3approverName;
	
}

