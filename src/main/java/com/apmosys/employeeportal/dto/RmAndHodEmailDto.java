package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class RmAndHodEmailDto {
	
	private String rmEmail;
	private String hodEmail;
	
	
	public RmAndHodEmailDto(String rmEmail,String hodEmail) {
		this.rmEmail=rmEmail;
		this.hodEmail=hodEmail;
	}

}
