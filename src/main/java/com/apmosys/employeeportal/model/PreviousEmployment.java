package com.apmosys.employeeportal.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class PreviousEmployment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long previousEmploymentId;
	
	private String employerName;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate  dateOfJoining;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate  dateOfRelieving;
	
	private Short yearsOfExperience;
	
	private String managerName;
	
    private Long  managerContactNumber;
    
    private String designation;
    
    private String hrName;
    
    private Long hrContactNumber; 
    
    private Long empId;	

}
