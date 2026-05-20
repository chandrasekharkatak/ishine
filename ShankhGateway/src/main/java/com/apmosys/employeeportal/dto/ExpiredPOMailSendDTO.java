package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpiredPOMailSendDTO {

	
	private String userEmail;
	private PoObject expiredData;
	
	@Data
	public class PoObject {
		String poNo;
		String projectName;
		String poType;
		String endDate;
	}
	
}
