package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class RmAndHodEmailDto {
	
	  private List<String> rmEmails;
	    private List<String> hodEmails;

	    public RmAndHodEmailDto(List<String> rmEmails, List<String> hodEmails) {
	        this.rmEmails = rmEmails;
	        this.hodEmails = hodEmails;
	    }
}
