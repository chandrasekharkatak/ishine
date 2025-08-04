package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter

public class AppreciationEventDTO {
	
private Long appreciationEventId;
	
	private String appreciationEventName;
	
	private String fromDate;
	
	private String toDate;
	
	private String createdOn;
	
    private List<EmployeeDTO> enableAppreciationList;
    
    private String appreciateType;
    
	private Long updatedBy;
	
	private String appreciationEventType;
private String name;
	
	private String department;
	
	private Long employeement_id;
	
	private String employmentIdAccToET;
	
	private String comment;
	
	private String AppreciationDate;
	
	   //totalcount
	private Long TotalYouAreMyStarCount;
	private Long TotalYouAreGemOfAPersonCount;
	private Long TotalYouAreAproblemSolverCount;
	private Long TotalYouAreSupportiveCount;
	private Long TotalYouAreReliableCount;
	private Long TotalYouAreAMotivatorCount;
	
	private Long totalAppreciation;
	
	
	

}
