package com.apmosys.employeeportal.dto;



import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeCertificateDTO {
	
	
	private String certificationName;
	private Long employeeCertificateId;
	private String certificationId;	
	private String duration;
	private String dateOfCompletion;	
	private String modeOfCourse;	
	private Long certificationNumber;
    private Long empId;	
	

}
