package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@ToString
@Getter
@Setter
public class AppreciationDTO {
	
private Long id;	
	
    private String appreciationEventName;
    private String comment;
	private String appreciateType;	
	private String reason;	
	private String appreciationDate;
	
	//name
    private String name;
    private String nameAppreciate;
    //MAILID
    private String email;
    private String emailAppreciated;
    //employementID
	private Long appreciationBy;
	private Long appreciationTo;
	private String appreciationToName;
	private String appreciationByName;
	//manager mail
	private String managerName;	
	private String managerMail;
	private Long appreciationEventId;
	//Count Appreciation
		private Long appreciationSent;
		private Long appreciationReceived;
		private Long YouAreMyStarCount;
		private Long YouAreGemOfAPersonCount;
		private Long YouAreAproblemSolverCount;
		private Long YouAreSupportiveCount;
		private Long YouAreReliableCount;
		private Long YouAreAMotivatorCount;
		   //sent count
		private Long SentYouAreMyStarCount;
		private Long SentYouAreGemOfAPersonCount;
		private Long SentYouAreAproblemSolverCount;
		private Long SentYouAreSupportiveCount;
		private Long SentYouAreReliableCount;
		private Long SentYouAreAMotivatorCount;
		   //totalcount
		private Long TotalYouAreMyStarCount;
		private Long TotalYouAreGemOfAPersonCount;
		private Long TotalYouAreAproblemSolverCount;
		private Long TotalYouAreSupportiveCount;
		private Long TotalYouAreReliableCount;
		private Long TotalYouAreAMotivatorCount;
		
		 private String startDate; // New field for filtering
		    private String endDate;   // New field for filtering
		    private Long employeementId; // Represents the current user's ID
		    private Long empId;
	    private Long appreciationToByEmpId;
	    private Long appreciationByByEmpId;
	
}
