package com.apmosys.employeeportal.dto;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

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
    private String levelOneApprover;
    private String levelTwoApprover;
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
    private List<Long> docIds; // Add this field

    // getters and setters
    public List<Long> getDocIds() {
        return docIds;
    }

    public void setDocIds(List<Long> docIds) {
        this.docIds = docIds;
    }
//    private String docIds;
    private String finalStatus;
    private String vehicleType;
    private String foodAllowanceType;
    private Timestamp dateOfFood;
    private String reportingManagerId ;
	private String reportingManagerName ;
	private String rejectReason; 
	private String reimbursementStatus;
	private Boolean isValid;
    
    
   
}
