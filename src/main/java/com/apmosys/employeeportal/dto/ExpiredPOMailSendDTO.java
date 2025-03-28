package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpiredPOMailSendDTO {

	
	private String userEmail;
	private String poNo;
	private String projectName;
	private String startDate;
	private String endDate;
	
}
