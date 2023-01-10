package com.apmosys.employeeportal.dto;

import lombok.Setter;
import lombok.ToString;
import lombok.Getter;

@Getter
@Setter
@ToString
public class PreviousEmploymentDTO {
	
	private Long previousEmploymentId;	
	private String employerName;	
	private String  dateOfJoining;	
	private String  dateOfRelieving;	
	private Float yearsOfExperience;	
	private String managerName;	
    private Long  managerContactNumber;    
    private String designation;    
    private String hrName;    
    private Long hrContactNumber;
    private Long empId;	
    private String isDraft;
}
