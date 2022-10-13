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
	
	private String appreciateType;	
	private String reason;	
	private LocalDateTime appreciationDate;
	
	//name
    private String name;
    private String nameAppreciate;
    //MAILID
    private String email;
    private String emailAppreciated;
    //employementID
	private Long appreciationBy;
	private Long appreciationTo;
	//manager mail
	private String managerName;	
	private String managerMail;
	
}
