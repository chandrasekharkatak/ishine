package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class ReimbursementDTO {
	
	private String name;          
    private String empId;         
    private String designationName;
    private String departmentName; 
    private String managerName;   
    private String mobileNo;
    private String amount;        
    private String travelMode;      
    private String distance;     
    private String fromDate;     
    private String toDate;       
    private String purpose;        
    private String fileData;
    private String selectedReason;    
    private String selectedCurrency;      
}
