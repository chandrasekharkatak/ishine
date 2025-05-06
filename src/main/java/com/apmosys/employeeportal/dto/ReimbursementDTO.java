package com.apmosys.employeeportal.dto;

import java.math.BigInteger;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class ReimbursementDTO {
	
	private String name;   
	private BigInteger requestId;
    private BigInteger empId;  
    private String email;
    private String designationName;
    private String departmentName; 
    private String managerName;   
    private BigInteger mobileNo;
    private BigInteger amount;        
    private String travelMode;      
    private BigInteger distance;     
    private Timestamp fromDate;     
    private Timestamp toDate;       
    private String purpose;        
    private String fileData;
    private String selectedReason;    
    private String selectedCurrency;   
    private String expenditureType;
    private String level;
    private Long levelOneApprover;
    private Long levelTwoApprover;
    private String levelThreeApprover;
    private String level1approverRemarks;
    private String level2approverRemarks;
    private String level3approverRemarks;
    private String level1approveremail;
    private String level2approveremail;
    private String level3approveremail;
    private String level3approvername;
    private String status;
    private String docId;
    private String finalStatus;
    private String vehicleType;
    private String foodAllowanceType;
    private Timestamp dateOfFood;
    
    
   
}
