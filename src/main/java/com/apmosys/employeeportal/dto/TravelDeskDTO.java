package com.apmosys.employeeportal.dto;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

import lombok.Data;

@Data
public class TravelDeskDTO {

	 	private BigInteger requestId;
	 	private String fullName ;
	    private BigInteger employeeId ;
	    private Long levelOneApprover;
	    private String departmentName ;
	    private String designationName ;
	    private String email ;
	    private BigInteger mobileNo ;
	    private String hodName ;
	    private String associatedTravelRequest ;
	    private String travelMode ;
	    private String travelClass ;
	    private String fromLocation;
	    private Timestamp fromDate ;
	    private String toLocation;
	    private Timestamp toDate ;
	    private String purposeOfTravel ;
	    private String supportingDocument ;
	    private String status;
	    private String level1approverRemarks;
	    private String level2approverRemarks;
	    private String level1approveremail;
	    private String level2Approver;
	    private String hotelCategory;
	    private String cityCategory;
	    private String level2ApproverStatus;
	    //private String docId;
	    private List<Long> docId; 
	    private String city;
	    private String finalStatus;
		private String reportingManagerId ;
		private String reportingManagerName ;
	    private String invoiceNo;
	    private Timestamp invoiceDate;
	    private Double amount;
	    private Long docIdTrevel;
	    private Integer travelId;
	    private String RequestType;
	    private String reimbursementStatus;
		
	
	    
}
